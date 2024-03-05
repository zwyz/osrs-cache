package osrs.unpack.script;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;

import java.util.*;

import static osrs.unpack.script.Command.*;

public class ScriptUnpacker {
    public static final boolean DISASSEMBLE_ONLY = false;
    public static final boolean PROPAGATE_TYPES = true;
    public static final boolean KEEP_LABELS = false;
    public static final boolean ASSUME_UNKNOWN_TYPES_ARE_BASE = true;
    public static final boolean OUTPUT_TYPE_ALIASES = false;
    public static final boolean ASSUME_UNUSED_IS_CLIENTSCRIPT = true;
    public static final boolean FORMAT_HOOKS = true;
    public static final boolean CHECK_NONEMPTY_STACK = true;
    public static final boolean CHECK_EMPTY_ARGUMENT = true;
    private static final Map<Integer, CompiledScript> SCRIPTS = new HashMap<>();
    private static final Map<Integer, List<Expression>> SCRIPTS_DECOMPILED = new HashMap<>();
    private static final Map<Integer, Integer> SCRIPT_PARAMETER_COUNT = new HashMap<>();
    private static final Map<Integer, Integer> SCRIPT_RETURN_COUNT = new HashMap<>();
    public static final Map<Integer, List<Type>> SCRIPT_PARAMETERS = new HashMap<>();
    public static final Map<Integer, List<Type>> SCRIPT_RETURNS = new HashMap<>();
    public static final Set<Integer> CALLED = new LinkedHashSet<>();
    public static final Set<Integer> CLIENTSCRIPT = new LinkedHashSet<>();

    public static void load(int id, byte[] data) {
        var script = CompiledScript.decode(data);
        SCRIPTS.put(id, script);
    }

    public static int getParameterCount(int script) {
        return SCRIPT_PARAMETER_COUNT.get(script);
    }

    public static int getParameterCount(int script, LocalDomain domain) {
        return switch (domain) {
            case INTEGER -> SCRIPTS.get(script).argumentCountInt;
            case STRING -> SCRIPTS.get(script).argumentCountObject;
        };
    }

    public static int getReturnCount(int script) {
        if (script == 4137) {
            return 0; // todo: beta
        }

        return SCRIPT_RETURN_COUNT.get(script);
    }

    public static void decompile() {
        if (DISASSEMBLE_ONLY) {
            return;
        }

        // compute parameter/return counts
        for (var id : SCRIPTS.keySet()) {
            var script = SCRIPTS.get(id);
            SCRIPT_PARAMETER_COUNT.put(id, script.argumentCountInt + script.argumentCountObject);
            var count = 0;

            for (var i = script.code.length - 2; i >= 0; i--) {
                var command = script.code[i].command();

                if (command != PUSH_CONSTANT_INT && command != PUSH_CONSTANT_STRING) {
                    break;
                }

                count++;
            }

            SCRIPT_RETURN_COUNT.put(id, count);
        }

        // decompile
        for (var id : SCRIPTS.keySet()) {
            var script = SCRIPTS.get(id);
            var decompiled = new SyntaxBuilder(id).build(script.code);
            SCRIPTS_DECOMPILED.put(id, decompiled);
        }

        // propagate types
        if (PROPAGATE_TYPES) {
            var propagation = new TypePropagator();

            for (var id : SCRIPTS_DECOMPILED.keySet()) {
                var script = SCRIPTS_DECOMPILED.get(id);
                propagation.run(id, script);
            }

            propagation.finish(SCRIPTS_DECOMPILED.keySet());
        }
    }

    public static List<String> unpack(int id) {
        var script = SCRIPTS_DECOMPILED.get(id);

        if (script == null) {
            return List.of();
        }

        var name = "[" + (CLIENTSCRIPT.contains(id) ? "clientscript" : "proc") + ",script" + id + "]";

        if (Unpacker.SCRIPT_NAMES.containsKey(id)) {
            name = Unpacker.SCRIPT_NAMES.get(id);
        }

        return CodeFormatter.formatScript(name, SCRIPT_PARAMETERS.get(id), SCRIPT_RETURNS.get(id), script).lines().toList();
    }
}
