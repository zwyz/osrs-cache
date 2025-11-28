package osrs.unpack;

import osrs.Unpack;
import osrs.unpack.script.ScriptUnpacker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InterfaceUnpacker {

    public static List<String> unpack(int id, IfType data) {
        var lines = new ArrayList<String>();

        lines.add("[" + Unpacker.formatComponentShort(id) + "]");

        if (!data.scripted) {
            return lines;
        }

        var type = data.type;
        line(lines, "type=", formatIfType(type));
        line(lines, "contenttype=", data.contenttype, 0);
        line(lines, "x=", data.x, 0); // if_getx
        line(lines, "y=", data.y, 0); // if_gety
        line(lines, "width=", data.width, 0); // if_getwidth
        line(lines, "height=", data.height, 0); // if_getheight

        if (Unpack.VERSION >= 79) {
            line(lines, "widthmode=", decodeSizeMode(data.widthmode), "abs");
            line(lines, "heightmode=", decodeSizeMode(data.heightmode), "abs");
            line(lines, "xmode=", decodeXMode(data.xmode), "abs_left");
            line(lines, "ymode=", decodeYMode(data.ymode), "abs_top");
        }

        var layerID = data.layerID;

        if (layerID != -1) {
            line(lines, "layer=", Unpacker.formatComponentShort((id & 0xffff0000) | layerID)); // if_getlayer
        }

        line(lines, "hide=", data.hide ? "yes" : "no", "no"); // if_sethide

        switch (type) {
            case 0 -> decodeLayer(lines, data);
            case 3 -> decodeRectangle(lines, data);
            case 4 -> decodeText(lines, data);
            case 5 -> decodeGraphic(lines, data);
            case 6 -> decodeModel(lines, data);
            case 9 -> decodeLine(lines, data);
            case 10 -> decodeCircle(lines, data);
            default -> throw new AssertionError("invalid type " + type);
        }

        line(lines, "events=", data.events, 0); // if_setevents
        line(lines, "opbase=", data.opbase, ""); // if_setopbase
        if (data.ops != null) {
            for (var i = 0; i < data.ops.length; ++i) {
                line(lines, "op" + (i + 1) + "=", data.ops[i], ""); // if_setop
            }
        }

        line(lines, "dragdeadzone=", data.dragdeadzone, 0); // if_setdragdeadzone
        line(lines, "dragdeadtime=", data.dragdeadtime, 0); // if_setdragdeadtime
        line(lines, "dragrenderbehaviour=", data.dragrenderbehaviour, 0); // if_setdragrenderbehaviour
        line(lines, "targetverb=", data.targetverb, ""); // if_settargetverb

        line(lines, "onload=", decodeHook(data.onload), "null");
        line(lines, "onmouseover=", decodeHook(data.onmouseover), "null"); // if_setonmouseover
        line(lines, "onmouseleave=", decodeHook(data.onmouseleave), "null"); // if_setonmouseleave
        line(lines, "ontargetleave=", decodeHook(data.ontargetleave), "null"); // if_setontargetleave
        line(lines, "ontargetenter=", decodeHook(data.ontargetenter), "null"); // if_setontargetenter
        line(lines, "onvartransmit=", decodeHook(data.onvartransmit), "null"); // if_setonvartransmit
        line(lines, "oninvtransmit=", decodeHook(data.oninvtransmit), "null"); // if_setoninvtransmit
        line(lines, "onstattransmit=", decodeHook(data.onstattransmit), "null"); // if_setonstattransmit
        line(lines, "ontimer=", decodeHook(data.ontimer), "null"); // if_setontimer
        line(lines, "onop=", decodeHook(data.onop), "null"); // if_setonop
        line(lines, "onmouserepeat=", decodeHook(data.onmouserepeat), "null"); // if_setonmouserepeat
        line(lines, "onclick=", decodeHook(data.onclick), "null"); // if_setonclick
        line(lines, "onclickrepeat=", decodeHook(data.onclickrepeat), "null"); // if_setonclickrepeat
        line(lines, "onrelease=", decodeHook(data.onrelease), "null"); // if_setonrelease
        line(lines, "onhold=", decodeHook(data.onhold), "null"); // if_setonhold
        line(lines, "ondrag=", decodeHook(data.ondrag), "null"); // if_setondrag
        line(lines, "ondragcomplete=", decodeHook(data.ondragcomplete), "null"); // if_setondragcomplete
        line(lines, "onscrollwheel=", decodeHook(data.onscrollwheel), "null"); // if_setonscrollwheel

        line(lines, "onvartransmitlist=", decodeHookTransmitList(data.onvartransmitlist, Type.VAR_PLAYER), "null");
        line(lines, "oninvtransmitlist=", decodeHookTransmitList(data.oninvtransmitlist, Type.INV), "null");
        line(lines, "onstattransmitlist=", decodeHookTransmitList(data.onstattransmitlist, Type.STAT), "null");


        return lines;
    }

    private static String decodeHook(IfType.IfTypeHook hook) {
        if (hook == null) {
            return "null";
        }

        var script = hook.id();
        var arguments = new ArrayList<String>();

        for (int i = 0; i < hook.args().size(); i++) {
            var arg = hook.args().get(i);
            arguments.add(formatHookArgument(arg, ScriptUnpacker.SCRIPT_PARAMETERS.get(script).get(i)));
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

    private static String decodeHookTransmitList(int[] transmitList, Type type) {
        if (transmitList == null) {
            return "null";
        }

        var sb = new StringBuilder();

        for (var i = 0; i < transmitList.length; ++i) {
            if (i > 0) {
                sb.append(",");
            }

            sb.append(Unpacker.format(type, transmitList[i]));
        }

        return sb.toString();
    }

    private static void decodeLayer(ArrayList<String> lines, IfType data) {
        line(lines, "scrollwidth=", data.scrollwidth, 0); // if_getscrollwidth
        line(lines, "scrollheight=", data.scrollheight, 0); // if_getscrollheight
        line(lines, "noclickthrough=", data.noclickthrough ? "yes" : "no", "no"); // if_setnoclickthrough
    }

    private static void decodeGraphic(ArrayList<String> lines, IfType data) {
        line(lines, "graphic=", Unpacker.format(Type.GRAPHIC, data.graphic), "null"); // if_setgraphic
        line(lines, "2dangle=", data.angle2d, 0); // if_set2dangle
        line(lines, "tiling=", data.tiling ? "yes" : "no", "no"); // if_settiling
        line(lines, "trans=", data.trans, 0); // if_settrans
        line(lines, "outline=", data.outline, 0); // if_setoutline
        line(lines, "graphicshadow=", data.graphicshadow, 0); // if_setgraphicshadow
        line(lines, "vflip=", (data.vflip ? "yes" : "no"), "no"); // if_setvflip
        line(lines, "hflip=", (data.hflip ? "yes" : "no"), "no"); // if_sethflip
    }

    private static void decodeModel(ArrayList<String> lines, IfType data) {
        line(lines, "model=", Unpacker.format(Type.MODEL, data.model));
        line(lines, "modelorigin_x=", data.modelorigin_x); // if_setmodelorigin
        line(lines, "modelorigin_y=", data.modelorigin_y); // if_setmodelorigin
        line(lines, "modelangle_x=", data.modelangle_x); // if_getmodelangle_x
        line(lines, "modelangle_y=", data.modelangle_y); // if_getmodelangle_y
        line(lines, "modelangle_z=", data.modelangle_z); // if_getmodelangle_z
        line(lines, "modelzoom=", data.modelzoom); // if_setmodelzoom
        line(lines, "modelanim=", Unpacker.format(Type.SEQ, data.modelanim), "null"); // if_setmodelanim
        line(lines, "modelorthog=", data.modelorthog ? "yes" : "no", "no"); // if_setmodelorthog
        if (Unpack.VERSION >= 79) {
            line(lines, "unknown1=", data.unknown1, 0);
            if (data.widthmode != 0 || data.heightmode != 0) {
                line(lines, "modelobjwidth=", data.modelobjwidth);
                line(lines, "modelobjheight=", data.modelobjheight);
            }
        }
    }

    private static void decodeText(ArrayList<String> lines, IfType data) {
        line(lines, "textfont=", Unpacker.format(Type.FONTMETRICS, data.textfont), "null"); // if_settextfont
        line(lines, "text=", data.text, ""); // if_settext
        line(lines, "textlineheight=", data.textlineheight, 0); // todo
        line(lines, "textalignh=", data.textalignh, 0); // if_settextalign
        line(lines, "textalignv=", data.textalignv, 0); // if_settextalign
        line(lines, "textshadow=", data.textshadow ? "yes" : "no", "no"); // if_settextshadow
        line(lines, "colour=", Unpacker.formatColour(data.colour)); // if_setcolour
    }

    private static void decodeRectangle(ArrayList<String> lines, IfType data) {
        line(lines, "colour=", Unpacker.formatColour(data.colour)); // if_setcolour
        line(lines, "fill=", data.fill ? "yes" : "no", "no"); // if_setfill
        line(lines, "trans=", data.trans, 0); // if_settrans
    }

    private static void decodeLine(ArrayList<String> lines, IfType data) {
        line(lines, "linewid=", data.linewid, 1); // if_setlinewid
        line(lines, "colour=", Unpacker.formatColour(data.colour)); // if_setcolour

        if (Unpack.VERSION >= 79) {
            line(lines, "linedirection=", data.linedirection ? "yes" : "no", "no"); // if_setlinedirection
        }
    }

    private static void decodeCircle(ArrayList<String> lines, IfType data) {
        line(lines, "colour=", Unpacker.formatColour(data.colour)); // if_setcolour
        line(lines, "fill=", data.fill ? "yes" : "no", "no"); // if_setfill
        line(lines, "trans=", data.trans, 0); // if_settrans
        line(lines, "arcstart=", data.arcstart);
        line(lines, "arcend=", data.arcend);
        line(lines, "linewid=", data.linewid, 1); // if_setlinewid
    }

    public static String decodeSizeMode(int sizemode) {
        return switch (sizemode) {
            case 0 -> "abs";
            case 1 -> "minus";
            case 2 -> "rel";
            case 3 -> "mode_3";
            case 4 -> "mode_4";
            default -> throw new IllegalStateException("Unexpected value: " + sizemode);
        };
    }

    public static String decodeXMode(int xmode) {
        return switch (xmode) {
            case 0 -> "abs_left";
            case 1 -> "abs_centre";
            case 2 -> "abs_right";
            case 3 -> "rel_left";
            case 4 -> "rel_centre";
            case 5 -> "rel_right";
            default -> throw new IllegalStateException("Unexpected value: " + xmode);
        };
    }

    public static String decodeYMode(int ymode) {
        return switch (ymode) {
            case 0 -> "abs_top";
            case 1 -> "abs_centre";
            case 2 -> "abs_bottom";
            case 3 -> "rel_top";
            case 4 -> "rel_centre";
            case 5 -> "rel_bottom";
            default -> throw new IllegalStateException("Unexpected value: " + ymode);
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
