package osrs.unpack.config;

import osrs.unpack.ColourConversion;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class ObjUnpacker {
    private static final long OZ_A = 28_349_523_125L; // https://en.wikipedia.org/wiki/Ounce
    private static final long OZ_B = 1_000_000_000L;

    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.OBJ, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("model=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 2 -> lines.add("name=" + packet.gjstr());
            case 3 -> lines.add("desc=" + packet.gjstr());
            case 4 -> lines.add("2dzoom=" + packet.g2()); // https://discord.com/channels/@me/698790755363323904/1057485711214923888
            case 5 -> lines.add("2dxan=" + packet.g2()); // https://discord.com/channels/@me/698790755363323904/1057485711214923888
            case 6 -> lines.add("2dyan=" + packet.g2()); // https://discord.com/channels/@me/698790755363323904/1057485711214923888
            case 7 -> lines.add("2dxof=" + packet.g2s());
            case 8 -> lines.add("2dyof=" + packet.g2s());
            case 9 -> lines.add("unknown9=" + packet.gjstr()); // todo: unused
            case 11 -> lines.add("stackable=yes"); // https://twitter.com/JagexAsh/status/1626564052901060609
            case 12 -> lines.add("cost=" + packet.g4s());
            case 13 -> lines.add("wearpos=" + Unpacker.formatWearPos(packet.g1())); // https://discord.com/channels/@me/698790755363323904/1057485711214923888
            case 14 -> lines.add("wearpos2=" + Unpacker.formatWearPos(packet.g1()));
            case 16 -> lines.add("members=yes");
            case 23 -> lines.add("manwear=" + Unpacker.format(Type.MODEL, packet.g2()) + "," + packet.g1()); // https://discord.com/channels/@me/698790755363323904/1057485711214923888
            case 24 -> lines.add("manwear2=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 25 -> lines.add("womanwear=" + Unpacker.format(Type.MODEL, packet.g2()) + "," + packet.g1()); // https://discord.com/channels/@me/698790755363323904/1057485711214923888
            case 26 -> lines.add("womanwear2=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 27 -> lines.add("wearpos3=" + Unpacker.formatWearPos(packet.g1()));
            case 30 -> lines.add("op1=" + packet.gjstr());
            case 31 -> lines.add("op2=" + packet.gjstr());
            case 32 -> lines.add("op3=" + packet.gjstr());
            case 33 -> lines.add("op4=" + packet.gjstr());
            case 34 -> lines.add("op5=" + packet.gjstr());
            case 35 -> lines.add("iop1=" + packet.gjstr()); // https://twitter.com/JagexAsh/status/1654115186863910912
            case 36 -> lines.add("iop2=" + packet.gjstr()); // https://twitter.com/JagexAsh/status/1654115186863910912
            case 37 -> lines.add("iop3=" + packet.gjstr()); // https://twitter.com/JagexAsh/status/1654115186863910912
            case 38 -> lines.add("iop4=" + packet.gjstr()); // https://twitter.com/JagexAsh/status/1654115186863910912
            case 39 -> lines.add("iop5=" + packet.gjstr()); // https://twitter.com/JagexAsh/status/1654115186863910912

            case 40 -> {
                var count = packet.g1();

                for (var i = 0; i < count; ++i) {
                    lines.add("recol" + (i + 1) + "s=" + ColourConversion.reverseRGBFromHSL(packet.g2()));
                    lines.add("recol" + (i + 1) + "d=" + ColourConversion.reverseRGBFromHSL(packet.g2()));
                }
            }

            case 41 -> {
                var count = packet.g1();

                for (var i = 0; i < count; ++i) {
                    lines.add("retex" + (i + 1) + "s=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                    lines.add("retex" + (i + 1) + "d=" + Unpacker.format(Type.MATERIAL, packet.g2()));
                }
            }

            case 42 -> lines.add("shiftclickiop=" + packet.g1s()); // https://twitter.com/JagexAsh/status/1654115186863910912
            case 65 -> lines.add("stockmarket=yes");

            case 75 -> { // https://twitter.com/JagexAsh/status/570235510691487744
                var weight_g = packet.g2s();
                var weight_oz = weight_g >= 0 ? Math.ceilDiv(weight_g * OZ_B, OZ_A) : -Math.ceilDiv(-weight_g * OZ_B, OZ_A); // nearest weight in oz, rounding away from zero

                if (weight_g == 0) {
                    lines.add("weight=0g");
                } else if (weight_oz * OZ_A / OZ_B == weight_g) { // is that weight in g, rounding towards zero, the actual weight?
                    if (weight_oz % 16 == 0) {
                        lines.add("weight=" + (weight_oz / 16) + "lb");
                    } else {
                        lines.add("weight=" + weight_oz + "oz");
                    }
                } else {
                    if (weight_g % 1000 == 0) {
                        lines.add("weight=" + (weight_g / 1000) + "kg");
                    } else {
                        lines.add("weight=" + weight_g + "g");
                    }
                }
            }

            case 78 -> lines.add("manwear3=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 79 -> lines.add("womanwear3=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 90 -> lines.add("manhead=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 91 -> lines.add("womanhead=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 92 -> lines.add("manhead2=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 93 -> lines.add("womanhead2=" + Unpacker.format(Type.MODEL, packet.g2()));
            case 94 -> lines.add("category=" + Unpacker.format(Type.CATEGORY, packet.g2()));
            case 95 -> lines.add("2dzan=" + packet.g2()); // https://discord.com/channels/@me/698790755363323904/1057485711214923888
            case 97 -> lines.add("certlink=" + Unpacker.format(Type.OBJ, packet.g2()));
            case 98 -> lines.add("certtemplate=" + Unpacker.format(Type.OBJ, packet.g2()));
            case 100 -> lines.add("count1=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 101 -> lines.add("count2=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 102 -> lines.add("count3=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 103 -> lines.add("count4=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 104 -> lines.add("count5=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 105 -> lines.add("count6=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 106 -> lines.add("count7=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 107 -> lines.add("count8=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 108 -> lines.add("count9=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 109 -> lines.add("count10=" + Unpacker.format(Type.OBJ, packet.g2()) + "," + packet.g2());
            case 110 -> lines.add("resizex=" + packet.g2());
            case 111 -> lines.add("resizey=" + packet.g2());
            case 112 -> lines.add("resizez=" + packet.g2());
            case 113 -> lines.add("ambient=" + packet.g1s());
            case 114 -> lines.add("contrast=" + packet.g1s());
            case 115 -> lines.add("team=" + packet.g1());
            case 139 -> lines.add("boughtlink=" + Unpacker.format(Type.OBJ, packet.g2()));
            case 140 -> lines.add("boughttemplate=" + Unpacker.format(Type.OBJ, packet.g2()));
            case 148 -> lines.add("placeholderlink=" + Unpacker.format(Type.OBJ, packet.g2()));
            case 149 -> lines.add("placeholdertemplate=" + Unpacker.format(Type.OBJ, packet.g2()));

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
