package osrs.unpack;

import osrs.util.Packet;

import java.util.ArrayList;

public class WorldAreaUnpacker {
    public static ArrayList<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.WORLD_AREA, id) + "]");
        lines.add("colour=" + Unpacker.formatColour(packet.g3()));

        while (packet.pos < packet.arr.length) {
            switch (packet.g1()) {
                case 1 -> lines.add("impostorsquare=" + Unpacker.format(Type.COORDGRID, packet.g4s()) + "," + Unpacker.format(Type.COORDGRID, packet.g4s()));
                case 2 -> lines.add("impostorzone=" + Unpacker.format(Type.COORDGRID, packet.g4s()) + "," + formatTemplateZone(packet.g4s()));
                default -> throw new IllegalStateException("unknown opcode");
            }
        }

        return lines;
    }

    public static String formatTemplateZone(int value) {
        if (value >>> 26 != 0) {
            throw new IllegalStateException("invalid template zone " + value);
        }

        var level = (value >> 24) & 0x3;
        var x = ((value >> 14) & 0x3ff) * 8;
        var z = ((value >> 3) & 0x7ff) * 8;
        var angle = (value >> 1) & 0x3;
        var unknown = value & 1;

        return level + "_" + (x / 64) + "_" + (z / 64) + "_" + (x % 64) + "_" + (z % 64) + "," + angle + "," + unknown;
    }
}
