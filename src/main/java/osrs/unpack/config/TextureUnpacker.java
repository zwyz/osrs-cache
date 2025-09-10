package osrs.unpack.config;

import osrs.Unpack;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class TextureUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.MATERIAL, id, false) + "]");

        if (Unpack.VERSION >= 233) {
            lines.add("sprite=" + Unpacker.format(Type.GRAPHIC, packet.g2null()));
            lines.add("averagecolour=" + packet.g2());
            lines.add("opaque=" + (packet.g1() == 1 ? "yes" : "no"));
            lines.add("animation=" + packet.g1() + "," + packet.g1());
        } else {
            lines.add("averagecolour=" + packet.g2());
            lines.add("opaque=" + (packet.g1() == 1 ? "yes" : "no"));

            if (packet.g1() != 1) {
                throw new IllegalStateException("not supported");
            }

            lines.add("sprite=" + Unpacker.format(Type.GRAPHIC, packet.g2null()));
            lines.add("unknown1=" + packet.g4s());
            lines.add("animation=" + packet.g1() + "," + packet.g1());
        }

        if (packet.pos != packet.arr.length) {
            throw new IllegalStateException("didn't reach end of file");
        }

        return lines;
    }
}
