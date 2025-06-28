package osrs.unpack.script;

import osrs.Unpack;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static osrs.unpack.script.Command.*;

public class SyntaxBuilder {
    private final int currentScript;
    private final List<Expression> stack = new ArrayList<>();
    private final List<Object> pops = new ArrayList<>();

    public SyntaxBuilder(int currentScript) {
        this.currentScript = currentScript;
    }

    public List<Expression> build(Instruction[] code) {
        var labels = new HashSet<Integer>();

        for (var index = 0; index < code.length; index++) {
            var instruction = code[index];

            if (isConditionalBranch(instruction.command())) {
                labels.add(index + 1);
                labels.add((int) instruction.operand());
            } else if (instruction.command() == BRANCH) {
                labels.add((int) instruction.operand());
            } else if (instruction.command() == SWITCH) {
                for (var branch : (List<SwitchCase>) instruction.operand()) {
                    labels.add(branch.target());
                }
            }
        }

        for (var index = 0; index < code.length; index++) {
            process(code, index, labels);
        }

        return block(0, stack.size());
    }

    private void process(Instruction[] code, int index, HashSet<Integer> labels) {
        var instruction = code[index];
        var command = instruction.command();
        var operand = instruction.operand();

        // merge pops into a single assign command
        if (command == POP_INT_LOCAL) {
            pops.add(new LocalReference(LocalDomain.INTEGER, (int) operand));
            return;
        }

        if (command == POP_STRING_LOCAL) {
            pops.add(new LocalReference(LocalDomain.STRING, (int) operand));
            return;
        }

        if (command == POP_INT_DISCARD || command == POP_STRING_DISCARD) {
            pops.add(null);
            return;
        }

        if (command == POP_VAR) {
            pops.add(new VarPlayerReference((int) operand));
            return;
        }

        if (command == POP_VARBIT) {
            pops.add(new VarPlayerBitReference((int) operand));
            return;
        }

        if (command == POP_VARC_INT) {
            pops.add(new VarClientReference((int) operand, false));
            return;
        }

        if (command == POP_VARC_STRING_OLD) {
            pops.add(new VarClientStringReference((int) operand));
            return;
        }

        if (command == POP_VARC_STRING) {
            pops.add(new VarClientReference((int) operand, true));
            return;
        }

        if (!pops.isEmpty()) {
            var argumentTypes = new ArrayList<Type>();

            for (var pop : pops.reversed()) {
                switch (pop) {
                    case VarPlayerReference(var var) -> argumentTypes.add(Type.UNKNOWN_INT);
                    case VarPlayerBitReference(var var) -> argumentTypes.add(Type.INT);
                    case VarClientReference(var var, var string) -> argumentTypes.add(string ? Type.STRING : Type.UNKNOWN_INT);
                    case VarClientStringReference(var var) -> argumentTypes.add(Type.STRING);

                    case LocalReference local -> argumentTypes.add(switch (local.domain()) {
                        case INTEGER -> Type.UNKNOWN_INT;
                        case STRING -> Unpack.VERSION >= 231 ? Type.UNKNOWN_OBJECT : Type.STRING;
                        case ARRAY -> throw new AssertionError();
                    });

                    case null -> argumentTypes.add(Type.UNKNOWN); // discards can pop stacks in different order
                    default -> throw new IllegalStateException("invalid pop type");
                }
            }

            buildCommand(code, index, FLOW_ASSIGN, new ArrayList<>(pops.reversed()), argumentTypes, List.of());
            pops.clear();
        }

        // add labels
        if (labels.contains(index)) {
            buildCommand(code, index, LABEL, index, List.of(), List.of());
        }

        // handle special commands
        if (command == PUSH_CONSTANT_INT) {
            var value = (int) operand;
            var booleanPossible = value == -1 || value == 0 || value == 1;
            buildCommand(code, index, PUSH_CONSTANT_INT, operand, List.of(), List.of(booleanPossible ? Type.UNKNOWN_INT : Type.UNKNOWN_INT_NOTBOOLEAN));
            return;
        }

        if (command == PUSH_INT_LOCAL) {
            buildCommand(code, index, FLOW_LOAD, new LocalReference(LocalDomain.INTEGER, (int) operand), List.of(), List.of(Type.UNKNOWN_INT));
            return;
        }

        if (command == PUSH_STRING_LOCAL) {
            buildCommand(code, index, FLOW_LOAD, new LocalReference(LocalDomain.STRING, (int) operand), List.of(), List.of(Unpack.VERSION >= 231 ? Type.UNKNOWN_OBJECT : Type.STRING));
            return;
        }

        if (command == PUSH_VAR) {
            var var = (int) operand;
            var type = Type.UNKNOWN_INT;
            buildCommand(code, index, FLOW_LOAD, new VarPlayerReference(var), List.of(), List.of(type));
            return;
        }

        if (command == PUSH_VARBIT) {
            var var = (int) operand;
            var type = Type.INT;
            buildCommand(code, index, FLOW_LOAD, new VarPlayerBitReference(var), List.of(), List.of(type));
            return;
        }

        if (command == PUSH_VARC_INT) {
            var var = (int) operand;
            var type = Type.UNKNOWN_INT;
            buildCommand(code, index, FLOW_LOAD, new VarClientReference(var, false), List.of(), List.of(type));
            return;
        }

        if (command == PUSH_VARC_STRING_OLD) {
            var var = (int) operand;
            var type = Type.STRING;
            buildCommand(code, index, FLOW_LOAD, new VarClientStringReference(var), List.of(), List.of(type));
            return;
        }

        if (command == PUSH_VARC_STRING) {
            var var = (int) operand;
            var type = Type.STRING;
            buildCommand(code, index, FLOW_LOAD, new VarClientReference(var, true), List.of(), List.of(type));
            return;
        }

        if (command == PUSH_VARCLANSETTING) {
            var var = (int) operand;
            var type = Unpacker.getVarClanSettingType(var);
            buildCommand(code, index, FLOW_LOAD, new VarClanSettingReference(var), List.of(), List.of(type));
            return;
        }

        if (command == PUSH_VARCLAN) {
            var var = (int) operand;
            var type = Unpacker.getVarClanType(var);
            buildCommand(code, index, FLOW_LOAD, new VarClanReference(var), List.of(), List.of(type));
            return;
        }

        if (command == GOSUB_WITH_PARAMS) {
            var argumentTypes = Collections.nCopies(ScriptUnpacker.getParameterCount((int) operand), Type.UNKNOWN);
            var returnTypes = ScriptUnpacker.getReturnTypes((int) operand);
            buildCommand(code, index, command, operand, argumentTypes, returnTypes);
            return;
        }

        if (command == RETURN) {
            var argumentTypes = ScriptUnpacker.getReturnTypes(currentScript);
            var returnTypes = List.<Type>of();
            buildCommand(code, index, command, operand, argumentTypes, returnTypes);
            return;
        }

        if (command == JOIN_STRING) {
            var argumentTypes = Collections.nCopies((int) operand, Type.STRING);
            var returnTypes = List.of(Type.STRING);
            buildCommand(code, index, command, operand, argumentTypes, returnTypes);
            return;
        }

        if (command == DB_GETFIELD) {
            var column = (int) stack.get(stack.size() - 2).operand;
            var argumentTypes = List.of(Type.DBROW, Type.DBCOLUMN, Type.INT_INT);
            var returnTypes = Unpacker.getDBColumnTypeTuple(column >>> 12, (column >>> 4) & 255, (column & 15) - 1);
            buildCommand(code, index, command, operand, argumentTypes, returnTypes);
            return;
        }

        // handle regular command
        var argumentTypes = command.arguments;
        var returnTypes = command.returns;

        if (argumentTypes == null || returnTypes == null) {
            throw new IllegalStateException("missing types for command: " + command.name);
        }

        while (argumentTypes.contains(Type.ARGUMENT_LIST)) {
            var hookIndex = argumentTypes.lastIndexOf(Type.ARGUMENT_LIST);

            var signature = ((String) stack.get(stack.size() - (argumentTypes.size() - hookIndex)).operand).codePoints().mapToObj(c -> {
                if (Unpack.VERSION < 231) {
                    if (ScriptUnpacker.IGNORE_HOOK_TYPE_INFO) {
                        return switch (c) {
                            case 's' -> Type.STRING;
                            case 'Y' -> Type.TRANSMIT_LIST;
                            default -> Type.UNKNOWN_INT;
                        };
                    }

                    return Type.byChar(c);
                } else {
                    return switch (c) {
                        case 'i' -> Type.UNKNOWN_INT;
                        case 's' -> Type.STRING;
                        case 'X' -> Type.STRINGARRAY;
                        case 'W' -> Type.UNKNOWN_INTARRAY;
                        case 'Y' -> Type.TRANSMIT_LIST;
                        default -> throw new IllegalStateException("unexpected hook type " + c);
                    };
                }
            }).toList();

            var result = new ArrayList<>(argumentTypes.subList(0, hookIndex));

            if (signature.isEmpty() || signature.get(signature.size() - 1) != Type.TRANSMIT_LIST) {
                result.addAll(signature);
            } else {
                var transmitListSize = (int) stack.get(stack.size() - (argumentTypes.size() - hookIndex) - 1).operand;
                result.addAll(signature.subList(0, signature.size() - 1));

                result.addAll(Collections.nCopies(transmitListSize, switch (command.name) {
                    case "if_setonstattransmit", "cc_setonstattransmit" -> Type.STAT;
                    case "if_setoninvtransmit", "cc_setoninvtransmit" -> Type.INV;
                    case "if_setonvartransmit", "cc_setonvartransmit" -> Type.VAR_PLAYER;
                    case "if_setonvarctransmit", "cc_setonvarctransmit", "if_setonvarcstrtransmit", "cc_setonvarcstrtransmit" -> Type.VAR_CLIENT;
                    default -> throw new IllegalStateException("unexpected transmit list for command " + command);
                }));

                result.add(Type.INT_INT); // transmit list size
            }

            result.add(Type.STRING);
            result.addAll(argumentTypes.subList(hookIndex + 1, argumentTypes.size()));
            argumentTypes = result;
        }

        buildCommand(code, index, command, operand, argumentTypes, returnTypes);
    }

    private void buildCommand(Instruction[] code, int index, Command command, Object operand, List<Type> argumentTypes, List<Type> returnTypes) {
        var arguments = new ArrayList<Expression>();
        var remainingTypes = new ArrayList<>(argumentTypes);

        while (!remainingTypes.isEmpty()) {
            if (stack.isEmpty()) {
                throw new IllegalStateException("argument count mismatch in script " + currentScript + ", context: " + List.of(code).subList(0, index + 1));
            }

            var last = stack.removeLast();
            arguments.addFirst(last);
            var expressionTypes = last.type;

            if (ScriptUnpacker.CHECK_EMPTY_ARGUMENT) {
                if (expressionTypes.isEmpty()) {
                    throw new IllegalStateException("argument has no return types in script " + currentScript + ", context: " + List.of(code).subList(0, index + 1));
                }
            }

            for (var i = expressionTypes.size() - 1; i >= 0; i--) {
                var currentType = expressionTypes.get(i);

                if (remainingTypes.isEmpty()) {
                    throw new IllegalStateException(" argument would overfill command types in script " + currentScript + ", context: " + List.of(code).subList(0, index + 1));
                } else {
                    var expectedType = remainingTypes.removeLast();

                    if (expectedType != currentType) {
                        var meet = Type.LATTICE.meet(expectedType, currentType);

                        if (meet != null) {
                            expressionTypes.set(i, meet); // propagate down
                        } else if (!Type.LATTICE.test(currentType, expectedType)) { // incomparable types
                            throw new IllegalStateException("type mismatch in script " + currentScript + ", assigning " + expectedType + " to " + currentType + ", context: " + List.of(code).subList(0, index + 1));
                        }
                    }
                }
            }
        }

        stack.add(new Expression(command, operand, returnTypes, arguments));
        buildControlFlow(code, index);
    }

    private void buildControlFlow(Instruction[] code, int index) {
        while (true) {
            if (buildPreIncrement()) continue;
            if (buildPreDecrement()) continue;
            if (buildPostIncrement()) continue;
            if (buildPostDecrement()) continue;
            if (buildCondition(index)) continue;
            if (buildConditionMerge()) continue;
            if (buildAnd(code, index)) continue;
            if (buildOr()) continue;
            if (buildIf()) continue;
            if (buildIfElse()) continue;
            if (buildWhile()) continue;
            if (buildSwitch(index)) continue;
            if (buildEmptySwitch(code, index)) continue;
            break;
        }
    }

    // match: [$x = $x + 1, $x]
    // replace: [++$x]
    private boolean buildPreIncrement() {
        if (stack.size() < 2) return false;
        var command1 = stack.get(stack.size() - 2);
        var command2 = stack.get(stack.size() - 1);
        if (command1.command != FLOW_ASSIGN) return false;
        if (command1.arguments.get(0).command != ADD) return false;
        if (command1.arguments.get(0).arguments.get(0).command != FLOW_LOAD) return false;
        if (command1.arguments.get(0).arguments.get(1).command != PUSH_CONSTANT_STRING) return false;
        if (command2.command != FLOW_LOAD) return false;
        if (!Objects.equals(command1.arguments.get(0).arguments.get(0).operand, command2.operand)) return false;
        if (!Objects.equals(command1.operand, List.of(command2.operand))) return false;
        stack.subList(stack.size() - 2, stack.size()).clear();
        stack.add(new Expression(FLOW_PREINC, command2.operand, List.of(Type.INT_INT), List.of()));
        return true;
    }

    // match: [$x = $x - 1, $x]
    // replace: [--$x]
    private boolean buildPreDecrement() {
        if (stack.size() < 2) return false;
        var command1 = stack.get(stack.size() - 2);
        var command2 = stack.get(stack.size() - 1);
        if (command1.command != FLOW_ASSIGN) return false;
        if (command1.arguments.get(0).command != SUB) return false;
        if (command1.arguments.get(0).arguments.get(0).command != FLOW_LOAD) return false;
        if (command1.arguments.get(0).arguments.get(1).command != PUSH_CONSTANT_STRING) return false;
        if (command2.command != FLOW_LOAD) return false;
        if (!Objects.equals(command1.arguments.get(0).arguments.get(0).operand, command2.operand)) return false;
        if (!Objects.equals(command1.operand, List.of(command2.operand))) return false;
        stack.subList(stack.size() - 2, stack.size()).clear();
        stack.add(new Expression(FLOW_PREDEC, command2.operand, List.of(Type.INT_INT), List.of()));
        return true;
    }

    // match: [$x = $x + 1, $x]
    // replace: [$x++]
    private boolean buildPostIncrement() {
        if (stack.size() < 2) return false;
        var command1 = stack.get(stack.size() - 2);
        var command2 = stack.get(stack.size() - 1);
        if (command2.command != FLOW_ASSIGN) return false;
        if (command2.arguments.get(0).command != ADD) return false;
        if (command2.arguments.get(0).arguments.get(0).command != FLOW_LOAD) return false;
        if (command2.arguments.get(0).arguments.get(1).command != PUSH_CONSTANT_STRING) return false;
        if (command1.command != FLOW_LOAD) return false;
        if (!Objects.equals(command2.arguments.get(0).arguments.get(0).operand, command1.operand)) return false;
        if (!Objects.equals(command2.operand, List.of(command1.operand))) return false;
        stack.subList(stack.size() - 2, stack.size()).clear();
        stack.add(new Expression(FLOW_POSTINC, command1.operand, List.of(Type.INT_INT), List.of()));
        return true;
    }

    // match: [$x = $x - 1, $x]
    // replace: [$x++]
    private boolean buildPostDecrement() {
        if (stack.size() < 2) return false;
        var command1 = stack.get(stack.size() - 2);
        var command2 = stack.get(stack.size() - 1);
        if (command2.command != FLOW_ASSIGN) return false;
        if (command2.arguments.get(0).command != SUB) return false;
        if (command2.arguments.get(0).arguments.get(0).command != FLOW_LOAD) return false;
        if (command2.arguments.get(0).arguments.get(1).command != PUSH_CONSTANT_STRING) return false;
        if (command1.command != FLOW_LOAD) return false;
        if (!Objects.equals(command2.arguments.get(0).arguments.get(0).operand, command1.operand)) return false;
        if (!Objects.equals(command2.operand, List.of(command1.operand))) return false;
        stack.subList(stack.size() - 2, stack.size()).clear();
        stack.add(new Expression(FLOW_POSTDEC, command1.operand, List.of(Type.INT_INT), List.of()));
        return true;
    }

    // match: [branch_x(..., label_a)]
    // replace: [branchif(x(...), label_a, next)]
    private boolean buildCondition(int index) {
        if (stack.size() < 1) return false;
        var command1 = stack.get(stack.size() - 1);

        var condition = switch (command1.command.name) {
            case "branch_not" -> new Expression(FLOW_NE, null, List.of(Type.CONDITION), command1.arguments);
            case "branch_equals" -> new Expression(FLOW_EQ, null, List.of(Type.CONDITION), command1.arguments);
            case "branch_less_than" -> new Expression(FLOW_LT, null, List.of(Type.CONDITION), command1.arguments);
            case "branch_greater_than" -> new Expression(FLOW_GT, null, List.of(Type.CONDITION), command1.arguments);
            case "branch_less_than_or_equals" -> new Expression(FLOW_LE, null, List.of(Type.CONDITION), command1.arguments);
            case "branch_greater_than_or_equals" -> new Expression(FLOW_GE, null, List.of(Type.CONDITION), command1.arguments);
            default -> null;
        };

        if (condition == null) {
            return false;
        }

        // we have a branchif
        stack.set(stack.size() - 1, new Expression(BRANCHIF, new BranchIfTarget((int) command1.operand, index + 1), List.of(), List.of(condition)));
        return true;
    }

    // match: [branchif(..., next, b), label(next), branchif(..., a, b)] + no targets to next after
    // replace: [branchif(... & ..., a, b)]
    private boolean buildAnd(Instruction[] code, int index) {
        if (stack.size() < 3) return false;
        var command1 = stack.get(stack.size() - 3);
        var command2 = stack.get(stack.size() - 2);
        var command3 = stack.get(stack.size() - 1);
        if (command1.command != BRANCHIF) return false;
        if (command2.command != LABEL) return false;
        if (command3.command != BRANCHIF) return false;
        if (((BranchIfTarget) command1.operand).a() != (int) command2.operand) return false;
        if (((BranchIfTarget) command1.operand).b() != (int) ((BranchIfTarget) command3.operand).b()) return false;
        if (Arrays.stream(code, index, code.length).anyMatch(i -> i.command() == BRANCH && (int) i.operand() == (int) command2.operand)) return false; // nested while/if, not &

        // we have an &
        stack.subList(stack.size() - 3, stack.size()).clear();
        var condition = new Expression(FLOW_AND, null, List.of(Type.CONDITION), List.of(command1.arguments.get(0), command3.arguments.get(0)));
        stack.add(new Expression(BRANCHIF, command3.operand, List.of(), List.of(condition)));
        return true;
    }

    // match: [branchif(..., a, next), label(next), branchif(..., a, b)]
    // replace: [branchif(... & ..., a, b)
    private boolean buildOr() {
        if (stack.size() < 3) return false;
        var command1 = stack.get(stack.size() - 3);
        var command2 = stack.get(stack.size() - 2);
        var command3 = stack.get(stack.size() - 1);
        if (command1.command != BRANCHIF) return false;
        if (command2.command != LABEL) return false;
        if (command3.command != BRANCHIF) return false;
        if (((BranchIfTarget) command1.operand).a() != (int) ((BranchIfTarget) command3.operand).a()) return false;
        if (((BranchIfTarget) command1.operand).b() != (int) command2.operand) return false;

        // we have an |
        stack.subList(stack.size() - 3, stack.size()).clear();
        var condition = new Expression(FLOW_OR, null, List.of(Type.CONDITION), List.of(command1.arguments.get(0), command3.arguments.get(0)));
        stack.add(new Expression(BRANCHIF, command3.operand, List.of(), List.of(condition)));
        return true;
    }

    // match: [branchif(..., a, b), label(b), branch(c)]
    // replace: [branchif(..., a, c)]
    private boolean buildConditionMerge() {
        if (stack.size() < 3) return false;
        var command1 = stack.get(stack.size() - 3); // branchif(..., a, b)
        var command2 = stack.get(stack.size() - 2); // label(b)
        var command3 = stack.get(stack.size() - 1); // branch(c)
        if (command1.command != BRANCHIF) return false;
        if (command2.command != LABEL) return false;
        if (command3.command != BRANCH) return false;
        var a = ((BranchIfTarget) command1.operand).a();
        var b = ((BranchIfTarget) command1.operand).b();
        if (b != (int) command2.operand) return false;

        // we have a branchif
        stack.subList(stack.size() - 3, stack.size()).clear();
        stack.add(new Expression(BRANCHIF, new BranchIfTarget(a, (int) command3.operand), List.of(), command1.arguments));
        return true;
    }

    // match: [branchif(..., a, b), label(a), ...[no branches]..., label(b)]
    // replace: [if (...) { ... }, label(b)]
    private boolean buildIf() {
        // find end of if/else
        if (stack.size() < 1) return false;
        var end1 = stack.get(stack.size() - 1); // label(b)
        if (end1.command != LABEL) return false;

        // find start
        var start = findBranchIfFalse((int) end1.operand);
        if (start == null || start > stack.size() - 2) return false;
        var start1 = stack.get(start); // branchif(..., a, b)
        var start2 = stack.get(start + 1); // label(a)
        if (start2.command != LABEL) return false;
        if (((BranchIfTarget) start1.operand).a() != (int) start2.operand) return false;
        if (((BranchIfTarget) start1.operand).b() != (int) end1.operand) return false;

        // find body
        var body = block(start + 1, stack.size() - 1);
        if (body.stream().anyMatch(i -> i.command == BRANCH || i.command == BRANCHIF)) return false;

        // build the if
        stack.subList(start, stack.size()).clear();
        stack.add(new Expression(FLOW_IF, body, List.of(), start1.arguments));
        stack.add(end1);
        return true;
    }

    // match: [branchif(..., a, b), label(a), ...[no branches]..., branch(c), label(b), ...[no branches]..., label(c)]
    // replace: [if (...) { ... } else { ... }, label(c)]
    private boolean buildIfElse() {
        // find end of if/else
        if (stack.size() < 1) return false;
        var end1 = stack.get(stack.size() - 1); // label(c)
        if (end1.command != LABEL) return false;

        // find middle
        var mid = findBranch((int) end1.operand);
        if (mid == null || mid > stack.size() - 2) return false;
        var mid1 = stack.get(mid); // branch(c)
        var mid2 = stack.get(mid + 1); // label(b)
        if (mid2.command != LABEL) return false;

        // find start
        var start = findBranchIfFalse((int) mid2.operand);
        if (start == null || start > mid - 2) return false;
        var start1 = stack.get(start); // branchif(..., a, b)
        var start2 = stack.get(start + 1); // label(a)
        if (start2.command != LABEL) return false;
        if (((BranchIfTarget) start1.operand).a() != (int) start2.operand) return false;
        if (((BranchIfTarget) start1.operand).b() != (int) mid2.operand) return false;

        // find body
        var bodyThen = block(start + 2, mid);
        var bodyElse = block(mid + 1, stack.size() - 1);
        if (bodyThen.stream().anyMatch(i -> i.command == BRANCH || i.command == BRANCHIF)) return false;
        if (bodyElse.stream().anyMatch(i -> i.command == BRANCH || i.command == BRANCHIF)) return false;

        // build the if/else
        stack.subList(start, stack.size()).clear();
        stack.add(new Expression(FLOW_IFELSE, new IfElseBranches(bodyThen, bodyElse), List.of(), start1.arguments));
        stack.add(end1);
        return true;
    }

    // match: [label(a), branchif(..., b, c), label(b), ...[no branches]..., branch(a), label(c)]
    // replace: [label(a), while (...) { ... }, label(c)]
    private boolean buildWhile() {
        // find end of loop
        if (stack.size() < 2) return false;
        var end1 = stack.get(stack.size() - 2); // branch(a)
        var end2 = stack.get(stack.size() - 1); // label(c)
        if (end1.command != BRANCH) return false;
        if (end2.command != LABEL) return false;

        // find start of loop
        var start = findLabel((int) end1.operand);
        if (start == null || start > stack.size() - 5) return false;
        var start1 = stack.get(start); // label(a)
        var start2 = stack.get(start + 1); // branchif(..., b, c)
        var start3 = stack.get(start + 2); // label(b)
        if (start1.command != LABEL) return false;
        if (start2.command != BRANCHIF) return false;
        if (start3.command != LABEL) return false;
        if (((BranchIfTarget) start2.operand).a() != (int) start3.operand) return false;
        if (((BranchIfTarget) start2.operand).b() != (int) end2.operand) return false;
        if ((int) end1.operand != (int) start1.operand) return false;

        // find body
        var body = block(start + 3, stack.size() - 2);
        if (body.stream().anyMatch(i -> i.command == BRANCH || i.command == BRANCHIF)) return false;

        // build the loop
        stack.subList(start + 1, stack.size()).clear();
        stack.add(new Expression(FLOW_WHILE, body, List.of(), start2.arguments));
        stack.add(end2);
        return true;
    }

    // match: [switch(..., (... => ai)*), (branch(ai) | ...(no branches)...), (label(ai), ...(no branches)..., branch(b))*, label(b)]
    // replace:
    private boolean buildSwitch(int index) {
        if (stack.size() < 1) return false;
        var end1 = stack.get(stack.size() - 1); // label(c)
        if (end1.command != LABEL) return false;

        var start = find(e -> e.command == SWITCH);
        if (start == null) return false;
        var start1 = stack.get(start);
        var start2 = stack.get(start + 1);
        var branches = (List<SwitchCase>) start1.operand;

        // make sure we're not at the start of the switch
        if (branches.stream().anyMatch(branch -> branch.target() > index)) return false;

        // find default case
        var defaultBranchStart = -1;
        var casesStart = -1;

        if (start2.command == BRANCH) {
            var defaultBranchLabel = (int) start2.operand;

            if (defaultBranchLabel == (int) end1.operand) {
                defaultBranchStart = -1; // there is no default branch
                casesStart = start + 2;
            } else {
                var defaultBranchLabelIndex = findLabel(defaultBranchLabel);
                if (defaultBranchLabelIndex == null) return false;

                if (defaultBranchLabelIndex < start) { // this is an unrelated branch (switch with only default, at the end of a while loop)
                    defaultBranchStart = start + 1;
                    casesStart = start + 1;
                } else {
                    defaultBranchStart = defaultBranchLabelIndex;
                    casesStart = start + 2;
                }
            }
        } else { // default branch is first
            defaultBranchStart = start + 1;
            casesStart = start + 1;
        }

        // build cases
        var blocks = new ArrayList<SwitchBranch>();
        var cases = (List<Integer>) null;
        var started = false;
        var bodyStart = casesStart;

        for (var i = casesStart; i < stack.size() - 1; i++) {
            var expression = stack.get(i);

            if (!started) {
                if (i == defaultBranchStart) {
                    cases = null; // default
                } else {
                    if (expression.command != LABEL) {
                        return false; // there is stuff in between the branches
                    }

                    cases = new ArrayList<>(branches.stream().filter(branch -> branch.target() == (int) expression.operand).map(SwitchCase::value).toList());

                    if (cases.isEmpty()) {
                        return false; // branch is not a target of the switch
                    }
                }

                started = true;
            }

            if (expression.command == BRANCH && (int) expression.operand == (int) end1.operand) {
                blocks.add(new SwitchBranch(cases, block(bodyStart, i)));
                cases = null;
                bodyStart = i + 1;
                started = false;
            } else if (expression.command == BRANCH || expression.command == BRANCHIF || expression.command == SWITCH) {
                return false; // this covers the case where we're not in fact at the end of the switch (we see the branch to the real end)
            }
        }

        if (!started) { // special case where the last label is target of a switch
            cases = new ArrayList<>(branches.stream().filter(branch -> branch.target() == (int) end1.operand).map(SwitchCase::value).toList());

            if (!cases.isEmpty()) {
                started = true;
            } else { // empty switch at the end
                started = true;
                cases = null;
                bodyStart = stack.size() - 1;
            }
        }

        if (started) { // last case doesn't have a branch, just flows into end
            blocks.add(new SwitchBranch(cases, block(bodyStart, stack.size() - 1)));
        }

        // we have a switch
        stack.subList(start, stack.size()).clear();
        stack.add(new Expression(FLOW_SWITCH, blocks, List.of(), start1.arguments));
        stack.add(end1);
        return true;
    }

    // match: [switch(..., [])] + not followed by forward jump
    // replace:
    private boolean buildEmptySwitch(Instruction[] code, int index) {
        if (stack.size() < 1) return false;
        var command = stack.get(stack.size() - 1);
        if (command.command != SWITCH) return false; // switch
        if (!((List<SwitchCase>) command.operand).isEmpty()) return false; // which has no cases
        if (code[index + 1].command() == BRANCH && (int) code[index + 1].operand() >= index) return false; // and has no branch

        // we have an empty switch
        stack.removeLast();
        stack.add(new Expression(FLOW_SWITCH, List.of(), List.of(), command.arguments));
        return true;
    }

    private List<Expression> block(int start, int end) {
        List<Expression> result;

        if (ScriptUnpacker.KEEP_LABELS) {
            result = new ArrayList<>(stack.subList(start, end));
        } else {
            result = stack.subList(start, end).stream().filter(e -> e.command != LABEL).collect(Collectors.toList());
        }

        if (ScriptUnpacker.CHECK_NONEMPTY_STACK) {
            for (var expression : result) {
                if (!expression.type.isEmpty()) {
                    throw new IllegalStateException("value left on stack in script " + currentScript);
                }
            }
        }

        return result;
    }

    private Integer findBranch(int operand) {
        return find(i -> i.command == BRANCH && (int) i.operand == operand);
    }

    private Integer findBranchIfFalse(int operand) {
        return find(i -> i.command == BRANCHIF && ((BranchIfTarget) i.operand).b() == operand);
    }

    private Integer findLabel(int label) {
        return find(i -> i.command == LABEL && (int) i.operand == label);
    }

    private Integer find(Predicate<Expression> predicate) {
        for (var index = stack.size() - 1; index >= 0; index--) {
            var instruction = stack.get(index);

            if (predicate.test(instruction)) {
                return index;
            }
        }

        return null;
    }

    private boolean isConditionalBranch(Command command) {
        if (command == BRANCH_NOT) return true;
        if (command == BRANCH_EQUALS) return true;
        if (command == BRANCH_LESS_THAN) return true;
        if (command == BRANCH_GREATER_THAN) return true;
        if (command == BRANCH_LESS_THAN_OR_EQUALS) return true;
        if (command == BRANCH_GREATER_THAN_OR_EQUALS) return true;
        return false;
    }
}
