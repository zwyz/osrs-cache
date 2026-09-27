package osrs.unpack;

import osrs.unpack.script.ScriptUnpacker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InterfaceUnpacker {
    public static List<String> unpack(Component component) {
        var lines = new ArrayList<String>();

        lines.add("[" + Unpacker.formatComponentShort(component.id) + "]");
        lines.add("type=" + formatIfType(component.type));


        if (component.clientcode != 0) lines.add("clientcode=" + component.clientcode);
        if (component.x != 0) lines.add("x=" + component.x); // if_getx
        if (component.y != 0) lines.add("y=" + component.y); // if_gety
        if (component.width != 0) lines.add("width=" + component.width); // if_getwidth
        if (component.height != 0) lines.add("height=" + component.height); // if_getheight
        if (component.widthmode != 0) lines.add("widthmode=" + formatSizeMode(component.widthmode));
        if (component.heightmode != 0) lines.add("heightmode=" + formatSizeMode(component.heightmode));
        if (component.xmode != 0) lines.add("xmode=" + formatXMode(component.xmode));
        if (component.ymode != 0) lines.add("ymode=" + formatYMode(component.ymode));
        if (component.layer != -1) lines.add("layer=" + Unpacker.formatComponentShort((component.id & 0xffff0000) | component.layer)); // if_getlayer
        if (component.mouseoverlayer != -1) lines.add("mouseoverlayer=" + Unpacker.formatComponentShort((component.id & 0xffff0000) | component.mouseoverlayer)); // if1 only
        if (component.hide) lines.add("hide=yes"); // if_sethide
        if (component.noclickthrough) lines.add("noclickthrough=yes"); // if_setnoclickthrough
        if (component.buttontype != 0) lines.add("buttontype=" + component.buttontype); // if1 only
        if (component.legacytrans != 0) lines.add("trans=" + component.legacytrans); // if1 only

        switch (component.type) {
            case 0 -> unpackLayer(lines, component);
            case 2 -> unpackInv(lines, component);
            case 3 -> unpackRectangle(lines, component);
            case 4 -> unpackText(lines, component);
            case 5 -> unpackGraphic(lines, component);
            case 6 -> unpackModel(lines, component);
            case 7 -> unpackInvText(lines, component);
            case 8 -> unpackTooltip(lines, component);
            case 9 -> unpackLine(lines, component);
            case 10 -> unpackCircle(lines, component);
            default -> throw new AssertionError("invalid type " + component.type);
        }

        var events = component.events;
        if (((events >>> 0) & 1) != 0) lines.add("pausebutton=yes");

        var transmitop = new ArrayList<String>();
        if (((events >>> 1) & 1) != 0) transmitop.add("1");
        if (((events >>> 2) & 1) != 0) transmitop.add("2");
        if (((events >>> 3) & 1) != 0) transmitop.add("3");
        if (((events >>> 4) & 1) != 0) transmitop.add("4");
        if (((events >>> 5) & 1) != 0) transmitop.add("5");
        if (((events >>> 6) & 1) != 0) transmitop.add("6");
        if (((events >>> 7) & 1) != 0) transmitop.add("7");
        if (((events >>> 8) & 1) != 0) transmitop.add("8");
        if (((events >>> 9) & 1) != 0) transmitop.add("9");
        if (((events >>> 10) & 1) != 0) transmitop.add("10");
        if (!transmitop.isEmpty()) lines.add("transmitop=" + String.join(",", transmitop));

        var targetmask = new ArrayList<String>();
        if ((events & (1 << 11)) != 0) targetmask.add("obj");
        if ((events & (1 << 12)) != 0) targetmask.add("npc");
        if ((events & (1 << 13)) != 0) targetmask.add("loc");
        if ((events & (1 << 14)) != 0) targetmask.add("player");
        if ((events & (1 << 15)) != 0) targetmask.add("inv");
        if ((events & (1 << 16)) != 0) targetmask.add("com");
        if ((events & (1 << 17)) != 0) targetmask.add("coord");
        if (!targetmask.isEmpty()) lines.add("targetmask=" + String.join(",", targetmask));

        if (((events >>> 18) & 0b111) != 0) lines.add("dragdepth=" + ((events >>> 18) & 0b111));
        if (((events >>> 21) & 1) != 0) lines.add("candrop=yes");
        if (((events >>> 22) & 1) != 0) lines.add("cantarget=yes");
        if (((events >>> 23) & 1) != 0) lines.add("event23=yes");
        if (((events >>> 24) & 1) != 0) lines.add("event24=yes");
        if (((events >>> 25) & 1) != 0) lines.add("event25=yes");
        if (((events >>> 26) & 1) != 0) lines.add("event26=yes");
        if (((events >>> 27) & 1) != 0) lines.add("event27=yes");
        if (((events >>> 28) & 1) != 0) lines.add("event28=yes");
        if (((events >>> 29) & 1) != 0) lines.add("event29=yes");
        if (((events >>> 30) & 1) != 0) lines.add("event30=yes");
        if (((events >>> 31) & 1) != 0) lines.add("event31=yes");

        if (!component.opbase.isEmpty()) lines.add("opbase=" + component.opbase); // if_setopbase

        for (var i = 0; i < component.ops.length; i++) { // if_setop
            if (!component.ops[i].isEmpty()) lines.add("op" + (i + 1) + "=" + component.ops[i]);
        }

        if (component.dragdeadzone != 0) lines.add("dragdeadzone=" + component.dragdeadzone); // if_setdragdeadzone
        if (component.dragdeadtime != 0) lines.add("dragdeadtime=" + component.dragdeadtime); // if_setdragdeadtime
        if (component.dragrenderbehaviour != 0) lines.add("dragrenderbehaviour=" + component.dragrenderbehaviour); // if_setdragrenderbehaviour
        if (!component.targetverb.isEmpty()) lines.add("targetverb=" + component.targetverb); // if_settargetverb
        if (!component.targetbase.isEmpty()) lines.add("targetbase=" + component.targetbase); // if1 only
        if (!component.buttontext.isEmpty()) lines.add("buttontext=" + component.buttontext); // if1 only

        if (component.scriptInstructions.length != 0) { // if1 only
            for (var i = 0; i < component.scriptInstructions.length; i++) {
                if (i >= component.scriptComparison.length || component.scriptComparison[i] == 0) {
                    lines.add("script" + i + "=" + unpackIfVar(component.scriptInstructions[i]));
                } else if (component.scriptComparison[i] == 1) {
                    lines.add("script=" + unpackIfVar(component.scriptInstructions[i]) + " = " + component.scriptComparisonValue[i]);
                } else if (component.scriptComparison[i] == 2) {
                    lines.add("script=" + unpackIfVar(component.scriptInstructions[i]) + " < " + component.scriptComparisonValue[i]);
                } else if (component.scriptComparison[i] == 3) {
                    lines.add("script=" + unpackIfVar(component.scriptInstructions[i]) + " > " + component.scriptComparisonValue[i]);
                } else if (component.scriptComparison[i] == 4) {
                    lines.add("script=" + unpackIfVar(component.scriptInstructions[i]) + " != " + component.scriptComparisonValue[i]);
                }
            }
        }


        if (component.onload != null) lines.add("onload=" + formatHook(component.onload, null, null));
        if (component.onmouseover != null) lines.add("onmouseover=" + formatHook(component.onmouseover, null, null)); // if_setonmouseover
        if (component.onmouseleave != null) lines.add("onmouseleave=" + formatHook(component.onmouseleave, null, null)); // if_setonmouseleave
        if (component.ontargetleave != null) lines.add("ontargetleave=" + formatHook(component.ontargetleave, null, null)); // if_setontargetleave
        if (component.ontargetenter != null) lines.add("ontargetenter=" + formatHook(component.ontargetenter, null, null)); // if_setontargetenter
        if (component.onvartransmit != null) lines.add("onvartransmit=" + formatHook(component.onvartransmit, component.onvartransmitlist, Type.VAR_PLAYER)); // if_setonvartransmit
        if (component.oninvtransmit != null) lines.add("oninvtransmit=" + formatHook(component.oninvtransmit, component.oninvtransmitlist, Type.INV)); // if_setoninvtransmit
        if (component.onstattransmit != null) lines.add("onstattransmit=" + formatHook(component.onstattransmit, component.onstattransmitlist, Type.STAT)); // if_setonstattransmit
        if (component.ontimer != null) lines.add("ontimer=" + formatHook(component.ontimer, null, null)); // if_setontimer
        if (component.onop != null) lines.add("onop=" + formatHook(component.onop, null, null)); // if_setonop
        if (component.onmouserepeat != null) lines.add("onmouserepeat=" + formatHook(component.onmouserepeat, null, null)); // if_setonmouserepeat
        if (component.onclick != null) lines.add("onclick=" + formatHook(component.onclick, null, null)); // if_setonclick
        if (component.onclickrepeat != null) lines.add("onclickrepeat=" + formatHook(component.onclickrepeat, null, null)); // if_setonclickrepeat
        if (component.onrelease != null) lines.add("onrelease=" + formatHook(component.onrelease, null, null)); // if_setonrelease
        if (component.onhold != null) lines.add("onhold=" + formatHook(component.onhold, null, null)); // if_setonhold
        if (component.ondrag != null) lines.add("ondrag=" + formatHook(component.ondrag, null, null)); // if_setondrag
        if (component.ondragcomplete != null) lines.add("ondragcomplete=" + formatHook(component.ondragcomplete, null, null)); // if_setondragcomplete
        if (component.onscrollwheel != null) lines.add("onscrollwheel=" + formatHook(component.onscrollwheel, null, null)); // if_setonscrollwheel

        return lines;
    }

    private static String unpackIfVar(int[] instructions) {
        var i = 0;
        var e = "";

        while (true) {
            var opcode = instructions[i++];

            if (opcode == 0) {
                break;
            } else if (opcode == 15) {
                e += " - ";
                continue;
            } else if (opcode == 16) {
                e += " / ";
                continue;
            } else if (opcode == 17) {
                e += " * ";
                continue;
            } else if (!e.isEmpty()) {
                e += " + ";
            }

            switch (opcode) {
                case 1 -> e += "stat(" + Unpacker.format(Type.STAT, instructions[i++]) + ")";
                case 2 -> e += "stat_base(" + Unpacker.format(Type.STAT, instructions[i++]) + ")";
                case 3 -> e += "stat_xp(" + Unpacker.format(Type.STAT, instructions[i++]) + ")";
                case 6 -> e += "stat_levelxp(" + instructions[i++] + ")";
                case 8 -> e += "stat_combat";
                case 9 -> e += "stat_total";

                case 4 -> e += "inv_total(" + Unpacker.format(Type.COMPONENT, instructions[i++] << 16 | instructions[i++]) + ", " + Unpacker.format(Type.OBJ, instructions[i++]) + ")";
                case 10 -> e += "inv_contains(" + Unpacker.format(Type.COMPONENT, instructions[i++] << 16 | instructions[i++]) + ", " + Unpacker.format(Type.OBJ, instructions[i++]) + ")";

                case 5 -> e += Unpacker.format(Type.VAR_PLAYER, instructions[i++]);
                case 14 -> e += Unpacker.format(Type.VAR_PLAYER_BIT, instructions[i++]);
                case 13 -> e += "testbit(" + Unpacker.format(Type.VAR_PLAYER, instructions[i++]) + ", " + instructions[i++] + ")";
                case 7 -> e += "fatigue(" + Unpacker.format(Type.VAR_PLAYER, instructions[i++]) + ")";

                case 11 -> e += "runenergy_visible";
                case 12 -> e += "runweight_visible";
                case 18 -> e += "coordx";
                case 19 -> e += "coordz";
                case 20 -> e += instructions[i++];

                default -> throw new IllegalStateException("unknown instruction " + opcode);
            }
        }

        return e;
    }

    private static String formatHook(Object[] hook, int[] transmitList, Type transmitType) {
        if (hook == null) {
            return "null";
        }

        var script = (int) hook[0];
        var sb = new StringBuilder();
        sb.append(Unpacker.format(Type.CLIENTSCRIPT, script));

        if (hook.length > 1) {
            sb.append('(');
            var parameters = ScriptUnpacker.SCRIPT_PARAMETERS.get(script);

            for (var i = 1; i < hook.length; i++) {
                if (i > 1) sb.append(", ");
                sb.append(formatHookArgument(hook[i], parameters.get(i - 1)));
            }

            sb.append(')');
        }

        if (transmitList != null) {
            sb.append('{');

            for (var i = 0; i < transmitList.length; ++i) {
                if (i > 0) sb.append(", ");
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

    private static void unpackLayer(ArrayList<String> lines, Component component) {
        if (component.scrollwidth != 0) lines.add("scrollwidth=" + component.scrollwidth); // if_getscrollwidth
        if (component.scrollheight != 0) lines.add("scrollheight=" + component.scrollheight); // if_getscrollheight
    }

    private static void unpackInv(ArrayList<String> lines, Component component) {
        if (component.draggable) lines.add("draggable=yes");
        if (component.interactable) lines.add("interactable=yes");
        if (component.usable) lines.add("usable=yes");
        if (component.swappable) lines.add("swappable=yes");
        if (component.paddingx != 0) lines.add("paddingx=" + component.paddingx);
        if (component.paddingy != 0) lines.add("paddingy=" + component.paddingy);

        for (var i = 0; i < component.sloticon.length; i++) {
            if (component.sloticon[i] != -1) {
                lines.add("slot" + (i + 1) + "=" + component.slotoffsetx[i] + "," + component.slotoffsety[i] + "," + Unpacker.format(Type.GRAPHIC, component.sloticon[i]));
            }
        }

        for (var i = 0; i < component.objops.length; i++) {
            if (!component.objops[i].isEmpty()) lines.add("op" + (i + 1) + "=" + component.objops[i]);
        }
    }

    private static void unpackInvText(ArrayList<String> lines, Component component) {
        if (component.textalignh != 0) lines.add("textalignh=" + formatAlignH(component.textalignh)); // if_settextalign
        if (component.textfont != -1) lines.add("textfont=" + Unpacker.format(Type.GRAPHIC, component.textfont)); // if_settextfont
        if (component.textshadow) lines.add("textshadow=yes"); // if_settextshadow
        lines.add("colour=" + Unpacker.formatColour(component.colour)); // if_setcolour
        if (component.paddingx != 0) lines.add("paddingx=" + component.paddingx);
        if (component.paddingy != 0) lines.add("paddingy=" + component.paddingy);
        if (component.interactable) lines.add("interactable=yes");

        for (var i = 0; i < component.objops.length; i++) {
            if (!component.objops[i].isEmpty()) lines.add("op" + (i + 1) + "=" + component.objops[i]);
        }
    }

    private static void unpackTooltip(ArrayList<String> lines, Component component) {
        if (!component.text.isEmpty()) lines.add("text=" + component.text); // if_settext
    }

    private static void unpackGraphic(ArrayList<String> lines, Component component) {
        if (component.graphic != -1) lines.add("graphic=" + Unpacker.format(Type.GRAPHIC, component.graphic)); // if_setgraphic
        if (component.graphicactive != -1) lines.add("graphicactive=" + Unpacker.format(Type.GRAPHIC, component.graphicactive)); // if1 only
        if (component.angle2d != 0) lines.add("2dangle=" + component.angle2d); // if_set2dangle
        if (component.tiling) lines.add("tiling=yes"); // if_settiling
        if (component.trans != 0) lines.add("trans=" + component.trans); // if_settrans
        if (component.outline != 0) lines.add("outline=" + component.outline); // if_setoutline
        if (component.graphicshadow != 0) lines.add("graphicshadow=" + component.graphicshadow); // if_setgraphicshadow
        if (component.vflip) lines.add("vflip=yes"); // if_setvflip
        if (component.hflip) lines.add("hflip=yes"); // if_sethflip
    }

    private static void unpackModel(ArrayList<String> lines, Component component) {
        lines.add("model=" + Unpacker.format(Type.MODEL, component.model));
        if (component.modelactive != -1) lines.add("modelactive=" + Unpacker.format(Type.MODEL, component.modelactive)); // if1 only

        if (component.modelorigin_x != 0 || component.modelorigin_y != 0) {
            lines.add("modelorigin=" + component.modelorigin_x + "," + component.modelorigin_y); // if_setmodelorigin
        }

        if (component.modelangle_x != 0 || component.modelangle_y != 0 || component.modelangle_z != 0) {
            lines.add("modelangle=" + component.modelangle_x + "," + component.modelangle_y + "," + component.modelangle_z); // if_setmodelangle
        }

        if (component.modelzoom != 100) lines.add("modelzoom=" + component.modelzoom); // if_setmodelzoom
        if (component.modelanim != -1) lines.add("modelanim=" + Unpacker.format(Type.SEQ, component.modelanim)); // if_setmodelanim
        if (component.modelanimactive != -1) lines.add("modelanimactive=" + Unpacker.format(Type.SEQ, component.modelanimactive)); // if1 only
        if (component.modelorthog) lines.add("modelorthog=yes"); // if_setmodelorthog
        if (component.unknown1 != 0) lines.add("unknown1=" + component.unknown1); // if_setmodelorthog
        if (component.modelobjwidth != 0) lines.add("modelobjwidth=" + component.modelobjwidth); // todo
        if (component.modelobjheight != 0) lines.add("modelobjheight=" + component.modelobjheight); // todo
    }

    private static void unpackText(ArrayList<String> lines, Component text) {
        if (text.textfont != -1) lines.add("textfont=" + Unpacker.format(Type.GRAPHIC, text.textfont)); // if_settextfont
        if (!text.text.isEmpty()) lines.add("text=" + text.text); // if_settext
        if (text.textactive != null && !text.textactive.isEmpty()) lines.add("textactive=" + text.textactive); // if1 only
        if (text.textlineheight != 0) lines.add("textlineheight=" + text.textlineheight); // todo
        if (text.textalignh != 0) lines.add("textalignh=" + formatAlignH(text.textalignh)); // if_settextalign
        if (text.textalignv != 0) lines.add("textalignv=" + formatAlignV(text.textalignv)); // if_settextalign
        if (text.textshadow) lines.add("textshadow=yes"); // if_settextshadow
        if (text.colour != 0xffffff) lines.add("colour=" + Unpacker.formatColour(text.colour)); // if_setcolour
        if (text.colouractive != 0) lines.add("colouractive=" + Unpacker.formatColour(text.colouractive)); // if1 only
        if (text.mouseovercolour != 0) lines.add("mouseovercolour=" + Unpacker.formatColour(text.mouseovercolour)); // if1 only
        if (text.mouseovercolouractive != 0) lines.add("mouseovercolouractive=" + Unpacker.formatColour(text.mouseovercolouractive)); // if1 only
    }

    private static void unpackRectangle(ArrayList<String> lines, Component component) {
        lines.add("colour=" + Unpacker.formatColour(component.colour)); // if_setcolour
        if (component.colouractive != 0) lines.add("colouractive=" + Unpacker.formatColour(component.colouractive)); // if1 only
        if (component.mouseovercolour != 0) lines.add("mouseovercolour=" + Unpacker.formatColour(component.mouseovercolour)); // if1 only
        if (component.mouseovercolouractive != 0) lines.add("mouseovercolouractive=" + Unpacker.formatColour(component.mouseovercolouractive)); // if1 only
        if (component.fill) lines.add("fill=yes"); // if_setfill
        if (component.trans != 0) lines.add("trans=" + component.trans); // if_settrans
    }

    private static void unpackLine(ArrayList<String> lines, Component component) {
        if (component.linewid != 1) lines.add("linewid=" + component.linewid); // if_setlinewid
        lines.add("colour=" + Unpacker.formatColour(component.colour)); // if_setcolour
        if (component.linedirection) lines.add("linedirection=yes"); // if_setlinedirection
    }

    private static void unpackCircle(ArrayList<String> lines, Component component) {
        lines.add("colour=" + Unpacker.formatColour(component.colour)); // if_setcolour
        if (component.fill) lines.add("fill=yes"); // if_setfill
        if (component.trans != 0) lines.add("trans=" + component.trans); // if_settrans
        lines.add("arc=" + component.arcstart + "," + component.arcend);
        if (component.linewid != 1) lines.add("linewid=" + component.linewid); // if_setlinewid
    }

    public static String formatSizeMode(int sizemode) {
        return switch (sizemode) {
            case 0 -> "abs";
            case 1 -> "minus";
            case 2 -> "rel";
            case 3 -> "mode_3";
            case 4 -> "aspect";
            default -> throw new IllegalStateException("Unexpected value: " + sizemode);
        };
    }

    public static String formatXMode(int xmode) {
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

    public static String formatYMode(int ymode) {
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
            case 3 -> "unknown_3"; // todo
            default -> throw new IllegalStateException();
        };
    }

    private static String formatAlignV(int id) {
        return switch (id) {
            case 0 -> "top";
            case 1 -> "centre";
            case 2 -> "bottom";
            case 3 -> "unknown_3"; // todo
            default -> throw new IllegalStateException();
        };
    }

    public static String formatIfType(int type) {
        return switch (type) {
            case 0 -> "layer";
            case 1 -> "inputbox";
            case 2 -> "inv";
            case 3 -> "rectangle";
            case 4 -> "text";
            case 5 -> "graphic";
            case 6 -> "model";
            case 7 -> "invtext";
            case 8 -> "tooltip";
            case 9 -> "line";
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
