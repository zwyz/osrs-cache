package osrs.unpack.script;

import osrs.Unpack;
import osrs.util.Packet;

import java.util.ArrayList;

import static osrs.unpack.script.Command.*;

public class CompiledScript {
    public String name;
    public int localCountInt;
    public int localCountObject;
    public int argumentCountInt;
    public int argumentCountObject;
    public Instruction[] code;

    public static CompiledScript decode(byte[] data) {
        var packet = new Packet(data);
        var headerSize = 12;

        if (Unpack.VERSION >= 140) {
            packet.pos = packet.arr.length - 2;
            headerSize += 2 + packet.g2();
        }

        var headerPos = packet.arr.length - headerSize;
        packet.pos = headerPos;
        var script = new CompiledScript();

        script.code = new Instruction[packet.g4s()];

        script.localCountInt = packet.g2();
        script.localCountObject = packet.g2();

        script.argumentCountInt = packet.g2();
        script.argumentCountObject = packet.g2();

        int[][] switchValue = null;
        int[][] switchOffset = null;

        if (Unpack.VERSION >= 140) {
            var switchCount = packet.g1();
            switchValue = new int[switchCount][];
            switchOffset = new int[switchCount][];

            for (var i = 0; i < switchCount; i++) {
                var caseCount = packet.g2();
                switchValue[i] = new int[caseCount];
                switchOffset[i] = new int[caseCount];

                for (var j = 0; j < caseCount; j++) {
                    switchValue[i][j] = packet.g4s();
                    switchOffset[i][j] = packet.g4s();
                }
            }
        }

        packet.pos = 0;
        script.name = packet.gjstrnull();
        var index = 0;

        while (packet.pos < headerPos) {
            var command = byId(packet.g2());
            script.code[index++] = new Instruction(command, decodeOperand(command, packet, index, switchValue, switchOffset));
        }

        return script;
    }

    private static Object decodeOperand(Command command, Packet packet, int index, int[][] switchValue, int[][] switchOffset) {
        // (command < 100 && command != 21 && command != 38 && command != 39) {
        if (command == PUSH_CONSTANT_INT) {
            return packet.g4s(); // int
        } else if (command == PUSH_VAR || command == POP_VAR) {
            return packet.g4s(); // varplayer
        } else if (command == PUSH_VARBIT || command == POP_VARBIT) {
            return packet.g4s(); // varplayerbit
        } else if (command == PUSH_VARC_INT || command == POP_VARC_INT || command == PUSH_VARC_STRING || command == POP_VARC_STRING) {
            return packet.g4s(); // varclient
        } else if (command == PUSH_VARC_STRING_OLD || command == POP_VARC_STRING_OLD) {
            return packet.g4s(); // varclientstring
        } else if (command == PUSH_VARCLANSETTING) {
            return packet.g4s(); // varclansetting
        } else if (command == PUSH_VARCLAN) {
            return packet.g4s(); // varclan
        } else if (command == PUSH_CONSTANT_STRING) {
            return packet.gjstr();
        } else if (command == BRANCH || command == BRANCH_NOT || command == BRANCH_EQUALS || command == BRANCH_LESS_THAN || command == BRANCH_GREATER_THAN || command == BRANCH_LESS_THAN_OR_EQUALS || command == BRANCH_GREATER_THAN_OR_EQUALS) {
            return index + packet.g4s(); // branch
        } else if (command == PUSH_INT_LOCAL || command == POP_INT_LOCAL || command == PUSH_STRING_LOCAL || command == POP_STRING_LOCAL) {
            return packet.g4s(); // local
        } else if (command == JOIN_STRING) {
            return packet.g4s(); // count
        } else if (command == GOSUB_WITH_PARAMS) {
            return packet.g4s(); // script
        } else if (command == DEFINE_ARRAY || command == PUSH_ARRAY_INT || command == POP_ARRAY_INT) {
            return packet.g4s(); // array
        } else if (command == SWITCH) {
            var i = packet.g4s();
            var operand = new ArrayList<SwitchCase>();

            for (var j = 0; j < switchValue[i].length; j++) {
                operand.add(new SwitchCase(switchValue[i][j], index + switchOffset[i][j]));
            }

            return operand; // value-branch map
        }

        return packet.g1();
    }

}
