package osrs.unpack.script;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static osrs.unpack.script.Command.*;

// converts ast to code
public class CodeFormatter {
    private static final Pattern DIRECT_STRING_PATTERN = Pattern.compile("[a-z_0-9]+");

    public static String formatScript(String name, List<Type> parameterTypes, List<Type> returnTypes, List<Expression> script) {
        var parameters = new ArrayList<String>();
        var returns = new ArrayList<String>();
        var indexInt = 0;
        var indexObject = 0;
        var declaredLocals = new HashSet<LocalReference>();

        for (var type : parameterTypes) {
            if (type == Type.INTARRAY || type == Type.COMPONENTARRAY) {
                declaredLocals.add(new LocalReference(LocalDomain.INTEGER, 0));
                parameters.add(formatType(type, true) + " " + formatLocal(new LocalReference(LocalDomain.INTEGER, 0), type));
                indexInt++;
            } else if (Type.subtype(type, Type.UNKNOWN_INT)) {
                declaredLocals.add(new LocalReference(LocalDomain.INTEGER, indexInt));
                parameters.add(formatType(type, true) + " " + formatLocal(new LocalReference(LocalDomain.INTEGER, indexInt++), type));
            } else if (type == Type.STRING) {
                declaredLocals.add(new LocalReference(LocalDomain.STRING, indexObject));
                parameters.add(formatType(type, true) + " " + formatLocal(new LocalReference(LocalDomain.STRING, indexObject++), type));
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
        return " ".repeat(indent) + switch (expression.command.name) {
            case "push_constant_int" -> formatConstant(expression.type.get(0), expression.operand);
            case "push_constant_string" -> formatConstant(expression.type.get(0), expression.operand);

            case "flow_assign" -> {
                var targets = (List<Object>) expression.operand;

                if (targets.stream().allMatch(Objects::isNull)) {
                    yield expression.arguments.stream().map(CodeFormatter::format).collect(Collectors.joining(", "));
                } else {
                    var left = new ArrayList<String>();
                    var types = expression.arguments.stream().flatMap(a -> a.type.stream()).toList();
                    var typeIndex = 0;

                    for (var target : targets) {
                        switch (target) {
                            case LocalReference local -> {
                                var type = types.get(typeIndex);

                                if (declaredLocals != null && declaredLocals.add(local)) {
                                    left.add("def_" + formatType(type, true) + " " + formatLocal(local, type));
                                } else {
                                    left.add(formatLocal(local, type));
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

                        typeIndex++;
                    }

                    yield String.join(", ", left) + " = " + expression.arguments.stream().map(CodeFormatter::format).collect(Collectors.joining(", "));
                }
            }

            case "flow_load" -> formatLoadTarget(expression.operand, expression.type.get(0));

            case "flow_preinc" -> "++" + formatLoadTarget(expression.operand, Type.INT_INT);
            case "flow_predec" -> "--" + formatLoadTarget(expression.operand, Type.INT_INT);
            case "flow_postinc" -> formatLoadTarget(expression.operand, Type.INT_INT) + "++";
            case "flow_postdec" -> formatLoadTarget(expression.operand, Type.INT_INT) + "--";

            case "gosub_with_params" -> {
                var script = formatConstant(Type.CLIENTSCRIPT, expression.operand);

                if (expression.arguments.isEmpty()) {
                    yield "~" + script;
                } else {
                    yield "~" + script + "(" + expression.arguments.stream().map(CodeFormatter::format).collect(Collectors.joining(", ")) + ")";
                }
            }

            case "define_array" -> {
                var index = (int) expression.operand >> 16;
                var type = Type.byChar((int) expression.operand & 0xffff);
                yield "def_" + formatType(type, true) + " $" + formatType(type, false) + "array" + index + "(" + format(expression.arguments.get(0)) + ")";
            }

            case "push_array_int" -> {
                var index = (int) expression.operand;
                yield "$" + formatType(expression.type.get(0), false) + "array" + index + "(" + format(expression.arguments.get(0)) + ")";
            }

            case "pop_array_int" -> {
                var index = (int) expression.operand;
                yield "$" + formatType(expression.arguments.get(1).type.get(0), false) + "array" + index + "(" + format(expression.arguments.get(0)) + ") = " + format(expression.arguments.get(1));
            }

            case "add" -> {
                var s = formatBinary(prec, 50, " + ", expression.arguments.get(0), expression.arguments.get(1));
                yield prec < 50 ? "calc(" + s + ")" : s;
            }

            case "sub" -> {
                var s = formatBinary(prec, 50, " - ", expression.arguments.get(0), expression.arguments.get(1));
                yield prec < 50 ? "calc(" + s + ")" : s;
            }

            case "multiply" -> {
                var s = formatBinary(prec, 60, " * ", expression.arguments.get(0), expression.arguments.get(1));
                yield prec < 50 ? "calc(" + s + ")" : s;
            }

            case "divide" -> {
                var s = formatBinary(prec, 60, " / ", expression.arguments.get(0), expression.arguments.get(1));
                yield prec < 50 ? "calc(" + s + ")" : s;
            }

            case "modulo" -> {
                var s = formatBinary(prec, 60, " % ", expression.arguments.get(0), expression.arguments.get(1));
                yield prec < 50 ? "calc(" + s + ")" : s;
            }

            case "join_string" -> {
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

                yield "\"" + result + "\"";
            }

            case "db_find", "db_find_with_count", "db_find_refine", "db_find_refine_with_count" -> expression.command.name + "(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ")";

            // control flow
            case "flow_ne" -> formatBinary(prec, 40, " ! ", expression.arguments.get(0), expression.arguments.get(1));
            case "flow_eq" -> formatBinary(prec, 40, " = ", expression.arguments.get(0), expression.arguments.get(1));
            case "flow_lt" -> formatBinary(prec, 40, " < ", expression.arguments.get(0), expression.arguments.get(1));
            case "flow_gt" -> formatBinary(prec, 40, " > ", expression.arguments.get(0), expression.arguments.get(1));
            case "flow_le" -> formatBinary(prec, 40, " <= ", expression.arguments.get(0), expression.arguments.get(1));
            case "flow_ge" -> formatBinary(prec, 40, " >= ", expression.arguments.get(0), expression.arguments.get(1));

            case "flow_and" -> formatBinary(prec, 20, " & ", expression.arguments.get(0), expression.arguments.get(1));
            case "flow_or" -> formatBinary(prec, 10, " | ", expression.arguments.get(0), expression.arguments.get(1));

            case "flow_if" -> "if (" + format(expression.arguments.get(0)) + ") {\n" + formatBlock((List<Expression>) expression.operand, indent + 4, declaredLocals) + " ".repeat(indent) + "}";

            case "flow_ifelse" -> {
                var trueBranch = ((IfElseBranches) expression.operand).trueBranch();
                var falseBranch = ((IfElseBranches) expression.operand).falseBranche();

                if (falseBranch.size() == 1 && (falseBranch.get(0).command == FLOW_IF || falseBranch.get(0).command == FLOW_IFELSE)) {
                    yield "if (" + format(expression.arguments.get(0)) + ") {\n" + formatBlock(trueBranch, indent + 4, declaredLocals) + " ".repeat(indent) + "} else " + format(falseBranch.get(0), 0, indent, declaredLocals).substring(indent);
                } else {
                    yield "if (" + format(expression.arguments.get(0)) + ") {\n" + formatBlock(trueBranch, indent + 4, declaredLocals) + " ".repeat(indent) + "} else {\n" + formatBlock(falseBranch, indent + 4, declaredLocals) + " ".repeat(indent) + "}";
                }
            }

            case "flow_while" -> "while (" + format(expression.arguments.get(0)) + ") {\n" + formatBlock((List<Expression>) expression.operand, indent + 4, declaredLocals) + " ".repeat(indent) + "}";

            case "flow_switch" -> {
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
                yield result;
            }

            // only used for debug output
            case "label" -> "label(" + expression.operand + ")";
            case "branchif" -> "branchif(" + format(expression.arguments.get(0)) + ", " + ((BranchIfTarget) expression.operand).a() + ", " + ((BranchIfTarget) expression.operand).b() + ")";

            case "branch" -> "branch(" + expression.operand + ")";
            case "branch_equals" -> "branch_equals(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")";
            case "branch_less_than" -> "branch_less_than(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")";
            case "branch_greater_than" -> "branch_greater_than(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")";
            case "branch_less_than_or_equals" -> "branch_less_than_or_equals(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")";
            case "branch_greater_than_or_equals" -> "branch_greater_than_or_equals(" + format(expression.arguments.get(0)) + ", " + format(expression.arguments.get(1)) + ", " + expression.operand + ")";

            case "switch" -> {
                var table = new ArrayList<String>();

                for (var branch : (List<SwitchCase>) expression.operand) {
                    table.add(branch.value() + " => " + branch.target());
                }

                yield "switch(" + expression.arguments.get(0) + ", " + String.join(", ", table) + ")";
            }

            default -> {
                var dot = expression.operand instanceof Integer i && i == 1;
                var operand = Objects.equals(expression.operand, 0) || Objects.equals(expression.operand, 1) ? "" : "[" + expression.operand + "]";

                if (expression.arguments.isEmpty()) {
                    yield (dot ? "." : "") + expression.command.name + operand;
                } else {
                    var arguments = expression.arguments.stream().map(CodeFormatter::format).collect(Collectors.joining(", "));

                    if (ScriptUnpacker.FORMAT_HOOKS && expression.command.hasHook()) {
                        var hookStart = 0;
                        var hookEnd = expression.arguments.size() - (expression.command.arguments.size() - 1);
                        arguments = formatHook(expression.arguments.subList(hookStart, hookEnd));

                        if (expression.arguments.size() != hookEnd) {
                            arguments += ", " + expression.arguments.subList(hookEnd, expression.arguments.size()).stream().map(CodeFormatter::format).collect(Collectors.joining(", "));
                        }
                    }

                    yield (dot ? "." : "") + expression.command.name + operand + "(" + arguments + ")";
                }
            }
        };
    }

    private static String formatLoadTarget(Object operand, Type type) {
        return switch (operand) {
            case LocalReference local -> formatLocal(local, type);
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
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 4)) return "event_opindex";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 5)) return "event_comsubid";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 6)) return "event_com2";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 7)) return "event_comsubid2";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 8)) return "event_key";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 9)) return "event_keychar";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 10)) return "event_gamepadvalue";
            if (Objects.equals(expression.operand, Integer.MIN_VALUE + 11)) return "event_gamepadbutton";
        }

        if (expression.command == PUSH_CONSTANT_STRING) {
            if (Objects.equals(expression.operand, "event_opbase")) return "event_opbase";
            if (Objects.equals(expression.operand, "event_text")) return "event_text";
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

        if (value instanceof Integer i) return Unpacker.format(type, i);
        if (value instanceof String s) return Unpacker.format(type, s);
        throw new IllegalStateException("invalid constant");
    }

    private static String formatType(Type type, boolean real) {
        type = ScriptUnpacker.chooseDisplayType(type);

        if (type.alias != null && real && !ScriptUnpacker.OUTPUT_TYPE_ALIASES) {
            type = type.alias;
        }

        return type.name;
    }

    private static String formatLocal(LocalReference local, Type type) {
        return "$" + formatType(type, false) + local.local();
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
