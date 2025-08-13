package osrs.unpack.config;

import osrs.unpack.ColourConversion;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class EffectAnimUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.SPOTANIM, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("model=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 2 -> lines.add("anim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 4 -> lines.add("resizeh=" + packet.g2());
            case 5 -> lines.add("resizev=" + packet.g2());
            case 6 -> lines.add("rotation=" + packet.g2());
            case 7 -> lines.add("ambient=" + packet.g1());
            case 8 -> lines.add("contrast=" + packet.g1());
            case 9 -> packet.gjstr(); // debugname, only exists in 230, removed in 231

            case 40 -> {
                var count = packet.g1();

                for (var i = 0; i < count; i++) {
                    lines.add("recol" + (i + 1) + "s=" + ColourConversion.reverseRGBFromHSL(packet.g2()));
                    lines.add("recol" + (i + 1) + "d=" + ColourConversion.reverseRGBFromHSL(packet.g2()));
                }
            }

            case 41 -> {
                var count = packet.g1();

                for (var i = 0; i < count; i++) {
                    lines.add("retex" + (i + 1) + "s=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                    lines.add("retex" + (i + 1) + "d=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                }
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
