package osrs.unpack.script;

import osrs.Unpack;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static osrs.unpack.script.Command.*;

// converts ast to code
public class CodeFormatter {
    private static final Pattern DIRECT_STRING_PATTERN = Pattern.compile("[a-z_0-9]+");
    private static Map<LocalReference, Type> localTypes;

    public static String formatScript(String name, List<Type> parameterTypes, List<Type> returnTypes, Map<LocalReference, Type> localTypes, List<Expression> script) {
        CodeFormatter.localTypes = localTypes;
        var parameters = new ArrayList<String>();
        var returns = new ArrayList<String>();
        var indexInt = 0;
        var indexObject = 0;
        var declaredLocals = new HashSet<LocalReference>();

        for (var type : parameterTypes) {
            if (Unpack.VERSION < 231 && Type.LATTICE.test(type, Type.UNKNOWNARRAY)) {
                declaredLocals.add(new LocalReference(LocalDomain.ARRAY, 0));
                parameters.add(formatType(type, true) + " " + formatLocal(new LocalReference(LocalDomain.ARRAY, 0)));
                indexInt++;
            } else if (Type.LATTICE.test(type, Type.UNKNOWN_INT)) {
                declaredLocals.add(new LocalReference(LocalDomain.INTEGER, indexInt));
                parameters.add(formatType(type, true) + " " + formatLocal(new LocalReference(LocalDomain.INTEGER, indexInt++)));
            } else if (Type.LATTICE.test(type, Type.UNKNOWN_OBJECT)) {
                declaredLocals.add(new LocalReference(LocalDomain.STRING, indexObject));
                parameters.add(formatType(type, true) + " " + formatLocal(new LocalReference(LocalDomain.STRING, indexObject++)));
            } else {
                throw new IllegalStateException("unknown parameter local");
            }
        }

        for (var type : returnTypes) {
            returns.add(formatType(type, true));
        }

        var header = name;

        if (!parameters.isEmpty() || !returns.isEmpty()) {
            header += "(" + String.join(", ", parameters) + ")";
        }

        if (!returns.isEmpty()) {
            header += "(" + String.join(", ", returns) + ")";
        }

        if (script.getLast().command == RETURN) {
            script = script.subList(0, script.size() - 1);
        } else {
            throw new IllegalStateException("script does not have default return");
        }

        return header + "\n" + formatBlock(script, 0, declaredLocals);
    }

    public static String formatBlock(List<Expression> statements, int indent, Set<LocalReference> declaredLocals) {
        var result = "";

        for (var statement : statements) {
            result += format(statement, 0, indent, declaredLocals);

            if (statement.command != FLOW_IF && statement.command != FLOW_IFELSE && statement.command != FLOW_WHILE && statement.command != FLOW_SWITCH) {
                result += ";";
            }

            result += "\n";
        }

        return result;
    }

    public static String format(Expression expression) {
        return format(expression, 0, 0, null);
    }

    static String format(Expression expression, int prec, int indent, Set<LocalReference> declaredLocals) {
        return " ".repeat(indent) + formatNoIndent(expression, prec, indent, declaredLocals);
    }

    private static String formatNoIndent(Expression expression, int prec, int indent, Set<LocalReference> declaredLocals) {
        var command = expression.command;

        if (command == PUSH_CONSTANT_INT) {
            return formatConstant(expression.type.get(0), expression.operand);
        } else if (command == PUSH_CONSTANT_STRING) {
            return formatConstant(expression.type.get(0), expression.operand);
        } else if (command == PUSH_CONSTANT_NULL) {
            return "null";
        } else if (command == FLOW_ASSIGN) {
            var targets = (List<Object>) expression.operand;

            if (targets.stream().allMatch(Objects::isNull)) {
                return expression.arguments.stream().map(CodeFormatter::format).collect(Collectors.joining(", "));
            } else {
                var left = new ArrayList<String>();

                for (var target : targets) {
                    switch (target) {
                        case LocalReference local -> {
                            if (declaredLocals != null && declaredLocals.add(local)) {
                                left.add("def_" + formatLocalType(local, true) + " " + formatLocal(local));
                            } else {
                                left.add(formatLocal(local));
                            }
                        }

                        case VarPlayerReference var -> left.add(formatVarPlayer(var));
                        case VarPlayerBitReference var -> left.add(formatVarPlayerBit(var));
                        case VarClientReference var -> left.add(formatVarClient(var));
                        case VarClientStringReference var -> left.add(formatVarClientString(var));
                        case VarClanSettingReference var -> left.add(formatVarClanSetting(var));
                        case VarClanReference var -> left.add(formatVarClan(var));
                        case null -> left.add("$_");
                        default -> throw new IllegalStateException("invalid assign target type");
                    }
                }

                return String.join(", ", left) + " = " + expression.arguments.stream().map(CodeFormatter::format).collect(Collectors.joining(", "));
            }
        } else if (command == FLOW_LOAD) {
            return formatLoadTarget(expression.operand);
        } else if (command == FLOW_PREINC) {
            return "++" + formatLoadTarget(expression.operand);
        } else if (command == FLOW_PREDEC) {
            return "--" + formatLoadTarget(expression.operand);
        } else if (command == FLOW_POSTINC) {
            return formatLoadTarget(expression.operand) + "++";
        } else if (command == FLOW_POSTDEC) {
            return formatLoadTarget(expression.operand) + "--";
        } else if (command == GOSUB_WITH_PARAMS) {
            var script = formatConstant(Type.CLIENTSCRIPT, expression.operand);

            if (expression.arguments.isEmpty()) {
                return "~" + script;
            } else {
                return "~" + script + "(" + expression.arguments.stream().map(CodeFormatter::format).collect(Collectors.joining(", ")) + ")";
            }
        } else if (command == DEFINE_ARRAY) {
            var index = (int) expression.operand >> 16;
            var type = Type.byChar((int) expression.operand & 0xffff);
            var local = new LocalReference(Unpack.VERSION < 231 ? LocalDomain.ARRAY : LocalDomain.STRING, index);
            return "def_" + formatType(type, true) + " " + formatLocal(local) + "(" + format(expression.arguments.get(0)) + ")";
        } else if (command == PUSH_ARRAY_INT) {
            var index = (int) expression.operand;
            var local = new LocalReference(Unpack.VERSION < 231 ? LocalDomain.ARRAY : LocalDomain.STRING, index);
            return formatLocal(local) + "(" + format(expression.arguments.get(0)) + ")";
        } else if (command == POP_ARRAY_INT && expression.arguments.size() == 2) {
            var index = (int) expression.operand;
            var local = new LocalReference(Unpack.VERSION < 231 ? LocalDomain.ARRAY : LocalDomain.STRING, index);
            return formatLocal(local) + "(" + format(expression.arguments.get(0)) + ") = " + format(expression.arguments.get(1));
        } else if (command == OR && expression.arguments.size() == 2) {
            var s = formatBinary(prec, 50, " | ", expression.arguments.get(0), expression.arguments.get(1));
            return prec < 50 ? "calc(" + s + ")" : s;
        } else if (command == AND && expression.arguments.size() == 2) {
            var s = formatBinary(prec, 60, " & ", expression.arguments.get(0), expression.arguments.get(1));
            return prec < 50 ? "calc(" + s + ")" : s;
        } else if (command == ADD && expression.arguments.size() == 2) {
            var s = formatBinary(prec, 70, " + ", expression.arguments.get(0), expression.arguments.get(1));
            return prec < 50 ? "calc(" + s + ")" : s;
        } else if (command == SUB && expression.arguments.size() == 2) {
            var s = formatBinary(prec, 70, " - ", expression.arguments.get(0), expression.arguments.get(1));
            return prec < 50 ? "calc(" + s + ")" : s;
        } else if (command == MULTIPLY && expression.arguments.size() == 2) {
            var s = formatBinary(prec, 80, " * ", expression.arguments.get(0), expression.arguments.get(1));
            return prec < 50 ? "calc(" + s + ")" : s;
        } else if (command == DIVIDE && expression.arguments.size() == 2) {
            var s = formatBinary(prec, 80, " / ", expression.arguments.get(0), expression.arguments.get(1));
            return prec < 50 ? "calc(" + s + ")" : s;
        } else if (command == MODULO && expression.arguments.size() == 2) {
            var s = formatBinary(prec, 80, " % ", expression.arguments.get(0), expression.arguments.get(1));
            return prec < 50 ? "calc(" + s + ")" : s;
        } else if (command == JOIN_STRING) {
            var result = "";
            var interpolations = new HashSet<Integer>();

            for (int i = 0; i < expression.arguments.size(); i++) {
                var arg = expression.arguments.get(i);

                if (arg.command == PUSH_CONSTANT_STRING && arg.operand instanceof String s) {
                    if (s.startsWith("<") && s.endsWith(">")) {
                        interpolations.add(i);
                    } else if (i > 0 && !interpolations.contains(i - 1)) {
                        var last = (String) expression.arguments.get(i - 1).operand;
                        var lastSpaced = last.startsWith(" ") || last.endsWith(" ") || last.startsWith(". ") || last.startsWith(", ") || last.startsWith(": ");
                        var currentSpaced = s.startsWith(" ") || s.endsWith(" ") || s.startsWith(". ") || s.startsWith(", ") || s.startsWith(": ");

                        if (!lastSpaced && currentSpaced) {
                            interpolations.add(i - 1);
                        } else {
                            interpolations.add(i);
                        }
                    }
                } else {
                    interpolations.add(i);
                }
            }

            for (int i = 0; i < expression.arguments.size(); i++) {
                var arg = expression.arguments.get(i);

                if (arg.command == PUSH_CONSTANT_STRING && arg.operand instanceof String s) {
                    if (!interpolations.contains(i)) {
                        result += escape(s);
                    } else if (s.startsWith("<") && s.endsWith(">")) {
                        result += s;
                    } else {
                        if (DIRECT_STRING_PATTERN.matcher(s).matches()) {
                            result += "<" + s + ">";
                        } else {
                            result += "<\"" + s + "\">";
                        }
                    }
                } else {
                    result += "<" + format(arg) + ">";
                }
            }

            return "\"" + result + "\"";
        } else if (command == CC_CREATE) {
            var args = expression.arguments;
            var dot = expression.operand instanceof Integer i && i == 1;

            if (Unpack.VERSION >= 230 && expression.arguments.getLast().command == PUSH_CONSTANT_INT && (int) expression.arguments.getLast().operand == 0) {
                args = args.subList(0, args.size() - 1);
            }

            return (dot ? "." : "") + "cc_create(" + args.stream().map(CodeFormatter::format).collect(Collectors.joining(", ")) + ")";
        } else if (command == IF_RUNSCRIPT) {
            var dot = expression.operand instanceof Integer i && i == 1;
            var args1 = expression.arguments.subList(0, 3);
            var args2 = expression.arguments.subList(3, expression.arguments.size() - 1);
            return (dot ? "." : "") + "if_runscript*(" + args1.stream().map(CodeFormatter::format).collect(Collectors.joining(", ")) + ")(" + args2.stream().map(CodeFormatter::format).collect(Collectors.joining(", ")) + ")";
        } else if (command == FLOW_NE) {
            return formatBinary(prec, 40, " ! ", expression.arguments.get(0), expression.arguments.get(1));
        } else if (command == FLOW_EQ) {
            return formatBinary(prec, 40, " = ", expression.arguments.get(0), expression.arguments.get(1));
        } else if (command == FLOW_LT) {
            return formatBinary(prec, 40, " < ", expression.arguments.get(0), expression.arguments.get(1));
        } else if (command == FLOW_GT) {
            return formatBinary(prec, 40, " > ", expression.arguments.get(0), expression.arguments.get(1));
        } else if (command == FLOW_LE) {
            return formatBinary(prec, 40, " <= ", expression.arguments.get(0), expression.arguments.get(1));
        } else if (command == FLOW_GE) {
            return formatBinary(prec, 40, " >= ", expression.arguments.get(0), expression.arguments.get(1));
        } else if (command == FLOW_AND) {
            return formatBinary(prec, 20, " & ", expression.arguments.get(0), expression.arguments.get(1));
        } else if (command == FLOW_OR) {
            return formatBinary(prec, 10, " | ", expression.arguments.get(0), expression.arguments.get(1));
        } else if (command == FLOW_IF) {
            return "if (" + format(expression.arguments.get(0)) + ") {\n" + formatBlock((List<Expression>) expression.operand, indent + 4, declaredLocals) + " ".repeat(indent) + "}";
        } else if (command == FLOW_IFELSE) {
            var trueBranch = ((IfElseBranches) expression.operand).trueBranch();
            var falseBranch = ((IfElseBranches) expression.operand).falseBranche();

            if (falseBranch.size() == 1 && (falseBranch.get(0).command == FLOW_IF || falseBranch.get(0).command == FLOW_IFELSE)) {
                return "if (" + format(expression.arguments.get(0)) + ") {\n" + formatBlock(trueBranch, indent + 4, declaredLocals) + " ".repeat(indent) + "} else " + format(falseBranch.get(0), 0, indent, declaredLocals).substring(indent);
            } else {
                return "if (" + format(expression.arguments.get(0)) + ") {\n" + formatBlock(trueBranch, indent + 4, declaredLocals) + " ".repeat(indent) + "} else {\n" + formatBlock(falseBranch, indent + 4, declaredLocals) + " ".repeat(indent) + "}";
            }
        } else if (command == FLOW_WHILE) {
            return "while (" + format(expression.arguments.get(0)) + ") {\n" + formatBlock((List<Expression>) expression.operand, indent + 4, declaredLocals) + " ".repeat(indent) + "}";
        } else if (command == FLOW_SWITCH) {
            var type = expression.arguments.get(0).type.get(0);
            var result = "switch_" + formatType(type, true) + " (" + expression.arguments.get(0) + ") {\n";

            for (var branch : (List<SwitchBranch>) expression.operand) {
                if (branch.values() == null) {
                    result += " ".repeat(indent + 4) + "case default :\n";
                } else {
                    result += " ".repeat(indent + 4) + "case " + String.join(", ", branch.values().stream().map(value -> formatConstant(type, value)).toList()) + " :\n";
                }

                result += formatBlock(branch.branch(), indent + 8, declaredLocals);
            }

            result += " ".repeat(indent) + "}";
            return result;
        } else if (command == LABEL) {
            return "label(" + expression.operand + ")";
        } else if (command == BRANCHIF) {
            return "branchif(" + format(expression.arguments.get(0)) + ", " + ((BranchIfTarget) expression.operand).a() + ", " + ((BranchIfTarget) expression.operand).b() + ")";
        } else if (command == BRANCH) {
            return "branch(" + expression.operand + ")"; // debug output only
        } else if (command == BRANCH_EQUALS) {
            return "branch_equals(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")"; // debug output only
        } else if (command == BRANCH_LESS_THAN) {
            return "branch_less_than(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")"; // debug output only
        } else if (command == BRANCH_GREATER_THAN) {
            return "branch_greater_than(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")"; // debug output only
        } else if (command == BRANCH_LESS_THAN_OR_EQUALS) {
            return "branch_less_than_or_equals(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")"; // debug output only
        } else if (command == BRANCH_GREATER_THAN_OR_EQUALS) {
            return "branch_greater_than_or_equals(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")"; // debug output only
        } else if (command == SWITCH) {
            var table = new ArrayList<String>();

            for (var branch : (List<SwitchCase>) expression.operand) {
                table.add(branch.value() + " => " + branch.target());
            }

            return "switch(" + expression.arguments.get(0) + ", " + String.join(", ", table) + ")";
        } else {
            var dot = expression.operand instanceof Integer i && i == 1;
            var operand = Objects.equals(expression.operand, 0) || Objects.equals(expression.operand, 1) ? "" : "[" + expression.operand + "]";

            if (expression.arguments.isEmpty()) {
                return (dot ? "." : "") + command.name + operand;
            } else {
                var arguments = expression.arguments.stream()
                        .filter(arg -> arg.type.get(0) != Type.BASEVARTYPE) // these are auto-generated to let the client know which stack to pop from
                        .map(CodeFormatter::format)
                        .collect(Collectors.joining(", "));

                if (ScriptUnpacker.FORMAT_HOOKS && command.hasHook()) {
                    var hookStart = 0;
                    var hookEnd = expression.arguments.size() - (command.arguments.size() - 2);
                    arguments = formatHook(expression.arguments.subList(hookStart, hookEnd));

                    if (expression.arguments.size() != hookEnd) {
                        arguments += ", " + expression.arguments.subList(hookEnd, expression.arguments.size()).stream().map(CodeFormatter::format).collect(Collectors.joining(", "));
                    }
                }

                return (dot ? "." : "") + command.name + operand + "(" + arguments + ")";
            }
        }
    }

    private static String formatLoadTarget(Object operand) {
        return switch (operand) {
            case LocalReference local -> formatLocal(local);
            case VarPlayerReference var -> formatVarPlayer(var);
            case VarPlayerBitReference var -> formatVarPlayerBit(var);
            case VarClientReference var -> formatVarClient(var);
            case VarClientStringReference var -> formatVarClientString(var);
            case VarClanSettingReference var -> formatVarClanSetting(var);
            case VarClanReference var -> formatVarClan(var);
            default -> throw new IllegalStateException("invalid load target type");
        };
    }

    private static String formatHook(List<Expression> arguments) {
        var script = arguments.get(0);
        var signature = (String) arguments.get(arguments.size() - 1).operand;

        if ((int) script.operand == -1) {
            return "null";
        }

        if (!signature.endsWith("Y")) {
            var args = arguments.subList(1, arguments.size() - 1);
            var result = format(script);

            if (!args.isEmpty()) {
                result += "(" + args.stream().map(CodeFormatter::formatHookArgument).collect(Collectors.joining(", ")) + ")";
            }

            return "\"" + escape(result) + "\"";
        } else {
            var transmitListCount = (int) arguments.get(arguments.size() - 2).operand;
            var args = arguments.subList(1, arguments.size() - 2 - transmitListCount);
            var transmits = arguments.subList(arguments.size() - 2 - transmitListCount, arguments.size() - 2);
            var result = format(script);

            if (!args.isEmpty()) {
                result += "(" + args.stream().map(CodeFormatter::formatHookArgument).collect(Collectors.joining(", ")) + ")";
            }

            if (!transmits.isEmpty()) {
                result += "{" + transmits.stream().map(CodeFormatter::format).collect(Collectors.joining(", ")) + "}";
            }

            return "\"" + escape(result) + "\"";
        }
    }

    private static String formatHookArgument(Expression expression) {
        if (expression.command == PUSH_CONSTANT_INT) {
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 1)) return "event_mousex";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 2)) return "event_mousey";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 3)) return "event_com";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 4)) return "event_op";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 5)) return "event_comsubid";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 6)) return "event_com2";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 7)) return "event_comsubid2";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 8)) return "event_keycode";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 9)) return "event_keychar";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 10)) return "event_subop";
        }

        if (expression.command == PUSH_CONSTANT_STRING) {
            if (Objects.equals(expression.operand, "event_opbase")) return "event_opbase";
        }

        return format(expression);
    }

    private static String formatBinary(int currentPrec, int prec, String operator, Expression left, Expression right) {
        var result = format(left, prec, 0, null) + operator + format(right, prec + 1, 0, null); // left-associative

        if (currentPrec > prec) {
            result = "(" + result + ")";
        }

        return result;
    }

    private static String formatConstant(Type type, Object value) {
        type = ScriptUnpacker.chooseDisplayType(type);

        if (value instanceof String s) {
            if (Objects.equals(s, "null")) {
                return null;
            }

            return "\"" + escape(s) + "\"";
        }

        if (Unpack.VERSION < 231 && type.element() != null) {
            return type.name + value;
        }

        if (value instanceof Integer i) return Unpacker.format(type, i);
        if (value instanceof String s) return s;
        throw new IllegalStateException("invalid constant");
    }

    private static String formatType(Type type, boolean real) {
        type = ScriptUnpacker.chooseDisplayType(type);

        if (type.alias != null && real && !ScriptUnpacker.OUTPUT_TYPE_ALIASES) {
            type = type.alias;
        }

        return type.name;
    }

    private static String formatLocal(LocalReference local) {
        return "$" + formatLocalType(local, false) + local.local();
    }

    private static String formatLocalType(LocalReference local, boolean real) {
        return formatType(localTypes == null ? Type.UNKNOWN : localTypes.getOrDefault(local, Type.UNKNOWN), real);
    }

    private static String formatVarPlayer(VarPlayerReference var) {
        return "%" + Unpacker.format(Type.VAR_PLAYER, var.var());
    }

    private static String formatVarPlayerBit(VarPlayerBitReference var) {
        return "%" + Unpacker.format(Type.VAR_PLAYER_BIT, var.var());
    }

    private static String formatVarClient(VarClientReference var) {
        return "%" + Unpacker.format(Type.VAR_CLIENT, var.var());
    }

    private static String formatVarClientString(VarClientStringReference var) {
        return "%" + Unpacker.format(Type.VAR_CLIENT_STRING, var.var());
    }

    private static String formatVarClanSetting(VarClanSettingReference var) {
        return "%" + Unpacker.format(Type.VAR_CLAN_SETTING, var.var());
    }

    private static String formatVarClan(VarClanReference var) {
        return "%" + Unpacker.format(Type.VAR_CLAN, var.var());
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
