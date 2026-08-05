package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class MapElementUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.MAPELEMENT, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> lines.add("sprite=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null())); // 216 GetSprite
            case 2 -> lines.add("mouseovergraphic=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null()));
            case 3 -> lines.add("text=" + packet.gjstr()); // 216 GetText, html5 unobfuscated
            case 4 -> lines.add("textcolour=" + Unpacker.formatColour(packet.g3())); // 216 GetTextRGBA
            case 5 -> lines.add("textmouseovercolour=" + Unpacker.formatColour(packet.g3())); // 216 GetTextMouseOverColour
            case 6 -> lines.add("textsize=" + packet.g1()); // 216 GetTextSize

            case 7 -> lines.add("show=" + switch (packet.g1()) {  // 216 GetShowOnWorldMap, GetShowOnMiniMap
                case 0 -> "none";
                case 1 -> "map";
                case 2 -> "minimap";
                case 3 -> "both";
                default -> throw new IllegalStateException();
            });

            case 8 -> lines.add("mapfunction=" + Unpacker.formatYesNo(packet.g1()));
            case 10 -> lines.add("op1=" + packet.gjstr());
            case 11 -> lines.add("op2=" + packet.gjstr());
            case 12 -> lines.add("op3=" + packet.gjstr());
            case 13 -> lines.add("op4=" + packet.gjstr());
            case 14 -> lines.add("op5=" + packet.gjstr());

            case 15 -> { // 216 GetPolygon
                var points = packet.g1();

                for (var i = 0; i < points; ++i) {
                    lines.add("polygonpoint" + i + "=" + packet.g2s() + "," + packet.g2s());
                }

                lines.add("polygonfill=" + Unpacker.formatColour(packet.g4s()));

                var palette = new int[packet.g1()];

                for (var i = 0; i < palette.length; ++i) {
                    palette[i] = packet.g4s();
                }

                if (palette.length == 1) {
                    lines.add("polygonoutline=" + Unpacker.formatColour(palette[0]));

                    for (var i = 0; i < points; ++i) {
                        packet.g1s();
                    }
                } else {
                    for (var i = 0; i < points; ++i) {
                        lines.add("polygonoutline" + i + "=" + Unpacker.formatColour(palette[packet.g1s()]));
                    }
                }
            }

            case 16 -> lines.add("listable=no"); // 216 GetListable
            case 17 -> lines.add("opbase=" + packet.gjstr());
            case 18 -> lines.add("worldmaparrow=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null())); // 216 GetWorldmapArrow
            case 19 -> lines.add("category=" + Unpacker.format(Type.CATEGORY, packet.g2())); // 216 GetCategory
            case 21 -> lines.add("textbackgroundoutline=" + Unpacker.formatColour(packet.g4s())); // 216 GetTextBackgroundOutlineRGBA
            case 22 -> lines.add("textbackgroundfill=" + Unpacker.formatColour(packet.g4s())); // 216 GetTextBackgroundFillRGBA
            case 23 -> lines.add("polygonoutlinedash=" + packet.g1() + "," + packet.g1() + "," + packet.g1()); // length, gap, phase
            case 24 -> lines.add("textoffset=" + packet.g2s() + "," + packet.g2s());
            case 25 -> lines.add("flashsprite=" + Unpacker.format(Type.GRAPHIC, packet.gSmart2or4null())); // 216 GetFlashSpriteID
            case 28 -> lines.add("minimapiconscale=" + packet.g1()); // 216 GetMinimapIconScale

            case 29 -> lines.add("halign=" + switch (packet.g1()) { // 216 GetHAlign
                case 0 -> "left";
                case 1 -> "centre";
                case 2 -> "right";
                default -> throw new IllegalStateException();
            });

            case 30 -> lines.add("valign=" + switch (packet.g1()) { // 216 GetVAlign
                case 0 -> "top";
                case 1 -> "centre";
                case 2 -> "bottom";
                default -> throw new IllegalStateException();
            });

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
