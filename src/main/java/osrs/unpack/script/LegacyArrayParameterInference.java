package osrs.unpack.script;

import java.util.*;

import static osrs.unpack.script.Command.*;

public class LegacyArrayParameterInference {
    private final Set<Integer> arrayUsages = new LinkedHashSet<>();
    private final Map<Integer, Set<Integer>> conditionalArrayUsages = new LinkedHashMap<>();

    public void run(Set<Integer> scripts) {
        // build initial set of array usages + conditional array usages
        for (var script : scripts) {
            buildArrayGraphBlock(script, ScriptUnpacker.SCRIPTS_DECOMPILED.get(script));
        }

        // propagate array usages through conditional array usages
        var queue = new ArrayDeque<>(arrayUsages);

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();

            for (var other : conditionalArrayUsages.getOrDefault(current, Set.of())) {
                if (!arrayUsages.contains(other)) {
                    arrayUsages.add(other);
                    queue.add(other);
                }
            }
        }

        // set the types of scripts using arrays
        for (var script : arrayUsages) {
            // guess which parameter is the array (first unused parameter, can be improved)
            var possible = new LinkedHashSet<Integer>();

            for (int i = 0; i < ScriptUnpacker.getParameterCount(script); i++) {
                possible.add(i);
            }

            var expressions = ScriptUnpacker.SCRIPTS_DECOMPILED.get(script);

            for (var expression : expressions) {
                removeUsedParameters(expression, possible);
            }

            ScriptUnpacker.SCRIPT_LEGACY_ARRAY_PARAMETER.put(script, possible.getFirst());
        }
    }

    private void removeUsedParameters(Expression expression, Set<Integer> unused) {
        if (expression.command == FLOW_LOAD) {
            if (expression.operand instanceof Command.LocalReference local) {
                if (local.domain() == Command.LocalDomain.INTEGER) {
                    unused.remove(local.local());
                }
            }
        }

        expression.visitChildren(child -> removeUsedParameters(child, unused));
    }

    private void buildArrayGraphBlock(int script, List<Expression> expressions) {
        for (var expression : expressions) {
            if (buildArrayGraphExpression(script, expression)) {
                break; // an array was defined, no need to check the rest
            }
        }
    }

    private boolean buildArrayGraphExpression(int script, Expression expression) {
        if (expression.command == DEFINE_ARRAY) {
            return true;
        }

        if (expression.command == PUSH_ARRAY_INT) {
            if ((int) expression.operand == 0) {
                // current script requires an array parameter
                arrayUsages.add(script);
            }
        }

        if (expression.command == POP_ARRAY_INT) {
            if ((int) expression.operand == 0) {
                // current script requires an array parameter
                arrayUsages.add(script);
            }
        }

        if (expression.command == GOSUB_WITH_PARAMS) {
            // if called script requires an array parameter, so does current script
            conditionalArrayUsages.computeIfAbsent((int) expression.operand, _ -> new LinkedHashSet<>()).add(script);
        }

        // arguments can't define an array
        for (var argument : expression.arguments) {
            buildArrayGraphExpression(script, argument);
        }

        // blocks run in a new scope for each
        if (expression.command == FLOW_IF) {
            buildArrayGraphBlock(script, (List<Expression>) expression.operand);
        }

        if (expression.command == FLOW_IFELSE) {
            buildArrayGraphBlock(script, ((IfElseBranches) expression.operand).trueBranch());
            buildArrayGraphBlock(script, ((IfElseBranches) expression.operand).falseBranche());
        }

        if (expression.command == FLOW_WHILE) {
            buildArrayGraphBlock(script, (List<Expression>) expression.operand);
        }

        if (expression.command == FLOW_SWITCH) {
            for (var branch : ((List<SwitchBranch>) expression.operand)) {
                buildArrayGraphBlock(script, branch.branch());
            }
        }

        return false;
    }
}
