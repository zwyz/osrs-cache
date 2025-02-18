package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class WorldEntityUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.WORLDENTITY, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 2 -> lines.add("unknown2=" + packet.g1());
            case 4 -> lines.add("unknown4=" + packet.g2s());
            case 5 -> lines.add("unknown5=" + packet.g2s());
            case 6 -> lines.add("unknown6=" + packet.g2s());
            case 7 -> lines.add("unknown7=" + packet.g2s());
            case 8 -> lines.add("unknown8=" + packet.g2());
            case 9 -> lines.add("unknown9=" + packet.g2());
            case 12 -> lines.add("unknown12=" + packet.gjstr());
            case 14 -> lines.add("unknown14=yes");
            case 15 -> lines.add("op1=" + packet.gjstr());
            case 16 -> lines.add("op2=" + packet.gjstr());
            case 17 -> lines.add("op3=" + packet.gjstr());
            case 18 -> lines.add("op4=" + packet.gjstr());
            case 19 -> lines.add("op5=" + packet.gjstr());
            case 20 -> lines.add("unknown20=" + packet.g2());

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
