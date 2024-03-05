package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class IDKUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.IDKIT, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("bodypart=" + packet.g1());

            case 2 -> {
                var modelCount = packet.g1();

                for (var i = 0; i < modelCount; ++i) {
                    lines.add("model=" + Unpacker.format(Type.MODEL, packet.g2()));
                }
            }

            case 3 -> lines.add("disable=yes");

            case 40 -> {
                var count = packet.g1();

                for (var i = 0; i < count; ++i) {
                    lines.add("recol" + (i + 1) + "s=" + packet.g2());
                    lines.add("recol" + (i + 1) + "d=" + packet.g2());
                }
            }

            case 41 -> {
                var count = packet.g1();

                for (var i = 0; i < count; ++i) {
                    lines.add("retex" + (i + 1) + "s=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                    lines.add("retex" + (i + 1) + "d=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                }
            }

            case 60 -> lines.add("head1=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 61 -> lines.add("head2=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 62 -> lines.add("head3=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 63 -> lines.add("head4=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 64 -> lines.add("head5=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 65 -> lines.add("head6=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 66 -> lines.add("head7=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 67 -> lines.add("head8=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 68 -> lines.add("head9=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 69 -> lines.add("head10=" + Unpacker.format(Type.MODEL, packet.g2()));
            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
