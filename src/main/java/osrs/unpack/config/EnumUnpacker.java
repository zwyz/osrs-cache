package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class EnumUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var nextAutoIntIndex = 0;
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.ENUM, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                if (nextAutoIntIndex != -1 && false) {
                    for (var i = 0; i < lines.size(); i++) {
                        var line = lines.get(i);

                        if (line.startsWith("inputtype=")) {
                            lines.set(i, "inputtype=autoint");
                        }

                        if (line.startsWith("val=")) {
                            lines.set(i, "val=" + line.substring(line.indexOf(",") + 1));
                        }
                    }
                }

                return lines;
            }

            case 1 -> {
                var type = packet.g1();
                Unpacker.setEnumInputType(id, Type.byChar(type));
                lines.add("inputtype=" + Unpacker.format(Type.TYPE, type));

                if (Type.byChar(type) != Type.INT_INT) {
                    nextAutoIntIndex = -1;
                }
            }

            case 2 -> {
                var type = packet.g1();
                Unpacker.setEnumOutputType(id, Type.byChar(type));
                lines.add("outputtype=" + Unpacker.format(Type.TYPE, type));
            }

            case 3 -> lines.add("default=" + packet.gjstr());
            case 4 -> lines.add("default=" + Unpacker.format(Unpacker.getEnumOutputType(id), packet.g4s()));

            case 5 -> {
                var count = packet.g2();

                for (var i = 0; i < count; ++i) {
                    var key = packet.g4s();
                    var value = packet.gjstr();
                    lines.add("val=" + Unpacker.format(Unpacker.getEnumInputType(id), key) + "," + value);

                    if (nextAutoIntIndex != -1 && key == nextAutoIntIndex) {
                        nextAutoIntIndex++;
                    } else {
                        nextAutoIntIndex = -1;
                    }
                }
            }

            case 6 -> {
                var count = packet.g2();

                for (var i = 0; i < count; ++i) {
                    var key = packet.g4s();
                    var value = packet.g4s();
                    lines.add("val=" + Unpacker.format(Unpacker.getEnumInputType(id), key) + "," + Unpacker.format(Unpacker.getEnumOutputType(id), value));

                    if (nextAutoIntIndex != -1 && key == nextAutoIntIndex) {
                        nextAutoIntIndex++;
                    } else {
                        nextAutoIntIndex = -1;
                    }
                }
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
