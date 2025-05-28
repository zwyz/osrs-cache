package osrs.unpack;

import osrs.Unpack;
import osrs.unpack.script.ScriptUnpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InterfaceUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.COMPONENT_NAME.getOrDefault(id, "com_" + (id & 0xffff)) + "]");

        var version = packet.g1();

        if (version != 255) {
            return lines; // todo
        }

        if (version == 255) {
            version = -1;
        }

        var type = packet.g1();
        line(lines, "type=", formatIfType(type));
        line(lines, "contenttype=", packet.g2(), 0);
        line(lines, "x=", packet.g2s(), 0); // if_getx
        line(lines, "y=", packet.g2s(), 0); // if_gety
        line(lines, "width=", packet.g2(), 0); // if_getwidth
        line(lines, "height=", (type == 9 ? packet.g2s() : packet.g2()), 0); // if_getheight
        var widthmode = 0;
        var heightmode = 0;

        if (Unpack.VERSION >= 79) {
            widthmode = packet.g1s();
            heightmode = packet.g1s();
            line(lines, "widthmode=", decodeSizeMode(widthmode), "abs");
            line(lines, "heightmode=", decodeSizeMode(heightmode), "abs");
            line(lines, "xmode=", decodeXMode(packet.g1s()), "abs_left");
            line(lines, "ymode=", decodeYMode(packet.g1s()), "abs_top");
        }

        var layerID = packet.g2null();

        if (layerID != -1) {
            line(lines, "layer=", Unpacker.COMPONENT_NAME.getOrDefault((id & 0xffff0000) | layerID, "com_" + layerID)); // if_getlayer
        }

        line(lines, "hide=", packet.g1() == 1 ? "yes" : "no", "no"); // if_sethide

        switch (type) {
            case 0 -> decodeLayer(lines, packet, version);
            case 3 -> decodeRectangle(lines, packet, version);
            case 4 -> decodeText(lines, packet, version);
            case 5 -> decodeGraphic(lines, packet, version);
            case 6 -> decodeModel(lines, packet, version, widthmode, heightmode);
            case 9 -> decodeLine(lines, packet, version);
            case 10 -> decodeArc(lines, packet, version);
            default -> throw new AssertionError("invalid type " + type);
        }

        line(lines, "events=", packet.g3(), 0); // if_setevents
        line(lines, "opbase=", packet.gjstr(), ""); // if_setopbase
        var opcount = packet.g1();

        for (var i = 0; i < opcount; ++i) {
            line(lines, "op" + (i + 1) + "=", packet.gjstr(), ""); // if_setop
        }

        line(lines, "dragdeadzone=", packet.g1(), 0); // if_setdragdeadzone
        line(lines, "dragdeadtime=", packet.g1(), 0); // if_setdragdeadtime
        line(lines, "dragrenderbehaviour=", packet.g1(), 0); // if_setdragrenderbehaviour
        line(lines, "targetverb=", packet.gjstr(), ""); // if_settargetverb

        line(lines, "onload=", decodeHook(packet), "null");
        line(lines, "onmouseover=", decodeHook(packet), "null"); // if_setonmouseover
        line(lines, "onmouseleave=", decodeHook(packet), "null"); // if_setonmouseleave
        line(lines, "ontargetleave=", decodeHook(packet), "null"); // if_setontargetleave
        line(lines, "ontargetenter=", decodeHook(packet), "null"); // if_setontargetenter
        line(lines, "onvartransmit=", decodeHook(packet), "null"); // if_setonvartransmit
        line(lines, "oninvtransmit=", decodeHook(packet), "null"); // if_setoninvtransmit
        line(lines, "onstattransmit=", decodeHook(packet), "null"); // if_setonstattransmit
        line(lines, "ontimer=", decodeHook(packet), "null"); // if_setontimer
        line(lines, "onop=", decodeHook(packet), "null"); // if_setonop
        line(lines, "onmouserepeat=", decodeHook(packet), "null"); // if_setonmouserepeat
        line(lines, "onclick=", decodeHook(packet), "null"); // if_setonclick
        line(lines, "onclickrepeat=", decodeHook(packet), "null"); // if_setonclickrepeat
        line(lines, "onrelease=", decodeHook(packet), "null"); // if_setonrelease
        line(lines, "onhold=", decodeHook(packet), "null"); // if_setonhold
        line(lines, "ondrag=", decodeHook(packet), "null"); // if_setondrag
        line(lines, "ondragcomplete=", decodeHook(packet), "null"); // if_setondragcomplete
        line(lines, "onscrollwheel=", decodeHook(packet), "null"); // if_setonscrollwheel

        line(lines, "onvartransmitlist=", decodeHookTransmitList(packet, Type.VAR_PLAYER), "null");
        line(lines, "oninvtransmitlist=", decodeHookTransmitList(packet, Type.INV), "null");
        line(lines, "onstattransmitlist=", decodeHookTransmitList(packet, Type.STAT), "null");

        if (packet.pos != packet.arr.length) {
            throw new IllegalStateException("end of file not reached");
        }

        return lines;
    }

    private static String decodeHook(Packet packet) {
        var count = packet.g1();

        if (count == 0) {
            return "null";
        }

        packet.g1();
        var script = packet.g4s();
        var arguments = new ArrayList<String>();

        for (var i = 0; i < count - 1; ++i) {
            var value = switch (packet.g1()) {
                case 0 -> packet.g4s();
                case 1 -> packet.gjstr();
                default -> throw new IllegalStateException("Unexpected value: " + packet.g1());
            };

            arguments.add(formatHookArgument(value, ScriptUnpacker.SCRIPT_PARAMETERS.get(script).get(i)));
        }

        if (arguments.isEmpty()) {
            return Unpacker.format(Type.CLIENTSCRIPT, script);
        } else {
            return Unpacker.format(Type.CLIENTSCRIPT, script) + "(" + String.join(", ", arguments) + ")";
        }
    }

    private static String formatHookArgument(Object value, Type type) {
        type = ScriptUnpacker.chooseDisplayType(type);

        if (Objects.equals(value, "event_opbase")) return "event_opbase";
        if (Objects.equals(value, Integer.MIN_VALUE + 1)) return "event_mousex";
        if (Objects.equals(value, Integer.MIN_VALUE + 2)) return "event_mousey";
        if (Objects.equals(value, Integer.MIN_VALUE + 3)) return "event_com";
        if (Objects.equals(value, Integer.MIN_VALUE + 4)) return "event_op";
        if (Objects.equals(value, Integer.MIN_VALUE + 5)) return "event_comsubid";
        if (Objects.equals(value, Integer.MIN_VALUE + 6)) return "event_com2";
        if (Objects.equals(value, Integer.MIN_VALUE + 7)) return "event_comsubid2";
        if (Objects.equals(value, Integer.MIN_VALUE + 8)) return "event_keycode";
        if (Objects.equals(value, Integer.MIN_VALUE + 9)) return "event_keychar";
        if (Objects.equals(value, Integer.MIN_VALUE + 10)) return "event_subop";

        if (value instanceof Integer i) {
            return Unpacker.format(type, i);
        }

        return "\"" + value + "\"";
    }

    private static String decodeHookTransmitList(Packet packet, Type type) {
        var count = packet.g1();

        if (count == 0) {
            return "null";
        }

        var sb = new StringBuilder();

        for (var i = 0; i < count; ++i) {
            if (i > 0) {
                sb.append(",");
            }

            sb.append(Unpacker.format(type, packet.g4s()));
        }

        return sb.toString();
    }

    private static void decodeLayer(ArrayList<String> lines, Packet packet, int version) {
        line(lines, "scrollwidth=", packet.g2(), 0); // if_getscrollwidth
        line(lines, "scrollheight=", packet.g2(), 0); // if_getscrollheight

        if (Unpack.VERSION >= 79) {
            line(lines, "noclickthrough=", ((packet.g1() == 1) ? "yes" : "no"), "no"); // if_setnoclickthrough
        }
    }

    private static void decodeGraphic(ArrayList<String> lines, Packet packet, int version) {
        line(lines, "graphic=", Unpacker.format(Type.GRAPHIC, packet.g4s()), "null"); // if_setgraphic
        line(lines, "2dangle=", packet.g2(), 0); // if_set2dangle
        line(lines, "tiling=", packet.g1() == 1 ? "yes" : "no", "no"); // if_settiling
        line(lines, "trans=", packet.g1(), 0); // if_settrans
        line(lines, "outline=", packet.g1(), 0); // if_setoutline
        line(lines, "graphicshadow=", packet.g4s(), 0); // if_setgraphicshadow
        line(lines, "vflip=", (packet.g1() == 1 ? "yes" : "no"), "no"); // if_setvflip
        line(lines, "hflip=", (packet.g1() == 1 ? "yes" : "no"), "no"); // if_sethflip
    }

    private static void decodeModel(ArrayList<String> lines, Packet packet, int version, int widthmode, int heightmode) {
        line(lines, "model=", Unpacker.format(Type.MODEL, packet.g2null()));
        line(lines, "modelorigin_x=", packet.g2s()); // if_setmodelorigin
        line(lines, "modelorigin_y=", packet.g2s()); // if_setmodelorigin
        line(lines, "modelangle_x=", packet.g2()); // if_getmodelangle_x
        line(lines, "modelangle_y=", packet.g2()); // if_getmodelangle_y
        line(lines, "modelangle_z=", packet.g2()); // if_getmodelangle_z
        line(lines, "modelzoom=", packet.g2()); // if_setmodelzoom
        line(lines, "modelanim=", Unpacker.format(Type.SEQ, packet.g2null()), "null"); // if_setmodelanim
        line(lines, "modelorthog=", packet.g1() == 1 ? "yes" : "no", "no"); // if_setmodelorthog

        if (Unpack.VERSION >= 79) {
            line(lines, "unknown1=", packet.g2(), 0); // todo: ???

            if (widthmode != 0 || heightmode != 0) { // todo: client has bug in decoding
                line(lines, "modelobjwidth=", packet.g2());
                line(lines, "modelobjheight=", packet.g2());
            }
        }
    }

    private static void decodeText(ArrayList<String> lines, Packet packet, int version) {
        line(lines, "textfont=", Unpacker.format(Type.FONTMETRICS, packet.g2null()), "null"); // if_settextfont
        line(lines, "text=", packet.gjstr(), ""); // if_settext
        line(lines, "textlineheight=", packet.g1(), 0); // todo
        line(lines, "textalignh=", packet.g1(), 0); // if_settextalign
        line(lines, "textalignv=", packet.g1(), 0); // if_settextalign
        line(lines, "textshadow=", (packet.g1() == 1 ? "yes" : "no"), "no"); // if_settextshadow
        line(lines, "colour=", Unpacker.formatColour(packet.g4s())); // if_setcolour
    }

    private static void decodeRectangle(ArrayList<String> lines, Packet packet, int version) {
        line(lines, "colour=", Unpacker.formatColour(packet.g4s())); // if_setcolour
        line(lines, "fill=", (packet.g1() == 1 ? "yes" : "no"), "no"); // if_setfill
        line(lines, "trans=", packet.g1(), 0); // if_settrans
    }

    private static void decodeLine(ArrayList<String> lines, Packet packet, int version) {
        line(lines, "linewid=", packet.g1(), 1); // if_setlinewid
        line(lines, "colour=", Unpacker.formatColour(packet.g4s())); // if_setcolour

        if (Unpack.VERSION >= 79) {
            line(lines, "linedirection=", (packet.g1() == 1 ? "yes" : "no"), "no"); // if_setlinedirection
        }
    }

    private static void decodeArc(ArrayList<String> lines, Packet packet, int version) {
        line(lines, "colour=", Unpacker.formatColour(packet.g4s())); // if_setcolour
        var fill = packet.g1() == 1;
        line(lines, "fill=", (fill ? "yes" : "no"), "no"); // if_setfill
        line(lines, "trans=", packet.g1(), 0); // if_settrans
        line(lines, "arcstart=", packet.g2());
        line(lines, "arcend=", packet.g2());

        if (!fill) {
            line(lines, "linewid=", packet.g1(), 1); // if_setlinewid
        }
    }

    public static String decodeSizeMode(int widthmode) {
        return switch (widthmode) {
            case 0 -> "abs";
            case 1 -> "minus";
            case 2 -> "mode_2";
            case 3 -> "mode_3";
            case 4 -> "mode_4";
            default -> throw new IllegalStateException("Unexpected value: " + widthmode);
        };
    }

    public static String decodeXMode(int widthmode) {
        return switch (widthmode) {
            case 0 -> "abs_left";
            case 1 -> "abs_centre";
            case 2 -> "abs_right";
            case 3 -> "xmode_3";
            case 4 -> "xmode_4";
            case 5 -> "xmode_5";
            default -> throw new IllegalStateException("Unexpected value: " + widthmode);
        };
    }

    public static String decodeYMode(int widthmode) {
        return switch (widthmode) {
            case 0 -> "abs_top";
            case 1 -> "abs_centre";
            case 2 -> "abs_bottom";
            case 3 -> "ymode_3";
            case 4 -> "ymode_4";
            case 5 -> "ymode_5";
            default -> throw new IllegalStateException("Unexpected value: " + widthmode);
        };
    }

    private static void line(ArrayList<String> lines, String name, Object value) {
        lines.add(name + value);
    }

    private static void line(ArrayList<String> lines, String name, Object value, Object ignore) {
        if (!Objects.equals(value, ignore)) {
            lines.add(name + value);
        }
    }

    public static String formatIfType(int type) {
        return switch (type) {
            case 0 -> "layer";
            case 3 -> "rectangle";
            case 4 -> "text";
            case 5 -> "graphic";
            case 6 -> "model";
            case 9 -> "line";
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
