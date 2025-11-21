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
        lines.add("[" + Unpacker.format(Type.WORLDENTITY, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 2 -> lines.add("mainlevel=" + packet.g1());
            case 4 -> lines.add("mainx=" + packet.g2s());
            case 5 -> lines.add("mainz=" + packet.g2s());
            case 6 -> lines.add("boundsoffsetx=" + packet.g2s());
            case 7 -> lines.add("boundsoffsetz=" + packet.g2s());
            case 8 -> lines.add("boundssizex=" + packet.g2());
            case 9 -> lines.add("boundssizez=" + packet.g2());
            case 12 -> lines.add("name=" + packet.gjstr());
            case 14 -> lines.add("active=yes");
            case 15 -> lines.add("op1=" + packet.gjstr());
            case 16 -> lines.add("op2=" + packet.gjstr());
            case 17 -> lines.add("op3=" + packet.gjstr());
            case 18 -> lines.add("op4=" + packet.gjstr());
            case 19 -> lines.add("op5=" + packet.gjstr());
            case 20 -> lines.add("category=" + Unpacker.format(Type.CATEGORY, packet.g2()));

            case 23 -> lines.add("interacttarget=" + switch (packet.g1()) {
                case 0 -> "world";
                case 1 -> "contents";
                case 2 -> "both";
                case 3 -> "none";
                default -> throw new IllegalStateException("invalid interact target");
            });

            case 24 -> lines.add("interactcontentsmode=" + switch (packet.g1()) {
                case 1 -> "none";
                case 2 -> "examine";
                case 3 -> "all";
                default -> throw new IllegalStateException("invalid interact mode");
            });

            case 25 -> lines.add("anim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 26 -> lines.add("minimapicon=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null()));
            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
