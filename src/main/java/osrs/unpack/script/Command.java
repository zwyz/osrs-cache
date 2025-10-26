package osrs.unpack.script;

import osrs.Unpack;
import osrs.unpack.Type;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class Command {
    private static final Map<Integer, Command> BY_ID = new HashMap<>();
    private static final Map<String, Command> BY_NAME = new HashMap<>();
    public final String name;
    public final List<Type> arguments;
    public final List<Type> returns;

    Command(String name, List<Type> arguments, List<Type> returns) {
        this.name = name;
        this.arguments = arguments;
        this.returns = returns;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Command byId(int id) {
        return Objects.requireNonNull(BY_ID.get(id));
    }

    private static Command defineCommand(String name) {
        var command = new Command(name, null, null);
        BY_NAME.put(name, command);
        return command;
    }

    private static Command defineCommand(String name, int id, List<Type> arguments, List<Type> returns) {
        var command = new Command(name, arguments, returns);
        BY_NAME.put(name, command);
        BY_ID.put(id, command);
        return command;
    }

    private static Command findCommand(String name) {
        return Objects.requireNonNull(BY_NAME.get(name));
    }

    // custom decompiler commands
    public static final Command LABEL = defineCommand("label");
    public static final Command BRANCHIF = defineCommand("branchif");
    public static final Command FLOW_ASSIGN = defineCommand("flow_assign"); // ..., ..., ... = ..., ..., ...
    public static final Command FLOW_LOAD = defineCommand("flow_load"); // ..., ..., ... = ..., ..., ...
    public static final Command FLOW_IF = defineCommand("flow_if"); // if (...) ...
    public static final Command FLOW_IFELSE = defineCommand("flow_ifelse"); // if (...) ... else ...
    public static final Command FLOW_WHILE = defineCommand("flow_while"); // while (...) ...
    public static final Command FLOW_SWITCH = defineCommand("flow_switch"); // switch (...) ...
    public static final Command FLOW_AND = defineCommand("flow_and"); // ... & ...
    public static final Command FLOW_OR = defineCommand("flow_or"); // ... | ...
    public static final Command FLOW_NE = defineCommand("flow_ne"); // ... != ...
    public static final Command FLOW_EQ = defineCommand("flow_eq"); // ... == ...
    public static final Command FLOW_LT = defineCommand("flow_lt"); // ... < ...
    public static final Command FLOW_GT = defineCommand("flow_gt"); // ... > ...
    public static final Command FLOW_LE = defineCommand("flow_le"); // ... <= ...
    public static final Command FLOW_GE = defineCommand("flow_ge"); // ... >= ...
    public static final Command FLOW_PREINC = defineCommand("flow_preinc"); // ++$x
    public static final Command FLOW_PREDEC = defineCommand("flow_predec"); // --$x
    public static final Command FLOW_POSTINC = defineCommand("flow_postinc"); // $x++
    public static final Command FLOW_POSTDEC = defineCommand("flow_postdec"); // $x--

    // core commands
    public static Command PUSH_CONSTANT_INT;
    public static Command PUSH_VAR;
    public static Command POP_VAR;
    public static Command PUSH_CONSTANT_STRING;
    public static Command BRANCH;
    public static Command BRANCH_NOT;
    public static Command BRANCH_EQUALS;
    public static Command BRANCH_LESS_THAN;
    public static Command BRANCH_GREATER_THAN;
    public static Command RETURN;
    public static Command PUSH_VARBIT;
    public static Command POP_VARBIT;
    public static Command BRANCH_LESS_THAN_OR_EQUALS;
    public static Command BRANCH_GREATER_THAN_OR_EQUALS;
    public static Command PUSH_INT_LOCAL;
    public static Command POP_INT_LOCAL;
    public static Command PUSH_STRING_LOCAL;
    public static Command POP_STRING_LOCAL;
    public static Command JOIN_STRING;
    public static Command POP_INT_DISCARD;
    public static Command POP_STRING_DISCARD;
    public static Command GOSUB_WITH_PARAMS;
    public static Command PUSH_VARC_INT;
    public static Command POP_VARC_INT;
    public static Command DEFINE_ARRAY;
    public static Command PUSH_ARRAY_INT;
    public static Command POP_ARRAY_INT;
    public static Command PUSH_VARC_STRING_OLD;
    public static Command POP_VARC_STRING_OLD;
    public static Command PUSH_VARC_STRING;
    public static Command POP_VARC_STRING;
    public static Command SWITCH;
    public static Command PUSH_CONSTANT_NULL;
    public static Command PUSH_VARCLANSETTING;
    public static Command PUSH_VARCLAN;

    // commands with special behavior
    public static Command ADD;
    public static Command SUB;

    public static Command ENUM;
    public static Command ENUM_STRING;

    public static Command LC_PARAM;
    public static Command NC_PARAM;
    public static Command OC_PARAM;
    public static Command STRUCT_PARAM;

    public static Command DB_FIND;
    public static Command DB_FIND_WITH_COUNT;
    public static Command DB_FIND_REFINE;
    public static Command DB_FIND_REFINE_WITH_COUNT;
    public static Command DB_GETFIELD;
    public static Command IF_FIND_CHILD;
    public static Command IF_RUNSCRIPT;
    public static Command CC_PARAM;
    public static Command CC_SETPARAM;
    public static Command IF_PARAM;
    public static Command IF_SETPARAM;

    public static Command ARRAY_COMPARE;
    public static Command ARRAY_INDEXOF;
    public static Command ARRAY_LASTINDEXOF;
    public static Command ARRAY_COUNT;
    public static Command ARRAY_MIN;
    public static Command ARRAY_MAX;
    public static Command ARRAY_FILL;
    public static Command ARRAY_COPY;
    public static Command ENUM_GETINPUTS;
    public static Command ENUM_GETOUTPUTS;
    public static Command ARRAY_CREATE;
    public static Command ARRAY_PUSH;
    public static Command ARRAY_PUSHALL;
    public static Command ARRAY_INSERT;
    public static Command ARRAY_INSERTALL;
    public static Command ARRAY_DELETE;

    // load commands
    private static final Pattern COMMAND_PATTERN = Pattern.compile("(?<opcode>\\d+) \\[command,(?<name>[a-zA-Z0-9_]+)](?:\\((?<arguments>[a-zA-Z0-9_]+\\s+\\$[a-zA-Z0-9_]+(?:\\s*,\\s*[a-zA-Z0-9_]+\\s+\\$[a-zA-Z0-9_]+)*)?\\))?(?:\\((?<returns>[a-zA-Z0-9_]+(?:\\s*, ?\\s*[a-zA-Z0-9_]+)*)?\\))?(?: (?<version>[0-9]+))?");

    public static void reset() {
        BY_ID.clear();
        BY_NAME.clear();

        try {
            for (var file : Files.list(Path.of("data/commands")).toList()) {
                for (var line : Files.readAllLines(file)) {
                    if (line.isBlank() || line.startsWith("//")) {
                        continue;
                    }

                    var matcher = COMMAND_PATTERN.matcher(line);
                    var missingTypes = false;

                    if (line.contains("(gap)")) {
                        continue;
                    }

                    if (!matcher.matches()) {
                        matcher = COMMAND_PATTERN.matcher(line.substring(0, line.lastIndexOf(']') + 1));
                        missingTypes = true;

                        if (!matcher.matches()) {
                            throw new IllegalStateException("invalid line " + line);
                        }
                    }

                    if (matcher.group("version") != null) {
                        var version = Integer.parseInt(matcher.group("version"));

                        if (version > Unpack.VERSION) {
                            continue; // overrides for higher versions
                        }
                    }

                    var name = matcher.group("name");
                    var id = Integer.parseInt(matcher.group("opcode"));

                    if (missingTypes) {
                        defineCommand(name, id, null, null);
                    } else {
                        var arguments = matcher.group("arguments") == null ? List.<Type>of() : Arrays.stream(matcher.group("arguments").split(",")).map(s -> Type.byName(s.trim().split(" ")[0])).toList();
                        var returns = matcher.group("returns") == null ? List.<Type>of() : Arrays.stream(matcher.group("returns").split(",")).map(s -> Type.byName(s.trim())).toList();
                        defineCommand(name, id, arguments, returns);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        PUSH_CONSTANT_INT = findCommand("push_constant_int");
        PUSH_VAR = findCommand("push_var");
        POP_VAR = findCommand("pop_var");
        PUSH_CONSTANT_STRING = findCommand("push_constant_string");
        BRANCH = findCommand("branch");
        BRANCH_NOT = findCommand("branch_not");
        BRANCH_EQUALS = findCommand("branch_equals");
        BRANCH_LESS_THAN = findCommand("branch_less_than");
        BRANCH_GREATER_THAN = findCommand("branch_greater_than");
        RETURN = findCommand("return");
        PUSH_VARBIT = findCommand("push_varbit");
        POP_VARBIT = findCommand("pop_varbit");
        BRANCH_LESS_THAN_OR_EQUALS = findCommand("branch_less_than_or_equals");
        BRANCH_GREATER_THAN_OR_EQUALS = findCommand("branch_greater_than_or_equals");
        PUSH_INT_LOCAL = findCommand("push_int_local");
        POP_INT_LOCAL = findCommand("pop_int_local");
        PUSH_STRING_LOCAL = findCommand("push_string_local");
        POP_STRING_LOCAL = findCommand("pop_string_local");
        JOIN_STRING = findCommand("join_string");
        POP_INT_DISCARD = findCommand("pop_int_discard");
        POP_STRING_DISCARD = findCommand("pop_string_discard");
        GOSUB_WITH_PARAMS = findCommand("gosub_with_params");
        PUSH_VARC_INT = findCommand("push_varc_int");
        POP_VARC_INT = findCommand("pop_varc_int");
        DEFINE_ARRAY = findCommand("define_array");
        PUSH_ARRAY_INT = findCommand("push_array_int");
        POP_ARRAY_INT = findCommand("pop_array_int");
        PUSH_VARC_STRING_OLD = findCommand("push_varc_string_old");
        POP_VARC_STRING_OLD = findCommand("pop_varc_string_old");
        PUSH_VARC_STRING = findCommand("push_varc_string");
        POP_VARC_STRING = findCommand("pop_varc_string");
        SWITCH = findCommand("switch");
        PUSH_CONSTANT_NULL = findCommand("push_constant_null");
        PUSH_VARCLANSETTING = findCommand("push_varclansetting");
        PUSH_VARCLAN = findCommand("push_varclan");

        // commands with special behavior
        ADD = findCommand("add");
        SUB = findCommand("sub");

        ENUM = findCommand("enum");
        ENUM_STRING = findCommand("enum_string");

        LC_PARAM = findCommand("lc_param");
        NC_PARAM = findCommand("nc_param");
        OC_PARAM = findCommand("oc_param");
        STRUCT_PARAM = findCommand("struct_param");

        DB_FIND = findCommand("db_find");
        DB_FIND_WITH_COUNT = findCommand("db_find_with_count");
        DB_FIND_REFINE = findCommand("db_find_refine");
        DB_FIND_REFINE_WITH_COUNT = findCommand("db_find_refine_with_count");
        DB_GETFIELD = findCommand("db_getfield");
        IF_FIND_CHILD = findCommand("if_find_child");
        IF_RUNSCRIPT = findCommand("if_runscript");
        CC_PARAM = findCommand("cc_param");
        CC_SETPARAM = findCommand("cc_setparam");
        IF_PARAM = findCommand("if_param");
        IF_SETPARAM = findCommand("if_setparam");

        ARRAY_COMPARE = findCommand("array_compare");
        ARRAY_INDEXOF = findCommand("array_indexof");
        ARRAY_LASTINDEXOF = findCommand("array_lastindexof");
        ARRAY_COUNT = findCommand("array_count");
        ARRAY_MIN = findCommand("array_min");
        ARRAY_MAX = findCommand("array_max");
        ARRAY_FILL = findCommand("array_fill");
        ARRAY_COPY = findCommand("array_copy");
        ENUM_GETINPUTS = findCommand("enum_getinputs");
        ENUM_GETOUTPUTS = findCommand("enum_getoutputs");
        ARRAY_CREATE = findCommand("array_create");
        ARRAY_PUSH = findCommand("array_push");
        ARRAY_PUSHALL = findCommand("array_pushall");
        ARRAY_INSERT = findCommand("array_insert");
        ARRAY_INSERTALL = findCommand("array_insertall");
        ARRAY_DELETE = findCommand("array_delete");
    }

    public boolean hasHook() {
        return name.contains("_seton");
    }

    public record Instruction(Command command, Object operand) {
        @Override
        public String toString() {
            return command + " " + operand;
        }
    }

    // switch
    public record SwitchCase(int value, int target) {

    }

    // branchif
    public record BranchIfTarget(int a, int b) {

    }

    // flow_ifelse
    public record IfElseBranches(List<Expression> trueBranch, List<Expression> falseBranche) {

    }

    // flow_switch
    public record SwitchBranch(List<Integer> values, List<Expression> branch) {

    }

    // flow_assign, flow_load
    public record VarPlayerReference(int var) {

    }

    public record VarPlayerBitReference(int var) {

    }

    // todo: refactor pops handling to get rid of string boolean
    public record VarClientReference(int var, boolean string) {

    }

    public record VarClientStringReference(int var) {

    }

    public record VarClanSettingReference(int var) {

    }

    public record VarClanReference(int var) {

    }

    public record LocalReference(LocalDomain domain, int local) {

    }

    public enum LocalDomain {
        INTEGER, STRING, ARRAY
    }
}
