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
            if (type == Type.ONCLIENTOPNPC) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONCLIENTOPNPC);
            } else if (type == Type.ONCLIENTOPLOC) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONCLIENTOPLOC);
            } else if (type == Type.ONCLIENTOPOBJ) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONCLIENTOPOBJ);
            } else if (type == Type.ONCLIENTOPPLAYER) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONCLIENTOPPLAYER);
            } else if (type == Type.ONCLIENTOPTILE) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.ONCLIENTOPTILE);
            }
        }

        // visit children
        expression.visitChildren(c -> run(script, c));
    }
}