package osrs.unpack;

import osrs.Unpack;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class IfType {

    public boolean scripted;
    public int type;
    public int contenttype;
    public int x;
    public int y;
    public int width;
    public int height;
    public int widthmode;
    public int heightmode;
    public int xmode;
    public int ymode;
    public int layerID = -1;
    public boolean hide;
    public int scrollwidth;
    public int scrollheight;
    public boolean noclickthrough;
    public int graphic = -1;
    public int angle2d;
    public boolean tiling;
    public int trans;
    public int outline;
    public int graphicshadow;
    public boolean vflip;
    public boolean hflip;
    public int model = -1;
    public int modeloriginx;
    public int modeloriginy;
    public int modelanglex;
    public int modelangley;
    public int modelanglez;
    public int modelzoom = 100;
    public int modelanim = -1;
    public boolean modelorthog;
    public int unknown1;
    public int modelobjwidth;
    public int modelobjheight;
    public int textfont = -1;
    public String text = "";
    public int textlineheight;
    public int textalignh;
    public int textalignv;
    public boolean textshadow;
    public int colour;
    public boolean fill;
    public int linewid = 1;
    public boolean linedirection;
    public int arcstart;
    public int arcend;
    public int events;
    public String opbase = "";
    public String[] ops;
    public int dragdeadzone;
    public int dragdeadtime;
    public int dragrenderbehaviour;
    public String targetverb = "";
    public IfTypeHook onload;
    public IfTypeHook onmouseover;
    public IfTypeHook onmouseleave;
    public IfTypeHook ontargetleave;
    public IfTypeHook ontargetenter;
    public IfTypeHook onvartransmit;
    public IfTypeHook oninvtransmit;
    public IfTypeHook onstattransmit;
    public IfTypeHook ontimer;
    public IfTypeHook onop;
    public IfTypeHook onmouserepeat;
    public IfTypeHook onclick;
    public IfTypeHook onclickrepeat;
    public IfTypeHook onrelease;
    public IfTypeHook onhold;
    public IfTypeHook ondrag;
    public IfTypeHook ondragcomplete;
    public IfTypeHook onscrollwheel;
    public int[] onvartransmitlist;
    public int[] oninvtransmitlist;
    public int[] onstattransmitlist;

    public IfType(int id, byte[] data) {
        var packet = new Packet(data);
        var version = packet.g1();

        scripted = version == 255;
        if (!scripted) {
            return;
        }

        if (version == 255) {
            version = -1;
        }

        type = packet.g1();
        contenttype = packet.g2();
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

        layerID = (id & ~0xffff) | packet.g2null();

        hide = packet.g1() == 1;

        switch (type) {
            case 0 -> decodeLayer(packet, version);
            case 3 -> decodeRectangle(packet, version);
            case 4 -> decodeText(packet, version);
            case 5 -> decodeGraphic(packet, version);
            case 6 -> decodeModel(packet, version);
            case 9 -> decodeLine(packet, version);
            case 10 -> decodeCircle(packet, version);
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

        if (packet.pos != packet.arr.length) {
            throw new IllegalStateException("end of file not reached");
        }
    }

    private void decodeLayer(Packet packet, int version) {
        scrollwidth = packet.g2();
        scrollheight = packet.g2();

        if (Unpack.VERSION >= 79) {
            noclickthrough = packet.g1() == 1;
        }
    }

    private void decodeGraphic(Packet packet, int version) {
        graphic = packet.g4s();
        angle2d = packet.g2();
        tiling = packet.g1() == 1;
        trans = packet.g1();
        outline = packet.g1();
        graphicshadow = packet.g4s();
        vflip = packet.g1() == 1;
        hflip = packet.g1() == 1;
    }

    private void decodeModel(Packet packet, int version) {
        model = packet.g2null();
        modeloriginx = packet.g2s();
        modeloriginy = packet.g2s();
        modelanglex = packet.g2();
        modelangley = packet.g2();
        modelanglez = packet.g2();
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

    private void decodeText(Packet packet, int version) {
        textfont = packet.g2null();
        text = packet.gjstr();
        textlineheight = packet.g1();
        textalignh = packet.g1();
        textalignv = packet.g1();
        textshadow = packet.g1() == 1;
        colour = packet.g4s(); // if_setcolour
    }

    private void decodeRectangle(Packet packet, int version) {
        colour = packet.g4s();
        fill = packet.g1() == 1;
        trans = packet.g1();
    }

    private void decodeLine(Packet packet, int version) {
        linewid = packet.g1();
        colour = packet.g4s();

        if (Unpack.VERSION >= 79) {
            linedirection = packet.g1() == 1;
        }
    }

    private void decodeCircle(Packet packet, int version) {
        colour = packet.g4s();
        fill = packet.g1() == 1;
        trans = packet.g1();
        arcstart = packet.g2();
        arcend = packet.g2();

        if (!fill) {
            linewid = packet.g1();
        }
    }

    public List<IfTypeHook> hooks() {
        var hooks = new ArrayList<IfTypeHook>(18);
        if (onload != null) {
            hooks.add(onload);
        }
        if (onmouseover != null) {
            hooks.add(onmouseover);
        }
        if (onmouseleave != null) {
            hooks.add(onmouseleave);
        }
        if (ontargetleave != null) {
            hooks.add(ontargetleave);
        }
        if (ontargetenter != null) {
            hooks.add(ontargetenter);
        }
        if (onvartransmit != null) {
            hooks.add(onvartransmit);
        }
        if (oninvtransmit != null) {
            hooks.add(oninvtransmit);
        }
        if (onstattransmit != null) {
            hooks.add(onstattransmit);
        }
        if (ontimer != null) {
            hooks.add(ontimer);
        }
        if (onop != null) {
            hooks.add(onop);
        }
        if (onmouserepeat != null) {
            hooks.add(onmouserepeat);
        }
        if (onclick != null) {
            hooks.add(onclick);
        }
        if (onclickrepeat != null) {
            hooks.add(onclickrepeat);
        }
        if (onrelease != null) {
            hooks.add(onrelease);
        }
        if (onhold != null) {
            hooks.add(onhold);
        }
        if (ondrag != null) {
            hooks.add(ondrag);
        }
        if (ondragcomplete != null) {
            hooks.add(ondragcomplete);
        }
        if (onscrollwheel != null) {
            hooks.add(onscrollwheel);
        }
        return hooks;
    }


    private static IfTypeHook decodeHook(Packet packet) {
        var count = packet.g1();

        if (count == 0) {
            return null;
        }

        packet.g1();
        var script = packet.g4s();
        var arguments = new ArrayList<>(count - 1);

        for (var i = 0; i < count - 1; ++i) {
            var value = switch (packet.g1()) {
                case 0 -> packet.g4s();
                case 1 -> packet.gjstr();
                default -> throw new IllegalStateException("Unexpected value: " + packet.g1());
            };

            arguments.add(value);

        }

        return new IfTypeHook(script, arguments);
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

    public record IfTypeHook(int id, List<Object> args) {
    }
}
    