package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class HeadbarUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.HEADBAR, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("unknown1=" + packet.g2()); // todo: unused
            case 2 -> lines.add("showpriority=" + packet.g1());
            case 3 -> lines.add("hidepriority=" + packet.g1());
            case 4 -> lines.add("fadeout=no"); // https://twitter.com/JagexAsh/status/1654124199194288137
            case 5 -> lines.add("sticktime=" + packet.g2()); // https://twitter.com/JagexAsh/status/1654124199194288137
            case 6 -> lines.add("unknown6=" + packet.g1()); // todo: unused
            case 7 -> lines.add("full=" + Unpacker.format(Type.GRAPHIC, packet.g2s())); // https://twitter.com/JagexAsh/status/1654124199194288137
            case 8 -> lines.add("empty=" + Unpacker.format(Type.GRAPHIC, packet.g2s())); // https://twitter.com/JagexAsh/status/1654124199194288137
            case 11 -> lines.add("fadeout=" + packet.g2()); // https://twitter.com/JagexAsh/status/1654124199194288137
            case 14 -> lines.add("segments=" + packet.g1()); // https://twitter.com/JagexAsh/status/1654124199194288137
            case 15 -> lines.add("padding=" + packet.g1()); // todo: unused
            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
