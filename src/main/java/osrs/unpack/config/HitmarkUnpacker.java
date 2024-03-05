package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class HitmarkUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.HITMARK, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("damagefont=" + packet.gSmart2or4s()); // todo: unused
            case 2 -> lines.add("damagecolour=" + packet.g3()); // todo: unused
            case 3 -> lines.add("classgraphic=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4s())); // todo: unused
            case 4 -> lines.add("leftgraphic=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4s())); // todo: unused
            case 5 -> lines.add("middlegraphic=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4s()));
            case 6 -> lines.add("rightgraphic=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4s())); // todo: unused
            case 7 -> lines.add("scrolltooffsetx=" + packet.g2s()); // todo: unused
            case 8 -> lines.add("damageformat=" + packet.gjstr2());
            case 9 -> lines.add("sticktime=" + packet.g2());
            case 10 -> lines.add("scrolltooffsety=" + packet.g2s()); // todo: unused
            case 11 -> lines.add("fadeout=no");
            case 12 -> lines.add("replacemode=" + packet.g1());
            case 13 -> lines.add("damageyof=" + packet.g2s());
            case 14 -> lines.add("fadeout=" + packet.g2()); // todo: unused

            case 17 -> {
                var multivarbit = packet.g2null();

                if (multivarbit != -1) {
                    lines.add("multivar=" + Unpacker.format(Type.VAR_PLAYER_BIT, multivarbit));
                }

                var multivarp = packet.g2null();

                if (multivarp != -1) {
                    lines.add("multivar=" + Unpacker.format(Type.VAR_PLAYER, multivarp));
                }

                var count = packet.g1();

                for (var i = 0; i <= count; ++i) {
                    var multi = packet.g2null();

                    if (multi != -1) {
                        lines.add("multimark=" + i + "," + Unpacker.format(Type.HITMARK, multi));
                    }
                }
            }

            case 18 -> {
                var multivarbit = packet.g2null();

                if (multivarbit != -1) {
                    lines.add("multivar=" + Unpacker.format(Type.VAR_PLAYER_BIT, multivarbit));
                }

                var multivarp = packet.g2null();

                if (multivarp != -1) {
                    lines.add("multivar=" + Unpacker.format(Type.VAR_PLAYER, multivarp));
                }

                var multidefault = packet.g2null();

                if (multidefault != -1) {
                    lines.add("multimark=default," + Unpacker.format(Type.HITMARK, multidefault));
                }

                var count = packet.g1();

                for (var i = 0; i <= count; ++i) {
                    var multi = packet.g2null();

                    if (multi != -1) {
                        lines.add("multimark=" + i + "," + Unpacker.format(Type.HITMARK, multi));
                    }
                }
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
