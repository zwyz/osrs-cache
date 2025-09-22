package osrs.unpack.script;

import osrs.unpack.ScriptTrigger;
import osrs.unpack.Type;

import java.util.List;

import static osrs.unpack.script.Command.PUSH_CONSTANT_INT;

public class TriggerInference {
    public void run(int script, List<Expression> expressions) {
        for (var expression : expressions) {
            run(script, expression);
        }
    }

    public void run(int script, Expression expression) {
        if (expression.command == PUSH_CONSTANT_INT) {
            // push_constant_int should have exactly 1 type, always
            Type type = expression.type.getFirst();
            if (type == Type.GCLIENTCLICKNPC) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.GCLIENTCLICKNPC);
            } else if (type == Type.GCLIENTCLICKLOC) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.GCLIENTCLICKLOC);
            } else if (type == Type.GCLIENTCLICKOBJ) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.GCLIENTCLICKOBJ);
            } else if (type == Type.GCLIENTCLICKPLAYER) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.GCLIENTCLICKPLAYER);
            } else if (type == Type.GCLIENTCLICKTILE) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.GCLIENTCLICKTILE);
            }
        }

        // visit children
        expression.visitChildren(c -> run(script, c));
    }
}