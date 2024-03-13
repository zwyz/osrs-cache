package osrs.unpack.config;

import osrs.Unpack;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class LocUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.LOC, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> {
                var count = packet.g1();

                for (var i = 0; i < count; ++i) {
                    lines.add("model=" + Unpacker.format(Type.MODEL, packet.g2()) + "," + Unpacker.format(Type.LOC_SHAPE, packet.g1()));
                }
            }

            case 2 -> lines.add("name=" + packet.gjstr()); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00

            case 5 -> {
                var count = packet.g1();

                for (var i = 0; i < count; ++i) {
                    lines.add("model=" + Unpacker.format(Type.MODEL, packet.g2())); // https://www.youtube.com/watch?v=vZ7oG1IDz1w 2:09:30
                }
            }

            case 14 -> lines.add("width=" + packet.g1()); // https://www.youtube.com/watch?v=vZ7oG1IDz1w
            case 15 -> lines.add("length=" + packet.g1()); // https://www.youtube.com/watch?v=vZ7oG1IDz1w
            case 17 -> lines.add("blockwalk=no"); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 18 -> lines.add("blockrange=no"); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 19 -> lines.add("active=" + Unpacker.getBooleanName(packet.g1())); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 21 -> lines.add("hillskew=yes"); //  https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 22 -> lines.add("sharelight=yes"); //  https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 23 -> lines.add("occlude=yes"); // https://www.youtube.com/watch?v=vZ7oG1IDz1w 2:09:30
            case 24 -> lines.add("anim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 27 -> lines.add("blockwalk=yes"); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 28 -> lines.add("wallwidth=" + packet.g1()); // * https://discord.com/channels/@me/698790755363323904/1131401170045374545
            case 29 -> lines.add("ambient=" + packet.g1s()); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 39 -> lines.add("contrast=" + packet.g1s()); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 30 -> lines.add("op1=" + packet.gjstr()); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 31 -> lines.add("op2=" + packet.gjstr()); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 32 -> lines.add("op3=" + packet.gjstr()); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 33 -> lines.add("op4=" + packet.gjstr()); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00
            case 34 -> lines.add("op5=" + packet.gjstr()); // https://www.youtube.com/watch?v=ovGBifJR4Fs 4:38:00

            case 40 -> { // https://www.youtube.com/watch?v=vZ7oG1IDz1w 2:09:30
                var count = packet.g1();

                for (var i = 0; i < count; ++i) {
                    lines.add("recol" + (i + 1) + "s=" + packet.g2());
                    lines.add("recol" + (i + 1) + "d=" + packet.g2());
                }
            }

            case 41 -> { // https://www.youtube.com/watch?v=vZ7oG1IDz1w 2:09:30
                var count = packet.g1();

                for (var i = 0; i < count; ++i) {
                    lines.add("retex" + (i + 1) + "s=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                    lines.add("retex" + (i + 1) + "d=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                }
            }

            case 60 -> lines.add("mapfunction=" + packet.g2());
            case 61 -> lines.add("category=" + Unpacker.format(Type.CATEGORY, packet.g2()));
            case 62 -> lines.add("mirror=yes");
            case 64 -> lines.add("shadow=no"); // https://www.youtube.com/watch?v=vZ7oG1IDz1w
            case 65 -> lines.add("resizex=" + packet.g2());
            case 66 -> lines.add("resizey=" + packet.g2());
            case 67 -> lines.add("resizez=" + packet.g2());
            case 68 -> lines.add("mapscene=" + packet.g2());

            case 69 -> { // https://twitter.com/JagexAsh/status/1641051532010434560
                int blocked = packet.g1s();
                var result = new ArrayList<String>();

                if ((blocked & 1) == 0) result.add("north");
                if ((blocked & 2) == 0) result.add("east");
                if ((blocked & 4) == 0) result.add("south");
                if ((blocked & 8) == 0) result.add("west");

                if (blocked >>> 4 != 0) {
                    throw new IllegalStateException("invalid blocked: " + blocked);
                }

                lines.add("forceapproach=" + String.join(",", result));
            }

            case 70 -> lines.add("offsetx=" + packet.g2s());
            case 71 -> lines.add("offsety=" + packet.g2s());
            case 72 -> lines.add("offsetz=" + packet.g2s());
            case 73 -> lines.add("forcedecor=yes");
            case 74 -> lines.add("breakroutefinding=yes"); // https://twitter.com/JagexAsh/status/1443150721734660096
            case 75 -> lines.add("raiseobject=" + Unpacker.getBooleanName(packet.g1())); // https://twitter.com/JagexAsh/status/1641051532010434560

            case 77 -> { // * https://twitter.com/JagexAsh/status/737426310545481728
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
                        lines.add("multiloc=" + i + "," + Unpacker.format(Type.LOC, multi));
                    }
                }
            }

            case 78 -> { // https://twitter.com/JagexAsh/status/1651904693671546881
                if (Unpack.VERSION < 220) {
                    lines.add("bgsound=" + Unpacker.format(Type.SYNTH, packet.g2()) + "," + packet.g1());
                } else {
                    lines.add("bgsound=" + Unpacker.format(Type.SYNTH, packet.g2()) + "," + packet.g1() + "," + packet.g1());
                }
            }

            case 79 -> { // https://twitter.com/JagexAsh/status/1651904693671546881
                if (Unpack.VERSION < 220) {
                    var line = "randomsound=" + packet.g2() + "," + packet.g2() + "," + packet.g1();
                    var count = packet.g1();

                    for (var i = 0; i < count; ++i) {
                        line += "," + Unpacker.format(Type.SYNTH, packet.g2());
                    }

                    lines.add(line);
                } else {
                    var line = "randomsound=" + packet.g2() + "," + packet.g2() + "," + packet.g1() + "," + packet.g1();
                    var count = packet.g1();

                    for (var i = 0; i < count; ++i) {
                        line += "," + Unpacker.format(Type.SYNTH, packet.g2());
                    }

                    lines.add(line);
                }
            }

            case 81 -> lines.add("treeskew=" + packet.g1()); // todo: unused
            case 82 -> lines.add("mapicon=" + Unpacker.format(Type.MAPELEMENT, packet.g2()));
            case 89 -> lines.add("randseq=no");

            case 92 -> {
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
                    lines.add("multiloc=default," + Unpacker.format(Type.LOC, multidefault));
                }

                var count = packet.g1();

                for (var i = 0; i <= count; ++i) {
                    var multi = packet.g2null();

                    if (multi != -1) {
                        lines.add("multiloc=" + i + "," + Unpacker.format(Type.LOC, multi));
                    }
                }
            }

            case 249 -> {
                var count = packet.g1();

                for (var i = 0; i < count; i++) {
                    if (packet.g1() == 1) {
                        lines.add("param=" + Unpacker.format(Type.PARAM, packet.g3()) + "," + packet.gjstr());
                    } else {
                        var param = packet.g3();
                        lines.add("param=" + Unpacker.format(Type.PARAM, param) + "," + Unpacker.format(Unpacker.getParamType(param), packet.g4s()));
                    }
                }
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
