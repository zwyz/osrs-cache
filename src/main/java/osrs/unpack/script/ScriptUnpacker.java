package osrs.unpack.script;

import osrs.Unpack;
import osrs.unpack.ScriptTrigger;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static osrs.unpack.script.Command.*;

public class ScriptUnpacker {
    public static final boolean DISASSEMBLE_ONLY = false;
    public static final boolean KEEP_LABELS = false;
    public static final boolean ASSUME_UNKNOWN_TYPES_ARE_BASE = true;
    public static final boolean OUTPUT_TYPE_ALIASES = false;
    public static final boolean ASSUME_UNUSED_IS_CLIENTSCRIPT = true;
    public static final boolean FORMAT_HOOKS = true;
    public static final boolean CHECK_NONEMPTY_STACK = true;
    public static final boolean CHECK_EMPTY_ARGUMENT = true;
    public static final boolean IGNORE_HOOK_TYPE_INFO = false;
    public static final boolean ERROR_ON_TYPE_CONFLICT = true;
    public static final boolean OVERRIDE_SIGNATURES = true;
    public static final Map<Integer, CompiledScript> SCRIPTS = new HashMap<>();
    public static final Map<Integer, List<Expression>> SCRIPTS_DECOMPILED = new HashMap<>();
    public static final Map<Integer, Integer> SCRIPT_PARAMETER_COUNT = new HashMap<>();
    public static final Map<Integer, List<Type>> SCRIPT_RETURN_TYPES = new HashMap<>();
    public static final Map<Integer, List<Type>> SCRIPT_PARAMETERS = new HashMap<>();
    public static final Map<Integer, List<Type>> SCRIPT_RETURNS = new HashMap<>();
    public static final Map<Integer, Integer> SCRIPT_LEGACY_ARRAY_PARAMETER = new HashMap<>();
    public static final Map<Integer, Map<LocalReference, Type>> SCRIPT_LOCALS = new HashMap<>();
    public static final Map<String, ScriptOverride> SCRIPT_OVERRIDES = new HashMap<>();
    public static final Set<Integer> CALLED = new LinkedHashSet<>();
    public static final Map<Integer, ScriptTrigger> SCRIPT_TRIGGERS = new LinkedHashMap<>();

    private static final Pattern OVERRIDE_PATTERN = Pattern.compile("\\[(?<trigger>[a-zA-Z0-9_]+),(?<name>[a-zA-Z0-9_]+)](?:\\((?<arguments>[a-zA-Z0-9_]+\\s+\\$[a-zA-Z0-9_]+(?:\\s*,\\s*[a-zA-Z0-9_]+\\s+\\$[a-zA-Z0-9_]+)*)?\\))?(?:\\((?<returns>[a-zA-Z0-9_]+(?:\\s*, ?\\s*[a-zA-Z0-9_]+)*)?\\))?(?: (?<version>[0-9]+))?");

    static {
        try {
            for (var line : Files.readAllLines(Path.of("data/names/manual/scripts-signatures.txt"))) {
                if (line.isBlank() || line.startsWith("//")) {
                    continue;
                }

                var matcher = OVERRIDE_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalStateException("invalid line " + line);
                }

                if (matcher.group("version") != null) {
                    var version = Integer.parseInt(matcher.group("version"));

                    if (version > Unpack.CLIENTSCRIPTS_VERSION) {
                        continue; // overrides for higher versions
                    }
                }

                var trigger = matcher.group("trigger");
                var name = matcher.group("name");
                var arguments = matcher.group("arguments") == null ? List.<Type>of() : Arrays.stream(matcher.group("arguments").split(",")).map(s -> Type.byNameAlias(s.trim().split(" ")[0])).toList();
                var returns = matcher.group("returns") == null ? List.<Type>of() : Arrays.stream(matcher.group("returns").split(",")).map(s -> Type.byNameAlias(s.trim())).toList();

                var fullName = "[" + trigger + "," + name + "]";
                SCRIPT_OVERRIDES.put(fullName, new ScriptOverride(arguments, returns));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

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
            case ARRAY -> 0;
        };
    }

    public static List<Type> getReturnTypes(int script) {
        return SCRIPT_RETURN_TYPES.get(script);
    }

    public static void decompile() {
        if (DISASSEMBLE_ONLY) {
            return;
        }

        // compute parameter/return counts
        for (var id : SCRIPTS.keySet()) {
            var script = SCRIPTS.get(id);
            SCRIPT_PARAMETER_COUNT.put(id, script.argumentCountInt + script.argumentCountObject);
            var returnTypes = new ArrayList<Type>();

            for (var i = script.code.length - 2; i >= 0; i--) {
                var command = script.code[i].command();

                if (command == PUSH_CONSTANT_INT) {
                    if ((int) script.code[i].operand() == 0) {
                        returnTypes.addFirst(Type.INT);
                    } else {
                        returnTypes.addFirst(Type.UNKNOWN_INT_NOTINT);
                    }
                } else if (command == PUSH_CONSTANT_STRING) {
                    returnTypes.addFirst(Type.STRING);
                } else if (command == PUSH_CONSTANT_NULL) {
                    returnTypes.addFirst(Type.UNKNOWNARRAY);
                } else {
                    break;
                }
            }

            SCRIPT_RETURN_TYPES.put(id, returnTypes);
        }

        // decompile
        for (var id : SCRIPTS.keySet()) {
            var script = SCRIPTS.get(id);
            var decompiled = new SyntaxBuilder(id).build(script.code);
            SCRIPTS_DECOMPILED.put(id, decompiled);
        }

        // propagate types
        if (Unpack.VERSION < 231) {
            new LegacyArrayParameterInference().run(SCRIPTS_DECOMPILED.keySet());
        }

        var propagator = new TypePropagator();

        for (var id : SCRIPTS_DECOMPILED.keySet()) {
            var script = SCRIPTS_DECOMPILED.get(id);
            propagator.run(id, script);
        }

        Unpacker.IF_TYPES
                .entrySet()
                .stream()
                .flatMap(e -> e.getValue().values().stream())
                .flatMap(ifType -> ifType.hooks().stream())
                .forEach(propagator::visitHook);

        propagator.finish(SCRIPTS_DECOMPILED.keySet());
        var triggerInference = new TriggerInference();

        for (var id : SCRIPTS_DECOMPILED.keySet()) {
            var script = SCRIPTS_DECOMPILED.get(id);
            triggerInference.run(id, script);
        }
    }

    public static List<String> unpack(int id) {
        var script = SCRIPTS_DECOMPILED.get(id);

        if (script == null) {
            return List.of();
        }

        return CodeFormatter.formatScript(Unpacker.getScriptName(id), SCRIPT_PARAMETERS.get(id), SCRIPT_RETURNS.get(id), SCRIPT_LOCALS.get(id), script).lines().toList();
    }

    public static Type chooseDisplayType(Type type) {
        if (ASSUME_UNKNOWN_TYPES_ARE_BASE) {
            if (type == Type.UNKNOWN_INT) return Type.INT_INT; // todo: could assume boolean
            if (type == Type.UNKNOWN_INT_NOTINT) return Type.BOOLEAN;
            if (type == Type.UNKNOWN_INT_NOTINT_NOTBOOLEAN) return Type.INT_INT; // todo: can format this specially
            if (type == Type.UNKNOWN_INT_NOTBOOLEAN) return Type.INT_INT;
            if (type == Type.UNKNOWN_OBJECT) return Type.STRING;
            if (type == Type.INT) return Type.INT_INT;
            if (type == Type.UNKNOWN_INTARRAY) return Type.INTARRAY;
            if (type == Type.UNKNOWNARRAY) return Type.INTARRAY;
        }

        return type;
    }
}
