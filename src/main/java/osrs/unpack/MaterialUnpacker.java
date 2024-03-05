package osrs.unpack;

import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class MaterialUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.MATERIAL, id) + "]");

        lines.add("averagecolour=" + packet.g2());
        lines.add("opaque=" + (packet.g1() == 1 ? "yes" : "no"));

        if (packet.g1() != 1) {
            throw new IllegalStateException("not supported");
        }

        lines.add("sprite=" + Unpacker.format(Type.GRAPHIC, packet.g2null()));
        lines.add("unknown1=" + packet.g4s());
        lines.add("animation=" + packet.g1() + "," + packet.g1());

        if (packet.pos != packet.arr.length) {
            throw new IllegalStateException("didn't reach end of file");
        }

        return lines;
    }
}
