package osrs.unpack.script;

import osrs.unpack.Type;

import java.util.*;
import java.util.stream.IntStream;

import static osrs.unpack.script.Command.*;

public class TypePropagator {
    private final Map<Node, VariableSet> sets = new HashMap<>();

    public void run(int script, List<Expression> expressions) {
        for (var expression : expressions) {
            run(script, expression);
        }
    }

    public void run(int script, Expression expression) {
        // arguments
        if (expression.command == GOSUB_WITH_PARAMS) {
            var otherScript = (int) expression.operand;
            ScriptUnpacker.CALLED.add(otherScript);
            var index = 0;

            for (var i = 0; i < expression.arguments.size(); i++) {
                var arg = expression.arguments.get(i);

                for (var j = 0; j < arg.type.size(); j++) {
                    merge(type(arg, j), parameter(otherScript, index++));
                }
            }

            for (var i = 0; i < expression.type.size(); i++) {
                merge(type(expression, i), result(otherScript, i));
            }
        }

        if (expression.command.hasHook()) {
            var hookStart = 0;
            var hookEnd = expression.arguments.size() - (expression.command.arguments.size() - 1);

            var otherScript = (int) expression.arguments.get(hookStart++).operand;
            var signature = (String) expression.arguments.get(hookEnd-- - 1).operand;

            if (signature.endsWith("Y")) {
                var transmitListCount = (int) expression.arguments.get(hookEnd - 1).operand;
                hookEnd -= 1 + transmitListCount;
            }

            if (otherScript != -1) {
                ScriptUnpacker.CALLED.add(otherScript);
                ScriptUnpacker.CLIENTSCRIPT.add(otherScript);
                var index = 0;

                for (var i = hookStart; i < hookEnd; i++) {
                    var arg = expression.arguments.get(i);

                    if (arg.command == PUSH_CONSTANT_INT) {
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 1)) bound(type(arg, 0), Type.INT_INT); // event_mousex
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 2)) bound(type(arg, 0), Type.INT_INT); // event_mousey
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 3)) bound(type(arg, 0), Type.COMPONENT); // event_com
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 4)) bound(type(arg, 0), Type.INT_INT); // event_opindex
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 5)) bound(type(arg, 0), Type.INT_INT); // event_comsubid
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 6)) bound(type(arg, 0), Type.COMPONENT); // event_com2
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 7)) bound(type(arg, 0), Type.INT_INT); // event_comsubid2
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 8)) bound(type(arg, 0), Type.INT_KEY); // event_key
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 9)) bound(type(arg, 0), Type.CHAR); // event_keychar
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 10)) bound(type(arg, 0), Type.INT_INT); // event_gamepadvalue
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 11)) bound(type(arg, 0), Type.INT_INT); // event_gamepadbutton
                    }

                    if (arg.command == PUSH_CONSTANT_STRING) {
                        if (Objects.equals(arg.operand, "event_opbase")) bound(type(arg, 0), Type.STRING); // event_opbase
                        if (Objects.equals(arg.operand, "event_text")) bound(type(arg, 0), Type.STRING); // event_text
                    }

                    for (var j = 0; j < arg.type.size(); j++) {
                        merge(type(arg, j), parameter(otherScript, index++));
                    }
                }
            }
        }

        if (expression.command == RETURN) {
            var index = 0;

            for (var i = 0; i < expression.arguments.size(); i++) {
                var arg = expression.arguments.get(i);

                for (var j = 0; j < arg.type.size(); j++) {
                    merge(type(arg, j), result(script, index++));
                }
            }
        }

        // locals
        if (expression.command == FLOW_LOAD) {
            if (expression.operand instanceof LocalReference local) {
                merge(type(expression, 0), local(script, local.domain(), local.local()));
            }
        }

        if (expression.command == FLOW_ASSIGN) {
            var targets = (List<Object>) expression.operand;

            for (var i = 0; i < targets.size(); i++) {
                if (targets.get(i) instanceof LocalReference local) {
                    merge(arg(expression, i), local(script, local.domain(), local.local()));
                }
            }
        }

        // arrays
        if (expression.command == PUSH_ARRAY_INT) {
            merge(type(expression, 0), array(script, (int) expression.operand));
        }

        if (expression.command == POP_ARRAY_INT) {
            merge(arg(expression, 1), array(script, (int) expression.operand));
        }

        // equality
        if (expression.command == FLOW_EQ || expression.command == FLOW_NE) {
            merge(arg(expression, 0), arg(expression, 1));
        }

        // visit children
        expression.visitChildren(c -> run(script, c));
    }

    public void finish(Set<Integer> scripts) {
        // merge parameters with locals
        for (var script : scripts) {
            var parameterCountInt = ScriptUnpacker.getParameterCount(script, LocalDomain.INTEGER);
            var parameterCountString = ScriptUnpacker.getParameterCount(script, LocalDomain.STRING);
            var parameterCount = parameterCountInt + parameterCountString;

            if (!ScriptUnpacker.CALLED.contains(script)) {
                // impossible to infer order, assign default order
                var index = 0;

                for (var i = 0; i < parameterCountInt; i++) {
                    bound(parameter(script, index++), Type.UNKNOWN_INT);
                }

                for (var i = 0; i < parameterCountString; i++) {
                    bound(parameter(script, index++), Type.STRING);
                }
            }

            var indexInt = 0;
            var indexString = 0;

            for (var i = 0; i < parameterCount; i++) {
                var parameter = parameter(script, i);

                // if only one kind of parameter is left, that confirms all the end kinds
                if (indexString == parameterCountString) bound(parameter, Type.UNKNOWN_INT);
                if (indexInt == parameterCountInt) bound(parameter, Type.STRING);

                var type = find(parameter).type;

                if (Type.subtype(type, Type.UNKNOWN_INT)) {
                    merge(parameter, local(script, LocalDomain.INTEGER, indexInt++));
                } else if (Type.subtype(type, Type.STRING)) {
                    merge(parameter, local(script, LocalDomain.STRING, indexString++));
                } else {
                    System.err.println("unable to infer parameter order for " + script);
                    break;
                }
            }
        }

        // output script signatures
        for (var script : scripts) {
            ScriptUnpacker.SCRIPT_PARAMETERS.put(script, IntStream.range(0, ScriptUnpacker.getParameterCount(script)).mapToObj(i -> find(parameter(script, i)).type).toList());
            ScriptUnpacker.SCRIPT_RETURNS.put(script, IntStream.range(0, ScriptUnpacker.getReturnCount(script)).mapToObj(i -> find(result(script, i)).type).toList());
        }

        // assume unused is clientscript
        if (ScriptUnpacker.ASSUME_UNUSED_IS_CLIENTSCRIPT) {
            for (var script : scripts) {
                if (!ScriptUnpacker.CALLED.contains(script) && ScriptUnpacker.SCRIPT_RETURNS.get(script).isEmpty()) {
                    ScriptUnpacker.CLIENTSCRIPT.add(script);
                }
            }
        }
    }

    private void merge(Node a, Node b) {
        // todo: for better performance, switch to a disjoint-set data structure
        var setA = find(a);
        var setB = find(b);

        if (setA == setB) {
            return;
        }

        if (setB.size() > setA.size()) {
            var t = setA;
            setA = setB;
            setB = t;
        }

        // merge types
        var type = Type.meet(setA.type, setB.type);

        if (type == null) {
            throw new IllegalStateException("type mismatch during propagation, " + setA.type + " and " + setB.type + " have no common subtype, sets " + setA.set + " and " + setB.set);
        }

        setA.setType(type);
        setB.setType(type);

        // merge sets
        setA.set.addAll(setB.set);

        for (var node : setB.set) {
            this.sets.put(node, setA);
        }
    }

    private void bound(Node parameter, Type type) {
        var set = find(parameter);
        var meet = Type.meet(set.type, type);

        if (meet == null) {
            throw new IllegalStateException("type mismatch during propagation, " + set.type + " and " + type + " have no common subtype, set " + set.set + " and explicit " + type);
        }

        set.setType(meet);
    }

    private VariableSet find(Node a) {
        return sets.computeIfAbsent(a, _ -> new VariableSet(a));
    }

    private Node parameter(int script, int index) {
        // We can't use locals directly because we don't know which local domain
        // they will go into (int/long/object). In a case like ~a(~b, ~c), we can
        // only determine the base types of ~b and ~c after their return types are
        // propagated, so we merge params with local variables in a separate pass
        // at the end
        return new ScriptParamNode(script, index);
    }

    private Node result(int script, int index) {
        return new ScriptReturnNode(script, index);
    }

    private Node local(int script, LocalDomain baseType, int index) {
        return new LocalNode(script, baseType, index);
    }

    private Node array(int script, int index) {
        return new ArrayNode(script, index);
    }

    private static ExpressionTypeNode type(Expression expression, int index) {
        return new ExpressionTypeNode(expression, index);
    }

    private static ExpressionTypeNode arg(Expression expression, int index) {
        for (var argument : expression.arguments) {
            if (index < argument.type.size()) {
                return type(argument, index);
            }

            index -= argument.type.size();
        }

        throw new IllegalArgumentException();
    }

    private static class VariableSet {
        private Type type;
        private Set<Node> set;

        public VariableSet(Node node) {
            set = new LinkedHashSet<>(List.of(node));
            type = node.getType();
        }

        public void setType(Type type) {
            if (this.type != type) {
                this.type = type;

                for (var node : set) {
                    node.setType(type);
                }
            }
        }

        public int size() {
            return set.size();
        }
    }

    private interface Node {
        default Type getType() {
            return Type.UNKNOWN;
        }

        default void setType(Type type) {
            // nothing to do
        }
    }

    private record ExpressionTypeNode(Expression expression, int index) implements Node {
        @Override
        public Type getType() {
            return expression.type.get(index);
        }

        @Override
        public void setType(Type type) {
            expression.type.set(index, type);
        }

        public String toString() {
            return expression + " type " + index;
        }
    }

    private record LocalNode(int script, LocalDomain domain, int index) implements Node {
        public String toString() {
            return "script" + script + "." + domain.name().toLowerCase(Locale.ROOT) + index;
        }
    }

    private record ArrayNode(int script, int index) implements Node {
        public String toString() {
            return "script" + script + ".array" + index;
        }
    }

    private record ScriptParamNode(int script, int index) implements Node {
        public String toString() {
            return "script" + script + ".param" + index;
        }
    }

    private record ScriptReturnNode(int script, int index) implements Node {
        public String toString() {
            return "script" + script + ".return" + index;
        }
    }
}
