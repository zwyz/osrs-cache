package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class EnumUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.ENUM, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> {
                var type = packet.g1();
                Unpacker.setEnumInputType(id, Type.byChar(type));
                lines.add("inputtype=" + Unpacker.format(Type.TYPE, type));
            }

            case 2 -> {
                var type = packet.g1();
                Unpacker.setEnumOutputType(id, Type.byChar(type));
                lines.add("outputtype=" + Unpacker.format(Type.TYPE, type));
            }

            case 3 -> lines.add("default=" + packet.gjstr());
            case 4 -> lines.add("default=" + Unpacker.format(Unpacker.getEnumOutputType(id), packet.g4s()));

            case 5 -> {
                var count = packet.g2();

                for (var i = 0; i < count; ++i) {
                    lines.add("val=" + Unpacker.format(Unpacker.getEnumInputType(id), packet.g4s()) + "," + packet.gjstr());
                }
            }

            case 6 -> {
                var count = packet.g2();

                for (var i = 0; i < count; ++i) {
                    lines.add("val=" + Unpacker.format(Unpacker.getEnumInputType(id), packet.g4s()) + "," + Unpacker.format(Unpacker.getEnumOutputType(id), packet.g4s()));
                }
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
