package osrs.unpack;

import osrs.Unpack;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class MapAreaUnpacker {
    public static List<String> unpack(int id, byte[] file) {
        var packet = new Packet(file);
        var lines = new ArrayList<String>();

        var debugname = packet.gjstr();
        Unpacker.setSymbolName(Type.MAPAREA, id, debugname);
        lines.add("[" + Unpacker.format(Type.MAPAREA, id, false) + "]");

        lines.add("name=" + packet.gjstr());
        lines.add("origin=" + Unpacker.format(Type.COORDGRID, packet.g4s()));
        lines.add("backgroundcolour=" + Unpacker.formatColour(packet.g4s()));
        if (Unpack.VERSION >= 217) {
            lines.add("fillcolour=" + Unpacker.formatColour(packet.g4s()));
        }
        lines.add("unknown=" + packet.g1());
        lines.add("defaultmap=" + (packet.g1() == 1));
        lines.add("zoom=" + packet.g1());

        var count = packet.g1();

        for (var i = 0; i < count; i++) {
            var type = packet.g1();
            switch (type) {
                case 0 -> lines.add("subarea=multisquare," + packet.g1() + "," + packet.g1() + "," + packet.g2() + "," + packet.g2() + "," + packet.g2() + "," + packet.g2() + "," + packet.g2() + "," + packet.g2() + "," + packet.g2() + "," + packet.g2());
                case 1 -> lines.add("subarea=singlesquare," + packet.g1() + "," + packet.g1() + "," + packet.g2() + "," + packet.g2() + "," + packet.g2() + "," + packet.g2());
                case 2 -> lines.add("subarea=multizone," + packet.g1() + "," + packet.g1() + "," + packet.g2() + "," + packet.g1() + "," + packet.g1() + "," + packet.g2() + "," + packet.g1() + "," + packet.g1() + "," + packet.g2() + "," + packet.g1() + "," + packet.g1() + "," + packet.g2() + "," + packet.g1() + "," + packet.g1());
                case 3 -> lines.add("subarea=singlezone," + packet.g1() + "," + packet.g1() + "," + packet.g2() + "," + packet.g1() + "," + packet.g2() + "," + packet.g1() + "," + packet.g2() + "," + packet.g1() + "," + packet.g2() + "," + packet.g1());
                default -> throw new AssertionError();
            }
        }

        return lines;
    }
}
