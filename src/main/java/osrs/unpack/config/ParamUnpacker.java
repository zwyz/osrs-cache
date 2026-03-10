package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class ParamUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.PARAM, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> {
                var type = Type.byChar(packet.g1());
                Unpacker.setParamType(id, type);
                lines.add("type=" + type.name);
            }

            case 2 -> lines.add("default=" + Unpacker.format(Unpacker.getParamType(id), packet.g4s()));
            case 4 -> lines.add("autodisable=no");
            case 5 -> lines.add("default=" + packet.gjstr());
            case 7 -> lines.add("default=" + packet.g8s());

            case 8 -> {
                var type = Type.byID(packet.g1());
                Unpacker.setParamType(id, type);
                lines.add("type=" + type.name);
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
