package osrs.unpack.script;

import osrs.Unpack;
import osrs.unpack.IfType;
import osrs.unpack.ScriptTrigger;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;

import java.util.*;
import java.util.stream.IntStream;

import static osrs.unpack.script.Command.*;

// 1. Visit the code, generating a type-valued variable for each code element whose type has to
//    be inferred, along with constraints between these variables based on data flow. The types
//    form a lattice of elements where lower elements represent more knowledge (for example, loc
//    < unknown_int < unknown).
//
// 2. Iteratively apply local rules to propagate knowledge across constraints, replacing the
//    type of vertices connected by an edge with the meet of the previous types, but with the
//    exception that namedobj -> ? only propagates forward as namedobj -> obj to allow for
//    upcasting when necessary. When two alias types conflict, their meet is the int_int type
//    type which then propagates across the entire connected component to erase the bad alias.
//    Constraints are re-processed when one of their variables changes type, and is repeated
//    until no more constraints are left to process.
//
// 3. Read the values of the type variables to set expression types, parameter types, etc. used
//    by other parts of the unpacker.
public class TypePropagator {
    private final Map<Node, Type> vars = new LinkedHashMap<>();
    private final Set<Constraint> constraints = new LinkedHashSet<>();

    public void run(int script, List<Expression> expressions) {
        for (var expression : expressions) {
            run(script, expression);
        }
    }

    public void run(int script, Expression expression) {
        // initial types from commands
        var types = expression.type;

        for (int i = 0; i < types.size(); i++) {
            emitAssign(type(expression, i), types.get(i));
        }

        // arguments
        if (expression.command == GOSUB_WITH_PARAMS) {
            var otherScript = (int) expression.operand;
            ScriptUnpacker.CALLED.add(otherScript);
            var index = 0;

            for (var i = 0; i < expression.arguments.size(); i++) {
                var arg = expression.arguments.get(i);

                for (var j = 0; j < arg.type.size(); j++) {
                    emitAssign(type(arg, j), parameter(otherScript, index++));
                }
            }

            for (var i = 0; i < expression.type.size(); i++) {
                emitEqual(type(expression, i), result(otherScript, i));
            }
        }

        if (expression.command.hasHook()) {
            var hookStart = 0;
            var hookEnd = expression.arguments.size() - (expression.command.arguments.size() - 2);

            var otherScript = (int) expression.arguments.get(hookStart++).operand;
            var signature = (String) expression.arguments.get(hookEnd-- - 1).operand;

            if (signature.endsWith("Y")) {
                var transmitListCount = (int) expression.arguments.get(hookEnd - 1).operand;
                hookEnd -= 1 + transmitListCount;
            }

            if (otherScript != -1) {
                ScriptUnpacker.CALLED.add(otherScript);
                ScriptUnpacker.SCRIPT_TRIGGERS.put(otherScript, ScriptTrigger.CLIENTSCRIPT);
                var index = 0;

                for (var i = hookStart; i < hookEnd; i++) {
                    var arg = expression.arguments.get(i);

                    if (arg.command == PUSH_CONSTANT_INT) {
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 1)) emitAssign(type(arg, 0), Type.INT_INT); // event_mousex
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 2)) emitAssign(type(arg, 0), Type.INT_INT); // event_mousey
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 3)) emitAssign(type(arg, 0), Type.COMPONENT); // event_com
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 4)) emitAssign(type(arg, 0), Type.INT_INT); // event_op
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 5)) emitAssign(type(arg, 0), Type.INT_INT); // event_comsubid
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 6)) emitAssign(type(arg, 0), Type.COMPONENT); // event_com2
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 7)) emitAssign(type(arg, 0), Type.INT_INT); // event_comsubid2
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 8)) emitAssign(type(arg, 0), Type.INT_KEY); // event_keycode
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 9)) emitAssign(type(arg, 0), Type.CHAR); // event_keychar
                        if (Objects.equals(arg.operand, Integer.MIN_VALUE + 10)) emitAssign(type(arg, 0), Type.INT_INT); // event_subop
                    }

                    if (arg.command == PUSH_CONSTANT_STRING) {
                        if (Objects.equals(arg.operand, "event_opbase")) emitAssign(type(arg, 0), Type.STRING); // event_opbase
                    }

                    for (var j = 0; j < arg.type.size(); j++) {
                        emitAssign(type(arg, j), parameter(otherScript, index++));
                    }
                }
            }
        }

        if (expression.command == RETURN) {
            var index = 0;

            for (var i = 0; i < expression.arguments.size(); i++) {
                var arg = expression.arguments.get(i);

                for (var j = 0; j < arg.type.size(); j++) {
                    emitAssign(type(arg, j), result(script, index++));
                }
            }
        }

        // locals
        if (expression.command == FLOW_LOAD) {
            if (expression.operand instanceof LocalReference local) {
                emitEqual(type(expression, 0), local(script, local.domain(), local.local()));
            }

            if (expression.operand instanceof VarPlayerReference var) {
                emitEqual(type(expression, 0), varplayer(var.var()));
            }

            if (expression.operand instanceof VarPlayerBitReference var) {
                emitEqual(type(expression, 0), varplayerbit(var.var()));
            }

            if (expression.operand instanceof VarClientReference var) {
                emitEqual(type(expression, 0), varclient(var.var()));
            }
        }

        if (expression.command == FLOW_ASSIGN) {
            var targets = (List<Object>) expression.operand;

            for (var i = 0; i < targets.size(); i++) {
                if (targets.get(i) instanceof LocalReference local) {
                    emitAssign(arg(expression, i), local(script, local.domain(), local.local()));
                }

                if (targets.get(i) instanceof VarPlayerReference var) {
                    emitAssign(arg(expression, i), varplayer(var.var()));
                }

                if (targets.get(i) instanceof VarPlayerBitReference var) {
                    emitAssign(arg(expression, i), varplayerbit(var.var()));
                }

                if (targets.get(i) instanceof VarClientReference var) {
                    emitAssign(arg(expression, i), varclient(var.var()));
                }
            }
        }

        // enums
        if (expression.command == ENUM) {
            var inputtype = expression.arguments.get(0);
            var outputtype = expression.arguments.get(1);
            var key = expression.arguments.get(3);
            emitAssign(type(key, 0), Type.byChar((int) inputtype.operand));
            emitEqual(type(expression, 0), Type.byChar((int) outputtype.operand));
        }

        if (expression.command == ENUM_STRING) {
            var enum_ = expression.arguments.get(0);
            var key = expression.arguments.get(1);
            emitAssign(type(key, 0), Unpacker.getEnumInputType((int) enum_.operand));
        }

        // params todo: can use a node to allow alias propagation through params
        if (expression.command == NC_PARAM || expression.command == LC_PARAM || expression.command == OC_PARAM || expression.command == STRUCT_PARAM) {
            var param = expression.arguments.get(1);
            emitEqual(type(expression, 0), Unpacker.getParamType((int) param.operand));
        }

        if (expression.command == CC_PARAM || expression.command == IF_PARAM) {
            var param = expression.arguments.get(0);
            emitEqual(type(expression, 0), Unpacker.getParamType((int) param.operand));
        }

        if (expression.command == CC_SETPARAM || expression.command == IF_SETPARAM) {
            var param = expression.arguments.get(0);
            var value = expression.arguments.get(1);
            emitEqual(type(value, 0), Unpacker.getParamType((int) param.operand));
        }

        // dbtables todo: can use a node to allow alias propagation through dbtables
        if (expression.command == DB_FIND || expression.command == DB_FIND_WITH_COUNT || expression.command == DB_FIND_REFINE || expression.command == DB_FIND_REFINE_WITH_COUNT) {
            var column = (int) expression.arguments.get(0).operand;
            var value = expression.arguments.get(1);
            emitEqual(type(value, 0), Unpacker.getDBColumnTypeTupleAssertSingle(column >>> 12, (column >>> 4) & 255, (column & 15) - 1));
        }

        // arrays
        if (expression.command == PUSH_ARRAY_INT) {
            emitIsArray(local(script, Unpack.VERSION >= 231 ? LocalDomain.STRING : LocalDomain.ARRAY, (int) expression.operand), type(expression, 0));
        }

        if (expression.command == POP_ARRAY_INT) {
            emitArrayStore(arg(expression, 1), local(script, Unpack.VERSION >= 231 ? LocalDomain.STRING : LocalDomain.ARRAY, (int) expression.operand));
        }

        if (expression.command == DEFINE_ARRAY) {
            var index = (int) expression.operand >> 16;
            var type = Type.byChar((int) expression.operand & 0xffff);
            emitEqual(local(script, Unpack.VERSION >= 231 ? LocalDomain.STRING : LocalDomain.ARRAY, index), type.array());
        }

        if (expression.command == ARRAY_COMPARE) {
            emitArrayArrayCompare(arg(expression, 0), arg(expression, 1));
        }

        if (expression.command == ARRAY_INDEXOF || expression.command == ARRAY_LASTINDEXOF || expression.command == ARRAY_COUNT) {
            emitArrayElementCompare(arg(expression, 1), arg(expression, 0));
        }

        if (expression.command == ARRAY_MIN || expression.command == ARRAY_MAX || expression.command == ARRAY_DELETE) {
            emitIsArray(arg(expression, 0), type(expression, 0));
        }

        if (expression.command == ARRAY_FILL || expression.command == ARRAY_PUSH || expression.command == ARRAY_INSERT) {
            emitArrayStore(arg(expression, 1), arg(expression, 0));
        }

        if (expression.command == ARRAY_COPY) {
            emitArrayArrayStore(arg(expression, 0), arg(expression, 1));
        }

        if (expression.command == ENUM_GETINPUTS || expression.command == ENUM_GETOUTPUTS || expression.command == ARRAY_CREATE) {
            var type = expression.arguments.get(0);
            emitEqual(type(expression, 0), Type.byChar((int) type.operand).array());
        }

        if (expression.command == ARRAY_PUSHALL || expression.command == ARRAY_INSERTALL) {
            emitArrayArrayStore(arg(expression, 1), arg(expression, 0));
        }

        // if_script
        if (expression.command == IF_RUNSCRIPT) {
            var otherScript = (int) expression.arguments.get(0).operand;
            var index = 0;

            for (var i = 3; i < expression.arguments.size() - 1; i++) {
                var arg = expression.arguments.get(i);

                for (var j = 0; j < arg.type.size(); j++) {
                    emitAssign(type(arg, j), serverParameter(otherScript, index++));
                }
            }
        }

        // equality
        if (expression.command == FLOW_EQ || expression.command == FLOW_NE) {
            emitCompare(arg(expression, 0), arg(expression, 1));
        }

        // visit children
        expression.visitChildren(c -> run(script, c));
    }

    public void visitHook(IfType.IfTypeHook hook) {
        var script = hook.id();
        for (var i = 0; i < hook.args().size(); i++) {
            if (hook.args().get(i) instanceof Integer value) {
                var type = switch (value) {
                    case Integer.MIN_VALUE + 3 -> Type.COMPONENT;
                    case Integer.MIN_VALUE + 6 -> Type.COMPONENT;
                    case Integer.MIN_VALUE + 8 -> Type.INT_KEY;
                    case Integer.MIN_VALUE + 9 -> Type.CHAR;
                    default -> Type.UNKNOWN_INT;
                };
                emitEqual(parameter(script, i), type);
            } else {
                emitEqual(parameter(script, i), Type.STRING);
            }
        }
    }

    public void finish(Set<Integer> scripts) {
        if (ScriptUnpacker.OVERRIDE_SIGNATURES) {
            // apply signature overrides
            for (var script : scripts) {
                var name = Unpacker.getScriptName(script);
                var overrides = ScriptUnpacker.SCRIPT_OVERRIDES.get(name);
                if (overrides == null) {
                    // no overrides for this script
                    continue;
                }

                // basic check for if the override is out of date. does not catch all cases.
                if (ScriptUnpacker.getParameterCount(script) != overrides.parameters().size()
                    || ScriptUnpacker.getReturnTypes(script).size() != overrides.results().size()) {
                    System.out.println("WARNING: " + name + " override is outdated. CLIENTSCRIPTS_VERSION=" + Unpack.CLIENTSCRIPTS_VERSION);
                    continue;
                }

                for (int i = 0; i < overrides.parameters().size(); i++) {
                    var override = overrides.parameters().get(i);
                    var parameter = parameter(script, i);
                    emitEqual(parameter, override);
                }

                for (int i = 0; i < overrides.results().size(); i++) {
                    var override = overrides.results().get(i);
                    var result = result(script, i);
                    emitEqual(result, override);
                }
            }
        }

        // propagate types
        propagateUntilStable();

        // merge parameters with locals
        for (var script : scripts) {
            var parameterCountInt = ScriptUnpacker.getParameterCount(script, LocalDomain.INTEGER);
            var parameterCountString = ScriptUnpacker.getParameterCount(script, LocalDomain.STRING);
            var parameterCount = parameterCountInt + parameterCountString;

            var indexInt = 0;
            var indexString = 0;

            for (var i = 0; i < parameterCount; i++) {
                var parameter = parameter(script, i);

                if (indexString == parameterCountString) { // only ints left, it's an int
                    if (Unpack.VERSION < 231 && ScriptUnpacker.SCRIPT_LEGACY_ARRAY_PARAMETER.getOrDefault(script, -1) == indexInt) {
                        emitEqual(parameter, local(script, LocalDomain.ARRAY, 0));
                        emitEqual(parameter, Type.UNKNOWNARRAY);
                    }

                    emitEqual(parameter, local(script, LocalDomain.INTEGER, indexInt++));
                    emitEqual(parameter, Type.UNKNOWN_INT);
                } else if (indexInt == parameterCountInt) { // only strings left, it's a string
                    emitEqual(parameter, local(script, LocalDomain.STRING, indexString++));
                    emitEqual(parameter, Type.UNKNOWN_OBJECT);
                } else {
                    var type = typeof(parameter);

                    if (Type.LATTICE.test(type, Type.UNKNOWN_INT)) { // inferred it's an int
                        if (Unpack.VERSION < 231 && ScriptUnpacker.SCRIPT_LEGACY_ARRAY_PARAMETER.getOrDefault(script, -1) == indexInt) {
                            emitEqual(parameter, local(script, LocalDomain.ARRAY, 0));
                            emitEqual(parameter, Type.UNKNOWNARRAY);
                        }

                        emitEqual(parameter, local(script, LocalDomain.INTEGER, indexInt++));
                        emitEqual(parameter, Type.UNKNOWN_INT);
                    } else if (Type.LATTICE.test(type, Unpack.VERSION >= 231 ? Type.UNKNOWN_OBJECT : Type.STRING)) { // inferred it's a string
                        emitEqual(parameter, local(script, LocalDomain.STRING, indexString++));
                        emitEqual(parameter, Type.UNKNOWN_OBJECT);
                    } else { // not enough info (script not called, guess int)
                        if (Unpack.VERSION < 231 && ScriptUnpacker.SCRIPT_LEGACY_ARRAY_PARAMETER.getOrDefault(script, -1) == indexInt) {
                            emitEqual(parameter, local(script, LocalDomain.ARRAY, 0));
                            emitEqual(parameter, Type.UNKNOWNARRAY);
                        }

                        emitEqual(parameter, local(script, LocalDomain.INTEGER, indexInt++));
                        emitEqual(parameter, Type.UNKNOWN_INT);
                    }
                }
            }
        }

        // propagate again with newly generated constraints
        propagateUntilStable();
//        printConnectedComponent(local(969, LocalDomain.STRING, 0));

        // output script signatures
        for (var script : scripts) {
            ScriptUnpacker.SCRIPT_PARAMETERS.put(script, IntStream.range(0, ScriptUnpacker.getParameterCount(script)).mapToObj(i -> typeof(parameter(script, i))).toList());
            ScriptUnpacker.SCRIPT_RETURNS.put(script, IntStream.range(0, ScriptUnpacker.getReturnTypes(script).size()).mapToObj(i -> typeof(result(script, i))).toList());
        }

        // assume unused is clientscript
        if (ScriptUnpacker.ASSUME_UNUSED_IS_CLIENTSCRIPT) {
            for (var script : scripts) {
                if (!ScriptUnpacker.CALLED.contains(script) && ScriptUnpacker.SCRIPT_RETURNS.get(script).isEmpty()) {
                    ScriptUnpacker.SCRIPT_TRIGGERS.put(script, ScriptTrigger.CLIENTSCRIPT);
                }
            }
        }

        // output types
        for (var node : vars.keySet()) {
            if (node instanceof Node.ExpressionType(var expression, var index)) {
                expression.type.set(index, typeof(node));
            }

            if (node instanceof Node.LocalType(var script, var domain, var index)) {
                ScriptUnpacker.SCRIPT_LOCALS.computeIfAbsent(script, _ -> new HashMap<>()).put(new LocalReference(domain, index), typeof(node));
            }

            if (node instanceof Node.VarPlayerType(var id)) {
                Unpacker.setVarPlayerType(id, ScriptUnpacker.chooseDisplayType(typeof(node)));
            }

            if (node instanceof Node.VarClientType(var id)) {
                Unpacker.setVarClientType(id, ScriptUnpacker.chooseDisplayType(typeof(node)));
            }
        }
    }

    private Type typeof(Node node) {
        return vars.getOrDefault(node, node instanceof Node.ConstantType(var t) ? t : Type.UNKNOWN);
    }

    private void emitAssign(Node a, Type b) {
        emitAssign(a, new Node.ConstantType(b));
    }

    private void emitAssign(Node a, Node b) {
        constraints.add(new Constraint(ConstraintKind.ASSIGN, a, b));
    }

    private void emitCompare(Node a, Node b) {
        constraints.add(new Constraint(ConstraintKind.COMPARE, a, b));
    }

    private void emitIsArray(Node a, Node b) {
        constraints.add(new Constraint(ConstraintKind.ISARRAY, a, b));
    }

    private void emitArrayStore(Node a, Node b) { // exists t, a < t and isarray(b, t)
        var t = new Node.TemporaryType();
        constraints.add(new Constraint(ConstraintKind.ASSIGN, a, t));
        constraints.add(new Constraint(ConstraintKind.ISARRAY, b, t));
    }

    private void emitArrayArrayStore(Node a, Node b) { // exists t u, isarray(a, t) and isarray(b, u) and t -> u
        var t = new Node.TemporaryType();
        var u = new Node.TemporaryType();
        constraints.add(new Constraint(ConstraintKind.ISARRAY, a, t));
        constraints.add(new Constraint(ConstraintKind.ISARRAY, b, t));
        constraints.add(new Constraint(ConstraintKind.ASSIGN, t, u));
    }

    private void emitArrayElementCompare(Node a, Node b) { // exists t, a ~ t and isarray(b, t)
        var t = new Node.TemporaryType();
        constraints.add(new Constraint(ConstraintKind.COMPARE, a, t));
        constraints.add(new Constraint(ConstraintKind.ISARRAY, b, t));
    }

    private void emitArrayArrayCompare(Node a, Node b) { // exists t u, isarray(a, t) and isarray(b, u) and t ~ u
        var t = new Node.TemporaryType();
        var u = new Node.TemporaryType();
        constraints.add(new Constraint(ConstraintKind.ISARRAY, a, t));
        constraints.add(new Constraint(ConstraintKind.ISARRAY, b, t));
        constraints.add(new Constraint(ConstraintKind.COMPARE, t, u));
    }

    private void emitEqual(Node a, Node b) {
        emitAssign(a, b);
        emitAssign(b, a);
    }

    private void emitEqual(Node a, Type b) {
        emitEqual(a, new Node.ConstantType(b));
    }

    // nicer syntax for nodes
    private Node parameter(int script, int index) {
        return new Node.ParameterType(script, index);
    }

    private Node serverParameter(int script, int index) {
        return new Node.ServerParameterType(script, index);
    }

    private Node result(int script, int index) {
        return new Node.ReturnType(script, index);
    }

    private Node local(int script, LocalDomain domain, int index) {
        return new Node.LocalType(script, domain, index);
    }

    private Node varplayer(int index) {
        return new Node.VarPlayerType(index);
    }

    private Node varplayerbit(int index) {
        return new Node.VarPlayerBitType(index);
    }

    private Node varclient(int index) {
        return new Node.VarClientType(index);
    }

    private Node type(Expression expression, int index) {
        return new Node.ExpressionType(expression, index);
    }

    private Node arg(Expression expression, int index) {
        for (var argument : expression.arguments) {
            if (index < argument.type.size()) {
                return type(argument, index);
            }

            index -= argument.type.size();
        }

        throw new IllegalArgumentException();
    }

    private void propagateUntilStable() {
        // build var -> incident constraints lookup table
        var incident = new HashMap<Node, Set<Constraint>>();

        for (var constraint : constraints) {
            incident.computeIfAbsent(constraint.a(), _ -> new HashSet<>()).add(constraint);
            incident.computeIfAbsent(constraint.b(), _ -> new HashSet<>()).add(constraint);
        }

        // apply local consistency rules until everything converges to a global solution
        var remaining = new LinkedHashSet<>(constraints);

        while (!remaining.isEmpty()) {
            var constraint = remaining.removeFirst();
            var kind = constraint.kind();
            var a = constraint.a();
            var b = constraint.b();
            var prevA = typeof(a);
            var prevB = typeof(b);
            var typeA = prevA;
            var typeB = prevB;

            // process the constraint
            if (kind == ConstraintKind.ASSIGN || kind == ConstraintKind.COMPARE) {
                if (typeA == typeB) {
                    // nothing to do
                } else if (typeA == Type.NAMEDOBJ && Type.LATTICE.test(typeA, typeB)) {
                    typeB = Type.OBJ;
                } else if (typeB == Type.NAMEDOBJ && Type.LATTICE.test(typeB, typeA) && kind == ConstraintKind.COMPARE) {
                    typeA = Type.OBJ; // comparison requires one to subtype the other, so only propagate obj across it
                } else {
                    var meet = Type.LATTICE.meet(typeA, typeB);

                    if (ScriptUnpacker.ERROR_ON_TYPE_CONFLICT && meet == Type.CONFLICT) {
                        System.err.println("Types " + typeA + " and " + typeB + " conflict. Paste the following into graphviz to see the data flow graph:");
                        printConnectedComponent(constraint.a());
                        throw new IllegalStateException("type conflict");
                    }

                    typeA = meet;
                    typeB = meet;
                }
            } else if (kind == ConstraintKind.ISARRAY) {
                var elementTypeA = typeA.element();

                if (elementTypeA == null) {
                    elementTypeA = typeA == Type.CONFLICT ? Type.CONFLICT : Type.UNKNOWN;
                }

                var meet = Type.LATTICE.meet(elementTypeA, typeB);

                if (ScriptUnpacker.ERROR_ON_TYPE_CONFLICT && meet == Type.CONFLICT) {
                    System.err.println("Types " + typeA + " and " + typeB + " conflict. Paste the following into graphviz to see the data flow graph:");
                    printConnectedComponent(constraint.a());
                    throw new IllegalStateException("type conflict");
                }

                typeA = meet == Type.CONFLICT ? Type.CONFLICT : meet.array();
                typeB = meet;
            }

            // update values and re-queue incident constraints that may need reprocessing
            if (prevA != typeA) {
                vars.put(a, typeA);
                remaining.addAll(incident.get(a));
            }

            if (prevB != typeB) {
                vars.put(b, typeB);
                remaining.addAll(incident.get(b));
            }
        }
    }

    private void printConnectedComponent(Node start) { // export to graphviz for debugging
        var incident = new HashMap<Node, Set<Constraint>>();

        for (var constraint : constraints) {
            incident.computeIfAbsent(constraint.a(), _ -> new HashSet<>()).add(constraint);
            incident.computeIfAbsent(constraint.b(), _ -> new HashSet<>()).add(constraint);
        }

        var componentNodes = new HashSet<Node>();
        var componentEdges = new HashSet<Constraint>();

        var queue = new ArrayDeque<Node>();
        componentNodes.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            var node = queue.removeFirst();
            var edges = incident.getOrDefault(node, Set.of());
            componentEdges.addAll(edges);

            for (var edge : edges) {
                if (componentNodes.add(edge.a())) queue.addLast(edge.a());
                if (componentNodes.add(edge.b())) queue.addLast(edge.b());
            }
        }

        var componentNodeIndices = new HashMap<Node, Integer>();
        var nextComponentNodeIndices = 0;

        System.out.println("digraph G {");

        for (var node : componentNodes) {
            var index = nextComponentNodeIndices++;
            componentNodeIndices.put(node, index);

            System.out.println("    " + index + " " + switch (node) {
                case Node.ConstantType(var t) -> "[shape=diamond,label=\"" + t + "\"]";
                case Node.ExpressionType(var e, var i) -> "[shape=box,label=\"" + e.toString().replace("\\", "\\\\").replace("\"", "\\\"") + " #" + i + "\\n" + typeof(node) + "\"]";
                case Node.LocalType(var s, var d, var i) -> "[shape=box,label=\"script_" + s + ".local" + d.name().toLowerCase() + i + "\\n" + typeof(node) + "\"]";
                case Node.ParameterType(var s, var i) -> "[shape=box,label=\"script_" + s + ".param" + i + "\\n" + typeof(node) + "\"]";
                case Node.ServerParameterType(var s, var i) -> "[shape=box,label=\"serverscript_" + s + ".param" + i + "\\n" + typeof(node) + "\"]";
                case Node.ReturnType(var s, var i) -> "[shape=box,label=\"script_" + s + ".result" + i + "\\n" + typeof(node) + "\"]";
                case Node.VarPlayerType(var i) -> "[shape=box,label=\"varplayer_" + i + "\\n" + typeof(node) + "\"]";
                case Node.VarPlayerBitType(var i) -> "[shape=box,label=\"varplayerbit_" + i + "\\n" + typeof(node) + "\"]";
                case Node.VarClientType(var i) -> "[shape=box,label=\"varclient_" + i + "\\n" + typeof(node) + "\"]";
                case Node.TemporaryType() -> "[shape=box,color=gray,fontcolor=gray,label=\"" + typeof(node) + "\"]";
            });
        }

        for (var edge : componentEdges) {
            var ia = componentNodeIndices.get(edge.a());
            var ib = componentNodeIndices.get(edge.b());

            switch (edge.kind()) {
                case ASSIGN -> System.out.println("    " + ia + " -> " + ib);
                case COMPARE -> System.out.println("    " + ia + " -> " + ib + " [dir=none,style=dashed]");
                case ISARRAY -> System.out.println("    " + ia + " -> " + ib + " [style=bold,color=blue,style=dashed]");
            }
        }

        System.out.println("}");
    }

    private sealed interface Node {
        record ConstantType(Type type) implements Node {
            public boolean equals(Object that) {
                return this == that;
            }

            public int hashCode() {
                return System.identityHashCode(this);
            }
        }

        record ExpressionType(Expression expression, int index) implements Node {}
        record LocalType(int script, Command.LocalDomain domain, int index) implements Node {}
        record ParameterType(int script, int index) implements Node {}
        record ServerParameterType(int script, int index) implements Node {}
        record ReturnType(int script, int index) implements Node {}
        record VarPlayerType(int id) implements Node {}
        record VarPlayerBitType(int id) implements Node {}
        record VarClientType(int id) implements Node {}

        record TemporaryType() implements Node {
            public boolean equals(Object that) {
                return this == that;
            }

            public int hashCode() {
                return System.identityHashCode(this);
            }
        }
    }

    private record Constraint(ConstraintKind kind, Node a, Node b) {}

    enum ConstraintKind {
        ASSIGN, // a < b
        COMPARE, // (a < b) or (b < a)
        ISARRAY, // a == array(b)
    }
}
