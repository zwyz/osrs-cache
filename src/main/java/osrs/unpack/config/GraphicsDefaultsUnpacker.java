package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class GraphicsDefaultsUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[graphicsdefaults_" + id + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("unknown1=" + packet.g3());

            case 2 -> {
                var mapcompass = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var maparrow = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var mapscene = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var headiconskull = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var headiconprayer = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var headiconhint = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var mapmarker = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var cross = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var mapdot = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var scrollbar = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                var texticon = Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null());
                lines.add("sprites=" + mapcompass + "," + maparrow + "," + mapscene + "," + headiconskull + "," + headiconprayer + "," + headiconhint + "," + mapmarker + "," + cross + "," + mapdot + "," + scrollbar + "," + texticon);
            }
            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
