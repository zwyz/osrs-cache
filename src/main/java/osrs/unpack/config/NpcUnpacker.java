package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class NpcUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.NPC, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> { // https://discord.com/channels/@me/698790755363323904/1203639168836833340
                var count = packet.g1();

                for (var i = 0; i < count; i++) {
                    lines.add("model=" + Unpacker.format(Type.MODEL, packet.g2()));
                }
            }

            case 2 -> lines.add("name=" + packet.gjstr()); // https://discord.com/channels/@me/698790755363323904/1203639168836833340
            case 12 -> lines.add("size=" + packet.g1()); // https://discord.com/channels/@me/698790755363323904/1203639168836833340
            case 13 -> lines.add("readyanim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 14 -> lines.add("walkanim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 15 -> lines.add("turnleftanim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 16 -> lines.add("turnrightanim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 17 -> lines.add("walkanim=" + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()));
            case 18 -> lines.add("category=" + Unpacker.format(Type.CATEGORY, packet.g2())); // https://discord.com/channels/@me/698790755363323904/1203639168836833340
            case 30 -> lines.add("op1=" + packet.gjstr()); // https://discord.com/channels/@me/698790755363323904/1203639168836833340
            case 31 -> lines.add("op2=" + packet.gjstr()); // https://discord.com/channels/@me/698790755363323904/1203639168836833340
            case 32 -> lines.add("op3=" + packet.gjstr()); // https://discord.com/channels/@me/698790755363323904/1203639168836833340
            case 33 -> lines.add("op4=" + packet.gjstr()); // https://discord.com/channels/@me/698790755363323904/1203639168836833340
            case 34 -> lines.add("op5=" + packet.gjstr()); // https://discord.com/channels/@me/698790755363323904/1203639168836833340

            case 40 -> {
                var length = packet.g1();

                for (var i = 0; i < length; ++i) {
                    lines.add("recol" + (i + 1) + "s=" + packet.g2());
                    lines.add("recol" + (i + 1) + "d=" + packet.g2());
                }
            }

            case 41 -> {
                var length = packet.g1();

                for (var i = 0; i < length; ++i) {
                    lines.add("retex" + (i + 1) + "s=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                    lines.add("retex" + (i + 1) + "d=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                }
            }

            case 60 -> { // https://discord.com/channels/@me/698790755363323904/1203639168836833340
                var length = packet.g1();

                for (var i = 0; i < length; i++) {
                    lines.add("headmodel=" + Unpacker.format(Type.MODEL, packet.g2()));
                }
            }

            case 93 -> lines.add("minimap=no"); // https://twitter.com/JagexAsh/status/1763550956443111935
            case 95 -> lines.add("vislevel=" + packet.g2());
            case 97 -> lines.add("resizeh=" + packet.g2());
            case 98 -> lines.add("resizev=" + packet.g2());
            case 99 -> lines.add("drawpriority=yes");
            case 100 -> lines.add("ambient=" + packet.g1s());
            case 101 -> lines.add("contrast=" + packet.g1s());

            case 102 -> {
                var filter = packet.g1();

                for (var i = 0; i < 31; i++) {
                    if ((filter & (1 << i)) != 0) {
                        lines.add("headicon" + (i + 1) + "=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null()) + "," + packet.gSmart1or2null());
                    }
                }
            }

            case 103 -> lines.add("turnspeed=" + packet.g2());

            case 106 -> {
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
                        lines.add("multinpc=" + i + "," + Unpacker.format(Type.NPC, multi));
                    }
                }
            }

            case 107 -> lines.add("active=no");
            case 109 -> lines.add("walksmoothing=no");
            case 114 -> lines.add("runanim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 115 -> lines.add("runanim=" + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()));
            case 116 -> lines.add("crawlanim=" + Unpacker.format(Type.SEQ, packet.g2()));
            case 117 -> lines.add("crawlanim=" + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()) + "," + Unpacker.format(Type.SEQ, packet.g2()));
            case 122 -> lines.add("follower=yes");
            case 123 -> lines.add("lowpriorityops=yes");

            case 118 -> {
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
                    lines.add("multinpc=default," + Unpacker.format(Type.NPC, multidefault));
                }

                var count = packet.g1();

                for (var i = 0; i <= count; ++i) {
                    var multi = packet.g2null();

                    if (multi != -1) {
                        lines.add("multinpc=" + i + "," + Unpacker.format(Type.NPC, multi));
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
