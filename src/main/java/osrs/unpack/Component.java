package osrs.unpack;

import osrs.Unpack;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class Component {
    public final int id;
    public boolean scripted;
    public int type;
    public int buttontype;
    public String buttontext = "";
    public int clientcode;

    public int x;
    public int y;
    public int width;
    public int height;
    public int widthmode;
    public int heightmode;
    public int xmode;
    public int ymode;
    public int layer = -1;
    public int mouseoverlayer = -1;
    public boolean hide;
    public boolean noclickthrough;

    // if1 only
    public int legacytrans;
    public int[] scriptComparison = new int[0];
    public int[] scriptComparisonValue = new int[0];
    public int[][] scriptInstructions = new int[0][];

    // specific properties
    public int scrollwidth;
    public int scrollheight;
    public int graphic = -1;
    public int graphicactive = -1;
    public int angle2d;
    public boolean tiling;
    public int trans;
    public int outline;
    public int graphicshadow;
    public boolean vflip;
    public boolean hflip;
    public int model = -1;
    public int modelactive = -1;
    public int modelorigin_x;
    public int modelorigin_y;
    public int modelangle_x;
    public int modelangle_y;
    public int modelangle_z;
    public int modelzoom = 100;
    public int modelanim = -1;
    public int modelanimactive = -1;
    public boolean modelorthog;
    public int unknown1;
    public int modelobjwidth;
    public int modelobjheight;
    public int textfont = -1;
    public String text = "";
    public String textactive = "";
    public int textlineheight;
    public int textalignh;
    public int textalignv;
    public boolean textshadow;
    public int colour;
    public int colouractive;
    public int mouseovercolour;
    public int mouseovercolouractive;
    public boolean fill;
    public int linewid = 1;
    public boolean linedirection;
    public int arcstart;
    public int arcend;
    public boolean draggable;
    public boolean interactable;
    public boolean usable;
    public boolean swappable;
    public int paddingx;
    public int paddingy;
    public int[] slotoffsetx = new int[20];
    public int[] slotoffsety = new int[20];
    public int[] sloticon = new int[20];
    public String[] objops = new String[5];
    public int unknown200;
    public int unknown201;

    // ops
    public String opbase = "";
    public String targetverb = "";
    public String targetbase = "";
    public String[] ops = new String[0];
    public int events;
    public int dragrenderbehaviour;
    public int dragdeadzone;
    public int dragdeadtime;
    public List<Object[]> hooks = new ArrayList<>();
    public Object[] onload;
    public Object[] onmouseover;
    public Object[] onmouseleave;
    public Object[] ontargetleave;
    public Object[] ontargetenter;
    public Object[] onvartransmit;
    public Object[] oninvtransmit;
    public Object[] onstattransmit;
    public Object[] ontimer;
    public Object[] onop;
    public Object[] onmouserepeat;
    public Object[] onclick;
    public Object[] onclickrepeat;
    public Object[] onrelease;
    public Object[] onhold;
    public Object[] ondrag;
    public Object[] ondragcomplete;
    public Object[] onscrollwheel;
    public int[] onvartransmitlist;
    public int[] oninvtransmitlist;
    public int[] onstattransmitlist;

    public Component(int id, byte[] data) {
        this.id = id;
        var packet = new Packet(data);

        if ((packet.arr[packet.pos] & 0xff) != 0xff) {
            decodeOld(packet);
        } else {
            packet.g1();
            decodeNew(packet);
        }

        if (packet.pos != packet.arr.length) {
            throw new IllegalStateException("end of file not reached");
        }
    }

    private void decodeOld(Packet packet) {
        scripted = false;
        type = packet.g1();
        buttontype = packet.g1();
        clientcode = packet.g2();
        x = packet.g2s();
        y = packet.g2s();
        width = packet.g2();
        height = packet.g2();
        legacytrans = packet.g1();
        layer = packet.g2null();
        mouseoverlayer = packet.g2null();

        var comparisoncount = packet.g1();
        scriptComparison = new int[comparisoncount];
        scriptComparisonValue = new int[comparisoncount];

        for (var i = 0; i < comparisoncount; i++) {
            scriptComparison[i] = packet.g1();
            scriptComparisonValue[i] = packet.g2();
        }

        var instructioncount = packet.g1();
        scriptInstructions = new int[instructioncount][];

        for (var i = 0; i < instructioncount; i++) {
            scriptInstructions[i] = new int[packet.g2()];

            for (var j = 0; j < scriptInstructions[i].length; j++) {
                scriptInstructions[i][j] = packet.g2null();
            }
        }

        switch (type) {
            case 0 -> {
                scrollheight = packet.g2();
                hide = packet.g1() == 1;
            }

            case 2 -> {
                draggable = packet.g1() == 1; // 0x10000000
                interactable = packet.g1() == 1; // 0x40000000
                usable = packet.g1() == 1; // 0x80000000
                swappable = packet.g1() == 1; // 0x20000000
                paddingx = packet.g1();
                paddingy = packet.g1();

                for (var i = 0; i < 20; i++) {
                    if (packet.g1() == 1) {
                        slotoffsetx[i] = packet.g2s();
                        slotoffsety[i] = packet.g2s();

                        sloticon[i] = packet.g4s();
                    } else {
                        sloticon[i] = -1;
                    }
                }

                for (var i = 0; i < 5; i++) {
                    objops[i] = packet.gjstr();
                }
            }

            case 1 -> {
                unknown200 = packet.g2();
                unknown201 = packet.g1();
                textalignh = packet.g1();
                textalignv = packet.g1();
                textlineheight = packet.g1();
                textfont = packet.g2null();
                textshadow = packet.g1() == 1;
                textalignh = packet.g1();
                textalignv = packet.g1();
                textlineheight = packet.g1();
                textfont = packet.g2null();
                textshadow = packet.g1() == 1;
                colour = packet.g4s();
            }

            case 3 -> {
                fill = packet.g1() == 1;
                colour = packet.g4s();
                colouractive = packet.g4s();
                mouseovercolour = packet.g4s();
                mouseovercolouractive = packet.g4s();
            }

            case 4 -> {
                textalignh = packet.g1();
                textalignv = packet.g1();
                textlineheight = packet.g1();
                textfont = packet.g2null();
                textshadow = packet.g1() == 1;
                text = packet.gjstr();
                textactive = packet.gjstr();
                colour = packet.g4s();
                colouractive = packet.g4s();
                mouseovercolour = packet.g4s();
                mouseovercolouractive = packet.g4s();
            }

            case 5 -> {
                graphic = packet.g4s();
                graphicactive = packet.g4s();
            }

            case 6 -> {
                model = Unpack.VERSION >= 237 ? packet.g4s() : packet.g2null();
                modelactive = Unpack.VERSION >= 237 ? packet.g4s() : packet.g2null();
                modelanim = packet.g2null();
                modelanimactive = packet.g2null();
                modelzoom = packet.g2();
                modelangle_x = packet.g2();
                modelangle_y = packet.g2();
            }

            case 7 -> {
                textalignh = packet.g1();
                textfont = packet.g2null();
                textshadow = packet.g1() == 1;
                colour = packet.g4s();
                paddingx = packet.g2s();
                paddingy = packet.g2s();
                interactable = packet.g1() == 1; // 0x40000000

                for (var i = 0; i < 5; i++) {
                    objops[i] = packet.gjstr();
                }
            }

            case 8 -> {
                text = packet.gjstr();
            }
        }

        if (buttontype == 2 || type == 2) {
            targetverb = packet.gjstr();
            targetbase = packet.gjstr();
            events |= (packet.g2() & 63) << 11; // targetmask
        }

        if (buttontype == 1 || buttontype == 4 || buttontype == 5 || buttontype == 6) {
            buttontext = packet.gjstr();
        }
    }

    private void decodeNew(Packet packet) {
        scripted = true;
        type = packet.g1();
        clientcode = packet.g2();
        x = packet.g2s();
        y = packet.g2s();
        width = packet.g2();
        height = type == 9 ? packet.g2s() : packet.g2();

        if (Unpack.VERSION >= 79) {
            widthmode = packet.g1s();
            heightmode = packet.g1s();
            xmode = packet.g1s();
            ymode = packet.g1s();
        }

        layer = packet.g2null();
        hide = packet.g1() == 1;

        switch (type) {
            case 0 -> decodeLayer(packet);
            case 3 -> decodeRectangle(packet);
            case 4 -> decodeText(packet);
            case 5 -> decodeGraphic(packet);
            case 6 -> decodeModel(packet);
            case 9 -> decodeLine(packet);
            case 10 -> decodeCircle(packet);
            default -> throw new AssertionError("invalid type " + type);
        }

        events = packet.g3();
        opbase = packet.gjstr();
        var opcount = packet.g1();
        if (opcount > 0) {
            ops = new String[opcount];
            for (var i = 0; i < opcount; ++i) {
                ops[i] = packet.gjstr();
            }
        }

        dragdeadzone = packet.g1();
        dragdeadtime = packet.g1();
        dragrenderbehaviour = packet.g1();
        targetverb = packet.gjstr();

        onload = decodeHook(packet);
        onmouseover = decodeHook(packet);
        onmouseleave = decodeHook(packet);
        ontargetleave = decodeHook(packet);
        ontargetenter = decodeHook(packet);
        onvartransmit = decodeHook(packet);
        oninvtransmit = decodeHook(packet);
        onstattransmit = decodeHook(packet);
        ontimer = decodeHook(packet);
        onop = decodeHook(packet);
        onmouserepeat = decodeHook(packet);
        onclick = decodeHook(packet);
        onclickrepeat = decodeHook(packet);
        onrelease = decodeHook(packet);
        onhold = decodeHook(packet);
        ondrag = decodeHook(packet);
        ondragcomplete = decodeHook(packet);
        onscrollwheel = decodeHook(packet);

        onvartransmitlist = decodeHookTransmitList(packet);
        oninvtransmitlist = decodeHookTransmitList(packet);
        onstattransmitlist = decodeHookTransmitList(packet);
    }

    private void decodeLayer(Packet packet) {
        scrollwidth = packet.g2();
        scrollheight = packet.g2();

        if (Unpack.VERSION >= 79) {
            noclickthrough = packet.g1() == 1;
        }
    }

    private void decodeGraphic(Packet packet) {
        graphic = packet.g4s();
        angle2d = packet.g2();
        tiling = packet.g1() == 1;
        trans = packet.g1();
        outline = packet.g1();
        graphicshadow = packet.g4s();
        vflip = packet.g1() == 1;
        hflip = packet.g1() == 1;
    }

    private void decodeModel(Packet packet) {
        model = Unpack.VERSION >= 237 ? packet.g4s() : packet.g2null();
        modelorigin_x = packet.g2s();
        modelorigin_y = packet.g2s();
        modelangle_x = packet.g2();
        modelangle_y = packet.g2();
        modelangle_z = packet.g2();
        modelzoom = packet.g2();
        modelanim = packet.g2null();
        modelorthog = packet.g1() == 1;

        if (Unpack.VERSION >= 79) {
            unknown1 = packet.g2();

            if (widthmode != 0 || heightmode != 0) { // todo: client has bug in decoding
                modelobjwidth = packet.g2();
                modelobjheight = packet.g2();
            }
        }
    }

    private void decodeText(Packet packet) {
        textfont = packet.g2null();
        text = packet.gjstr();
        textlineheight = packet.g1();
        textalignh = packet.g1();
        textalignv = packet.g1();
        textshadow = packet.g1() == 1;
        colour = packet.g4s(); // if_setcolour
    }

    private void decodeRectangle(Packet packet) {
        colour = packet.g4s();
        fill = packet.g1() == 1;
        trans = packet.g1();
    }

    private void decodeLine(Packet packet) {
        linewid = packet.g1();
        colour = packet.g4s();

        if (Unpack.VERSION >= 79) {
            linedirection = packet.g1() == 1;
        }
    }

    private void decodeCircle(Packet packet) {
        colour = packet.g4s();
        fill = packet.g1() == 1;
        trans = packet.g1();
        arcstart = packet.g2();
        arcend = packet.g2();

        if (!fill) {
            linewid = packet.g1();
        }
    }

    private Object[] decodeHook(Packet packet) {
        var count = packet.g1();

        if (count == 0) {
            return null;
        }

        packet.g1();
        var hook = new Object[count];
        hook[0] = packet.g4s();

        for (var i = 0; i < count - 1; ++i) {
            var type = packet.g1();

            hook[i + 1] = switch (type) {
                case 0 -> packet.g4s();
                case 1 -> packet.gjstr();
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
        }

        hooks.add(hook);
        return hook;
    }

    private static int[] decodeHookTransmitList(Packet packet) {
        var count = packet.g1();

        if (count == 0) {
            return null;
        }

        var ids = new int[count];

        for (var i = 0; i < count; ++i) {
            ids[i] = packet.g4s();
        }

        return ids;
    }
}
