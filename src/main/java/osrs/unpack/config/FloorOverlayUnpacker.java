package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class FloorOverlayUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.OVERLAY, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("colour=0x" + Integer.toHexString(packet.g3()));
            case 2 -> lines.add("material=" + Unpacker.format(Type.MATERIAL, packet.g1()));
            case 5 -> lines.add("occlude=no");
            case 7 -> lines.add("mapcolour=0x" + Integer.toHexString(packet.g3()));
            case 8 -> lines.add("unknown8=yes");
            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
