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
            if (type == Type.CLIENTOPNPC) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.CLIENTOPNPC);
            } else if (type == Type.CLIENTOPLOC) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.CLIENTOPLOC);
            } else if (type == Type.CLIENTOPOBJ) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.CLIENTOPOBJ);
            } else if (type == Type.CLIENTOPPLAYER) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.CLIENTOPPLAYER);
            } else if (type == Type.CLIENTOPTILE) {
                ScriptUnpacker.SCRIPT_TRIGGERS.put((int)expression.operand, ScriptTrigger.CLIENTOPTILE);
            }
        }

        // visit children
        expression.visitChildren(c -> run(script, c));
    }
}