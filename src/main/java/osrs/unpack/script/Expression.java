package osrs.unpack.script;

import osrs.unpack.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static osrs.unpack.script.Command.*;

public class Expression {
    public final Command command;
    public final Object operand;
    public final List<Type> type;
    public final List<Expression> arguments;

    public Expression(Command command, Object operand, List<Type> type, List<Expression> arguments) {
        this.command = command;
        this.operand = operand;
        this.type = new ArrayList<>(type);
        this.arguments = arguments;
    }

    public String toString() {
        return CodeFormatter.format(this);
    }

    public void visitChildren(Consumer<Expression> consumer) {
        for (var argument : arguments) {
            consumer.accept(argument);
        }

        if (command == FLOW_IF) {
            for (var expression : ((List<Expression>) operand)) {
                consumer.accept(expression);
            }
        }

        if (command == FLOW_IFELSE) {
            for (var expression : ((IfElseBranches) operand).trueBranch()) {
                consumer.accept(expression);
            }

            for (var expression : ((IfElseBranches) operand).falseBranche()) {
                consumer.accept(expression);
            }
        }

        if (command == FLOW_WHILE) {
            for (var expression : ((List<Expression>) operand)) {
                consumer.accept(expression);
            }
        }

        if (command == FLOW_SWITCH) {
            for (var branch : ((List<SwitchBranch>) operand)) {
                for (var expression : branch.branch()) {
                    consumer.accept(expression);
                }
            }
        }
    }
}
