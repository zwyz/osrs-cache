package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class MapElementUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.MAPELEMENT, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("graphic=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4s()));
            case 2 -> lines.add("unknown2=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4s()));
            case 3 -> lines.add("text=" + packet.gjstr());
            case 4 -> lines.add("colour=" + packet.g3());
            case 5 -> lines.add("unknown5=" + packet.g3());
            case 6 -> lines.add("size=" + packet.g1());
            case 7 -> lines.add("vis=" + packet.g1());
            case 8 -> lines.add("mapfunction=" + Unpacker.getBooleanName(packet.g1()));
            case 10 -> lines.add("op1=" + packet.gjstr());
            case 11 -> lines.add("op2=" + packet.gjstr());
            case 12 -> lines.add("op3=" + packet.gjstr());
            case 13 -> lines.add("op4=" + packet.gjstr());
            case 14 -> lines.add("op5=" + packet.gjstr());

            case 15 -> {
                var count = packet.g1();
                var unknown15_a = new int[count * 2];

                for (var i = 0; i < count * 2; ++i) {
                    unknown15_a[i] = packet.g2s();
                }

                var unknown15_b = packet.g4s();
                var unknown15_c = new int[packet.g1()];

                for (var i = 0; i < unknown15_c.length; ++i) {
                    unknown15_c[i] = packet.g4s();
                }

                var unknown15_d = new byte[count];

                for (var i = 0; i < count; ++i) {
                    unknown15_d[i] = (byte) packet.g1s();
                }

                lines.add("{data}");
            }

            case 16 -> lines.add("unknown16=yes");
            case 17 -> lines.add("unknown17=" + packet.gjstr());
            case 18 -> lines.add("unknown18=" + packet.gSmart2or4s());
            case 19 -> lines.add("category=" + Unpacker.format(Type.CATEGORY, packet.g2()));
            case 21 -> lines.add("unknown21=" + packet.g4s());
            case 22 -> lines.add("unknown22=" + packet.g4s());
            case 23 -> lines.add("unknown23=" + packet.g1() + "," + packet.g1() + "," + packet.g1());
            case 24 -> lines.add("unknown24=" + packet.g2s() + "," + packet.g2s());
            case 25 -> lines.add("unknown25=" + packet.gSmart2or4s());
            case 28 -> lines.add("unknown28=" + packet.g1());
            case 29 -> lines.add("unknown29=" + packet.g1());
            case 30 -> lines.add("unknown30=" + packet.g1());

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
