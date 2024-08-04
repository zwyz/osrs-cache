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
            if (type == Type.ONSHIFTCLICKNPC) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONSHIFTCLICKNPC);
            } else if (type == Type.ONSHIFTCLICKLOC) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONSHIFTCLICKLOC);
            } else if (type == Type.ONSHIFTCLICKOBJ) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONSHIFTCLICKOBJ);
            } else if (type == Type.ONSHIFTCLICKPLAYER) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONSHIFTCLICKPLAYER);
            } else if (type == Type.ONSHIFTCLICKTILE) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONSHIFTCLICKTILE);
            }
        }

        // visit children
        expression.visitChildren(c -> run(script, c));
    }
}