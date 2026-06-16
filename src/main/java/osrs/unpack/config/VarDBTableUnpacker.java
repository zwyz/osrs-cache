package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class VarDBTableUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.VARDBTABLE, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 7 -> unpackColumnSet(packet, lines, "column", "default");
            case 8 -> unpackColumnSet(packet, lines, "secondarycolumn", "secondarydefault");

            default -> throw new IllegalStateException("unknown opcode");
        }
    }

    private static void unpackColumnSet(Packet packet, ArrayList<String> lines, String fieldLabel, String defaultLabel) {
        var count = packet.g2();

        for (var i = 0; i < count; i++) {
            var name = "col" + i;
            var typeID = packet.g1();

            if (typeID >= 252) {
                typeID = (typeID - 252 << 8) + packet.g1();
            }

            var type = Type.byID(typeID);
            lines.add(fieldLabel + "=" + name + "," + type);

            lines.add(defaultLabel + "=" + name + "," + switch (type.base) {
                case INTEGER -> Unpacker.format(type, packet.g4s());
                case LONG -> Long.toString(packet.g8s());
                case STRING -> packet.gjstr();
                default -> throw new IllegalStateException("invalid");
            });
        }
    }
}
