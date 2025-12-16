package osrs.unpack;

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

        if (data.widthmode != 0) {
            line(lines, "widthmode=", decodeSizeMode(data.widthmode));
        }
        if (data.heightmode != 0) {
            line(lines, "heightmode=", decodeSizeMode(data.heightmode));
        }
        if (data.xmode != 0) {
            line(lines, "xmode=", decodeXMode(data.xmode));
        }
        if (data.ymode != 0) {
            line(lines, "ymode=", decodeYMode(data.ymode));
        }

        var layerID = data.layerID;

        if (layerID != -1) {
            line(lines, "layer=", Unpacker.formatComponentShort((id & 0xffff0000) | layerID)); // if_getlayer
        }

        if (data.hide) {
            lines.add("hide=yes"); // if_sethide
        }

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
        line(lines, "onload=", decodeHook(data.onload, null, null), "null");
        line(lines, "onmouseover=", decodeHook(data.onmouseover, null, null), "null"); // if_setonmouseover
        line(lines, "onmouseleave=", decodeHook(data.onmouseleave, null, null), "null"); // if_setonmouseleave
        line(lines, "ontargetleave=", decodeHook(data.ontargetleave, null, null), "null"); // if_setontargetleave
        line(lines, "ontargetenter=", decodeHook(data.ontargetenter, null, null), "null"); // if_setontargetenter
        line(lines, "onvartransmit=", decodeHook(data.onvartransmit, data.onvartransmitlist, Type.VAR_PLAYER), "null"); // if_setonvartransmit
        line(lines, "oninvtransmit=", decodeHook(data.oninvtransmit, data.oninvtransmitlist, Type.INV), "null"); // if_setoninvtransmit
        line(lines, "onstattransmit=", decodeHook(data.onstattransmit, data.onstattransmitlist, Type.STAT), "null"); // if_setonstattransmit
        line(lines, "ontimer=", decodeHook(data.ontimer, null, null), "null"); // if_setontimer
        line(lines, "onop=", decodeHook(data.onop, null, null), "null"); // if_setonop
        line(lines, "onmouserepeat=", decodeHook(data.onmouserepeat, null, null), "null"); // if_setonmouserepeat
        line(lines, "onclick=", decodeHook(data.onclick, null, null), "null"); // if_setonclick
        line(lines, "onclickrepeat=", decodeHook(data.onclickrepeat, null, null), "null"); // if_setonclickrepeat
        line(lines, "onrelease=", decodeHook(data.onrelease, null, null), "null"); // if_setonrelease
        line(lines, "onhold=", decodeHook(data.onhold, null, null), "null"); // if_setonhold
        line(lines, "ondrag=", decodeHook(data.ondrag, null, null), "null"); // if_setondrag
        line(lines, "ondragcomplete=", decodeHook(data.ondragcomplete, null, null), "null"); // if_setondragcomplete
        line(lines, "onscrollwheel=", decodeHook(data.onscrollwheel, null, null), "null"); // if_setonscrollwheel

        return lines;
    }

    private static String decodeHook(IfType.IfTypeHook hook, int[] transmitList, Type transmitType) {
        if (hook == null) {
            if (transmitList != null) {
                // safe check
                throw new IllegalStateException();
            }
            return "null";
        }

        var script = hook.id();

        StringBuilder sb = new StringBuilder();
        sb.append(Unpacker.format(Type.CLIENTSCRIPT, script));

        var args = hook.args();
        if (!args.isEmpty()) {
            sb.append('(');
            var parameters = ScriptUnpacker.SCRIPT_PARAMETERS.get(script);
            for (var i = 0; i < args.size(); ++i) {
                if (i > 0) {
                    if (i <= args.size() - 1) {
                        sb.append(", ");
                    } else {
                        sb.append(",");
                    }
                }
                sb.append(formatHookArgument(args.get(i), parameters.get(i)));
            }
            sb.append(')');
        }
        if (transmitList != null) {
            sb.append('{');
            for (var i = 0; i < transmitList.length; ++i) {
                if (i > 0) {
                    if (i < transmitList.length - 1) {
                        sb.append(", ");
                    } else {
                        sb.append(",");
                    }
                }
                sb.append(Unpacker.format(transmitType, transmitList[i]));
            }
            sb.append('}');
        }
        return sb.toString();
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

    private static void decodeLayer(ArrayList<String> lines, IfType data) {
        line(lines, "scrollwidth=", data.scrollwidth, 0); // if_getscrollwidth
        line(lines, "scrollheight=", data.scrollheight, 0); // if_getscrollheight
        if (data.noclickthrough) {
            lines.add("noclickthrough=yes"); // if_setnoclickthrough
        }
    }

    private static void decodeGraphic(ArrayList<String> lines, IfType data) {
        line(lines, "graphic=", Unpacker.format(Type.GRAPHIC, data.graphic), "null"); // if_setgraphic
        line(lines, "2dangle=", data.angle2d, 0); // if_set2dangle
        if (data.tiling) {
            lines.add("tiling=yes"); // if_settiling
        }
        line(lines, "trans=", data.trans, 0); // if_settrans
        line(lines, "outline=", data.outline, 0); // if_setoutline
        line(lines, "graphicshadow=", data.graphicshadow, 0); // if_setgraphicshadow
        if (data.vflip) {
            lines.add("vflip=yes"); // if_setvflip
        }
        if (data.hflip) {
            lines.add("hflip=yes"); // if_sethflip
        }
    }

    private static void decodeModel(ArrayList<String> lines, IfType data) {
        line(lines, "model=", Unpacker.format(Type.MODEL, data.model));
        line(lines, "modeloriginx=", data.modeloriginx, 0); // if_setmodelorigin
        line(lines, "modeloriginy=", data.modeloriginy, 0); // if_setmodelorigin
        line(lines, "modelanglex=", data.modelanglex, 0); // if_getmodelangle_x
        line(lines, "modelangley=", data.modelangley, 0); // if_getmodelangle_y
        line(lines, "modelanglez=", data.modelanglez, 0); // if_getmodelangle_z
        line(lines, "modelzoom=", data.modelzoom, 100); // if_setmodelzoom
        line(lines, "modelanim=", Unpacker.format(Type.SEQ, data.modelanim), "null"); // if_setmodelanim
        if (data.modelorthog) {
            lines.add("modelorthog=yes"); // if_setmodelorthog
        }
        line(lines, "unknown1=", data.unknown1, 0);
        line(lines, "modelobjwidth=", data.modelobjwidth, 0);
        line(lines, "modelobjheight=", data.modelobjheight, 0);
    }

    private static void decodeText(ArrayList<String> lines, IfType data) {
        line(lines, "textfont=", Unpacker.format(Type.FONTMETRICS, data.textfont), "null"); // if_settextfont
        line(lines, "text=", data.text, ""); // if_settext
        line(lines, "textlineheight=", data.textlineheight, 0); // todo
        line(lines, "textalignh=", formatAlignH(data.textalignh), 0); // if_settextalign
        line(lines, "textalignv=", formatAlignV(data.textalignv), 0); // if_settextalign
        if (data.textshadow) {
            lines.add("textshadow=yes"); // if_settextshadow
        }
        line(lines, "colour=", Unpacker.formatColour(data.colour)); // if_setcolour
    }

    private static void decodeRectangle(ArrayList<String> lines, IfType data) {
        line(lines, "colour=", Unpacker.formatColour(data.colour)); // if_setcolour
        if (data.fill) {
            lines.add("fill=yes"); // if_setfill
        }
        line(lines, "trans=", data.trans, 0); // if_settrans
    }

    private static void decodeLine(ArrayList<String> lines, IfType data) {
        line(lines, "linewid=", data.linewid, 1); // if_setlinewid
        line(lines, "colour=", Unpacker.formatColour(data.colour)); // if_setcolour
        if (data.linedirection) {
            lines.add("linedirection=yes"); // if_setlinedirection
        }
    }

    private static void decodeCircle(ArrayList<String> lines, IfType data) {
        line(lines, "colour=", Unpacker.formatColour(data.colour)); // if_setcolour
        if (data.fill) {
            lines.add("fill=yes"); // if_setfill
        }
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
            case 4 -> "aspect";
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

    private static String formatAlignH(int id) {
        return switch (id) {
            case 0 -> "left";
            case 1 -> "centre";
            case 2 -> "right";
            default -> throw new IllegalStateException();
        };
    }

    private static String formatAlignV(int id) {
        return switch (id) {
            case 0 -> "top";
            case 1 -> "centre";
            case 2 -> "bottom";
            default -> throw new IllegalStateException();
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
