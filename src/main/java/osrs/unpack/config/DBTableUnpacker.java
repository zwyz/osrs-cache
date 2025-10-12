package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBTableUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.DBTABLE, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> {
                var var1 = packet.g1();

                for (var value = packet.g1(); value != 255; value = packet.g1()) {
                    var column = value & 127;
                    var hasdefault = (value & 128) != 0;
                    var length = packet.g1();
                    var types = new ArrayList<Type>(length);

                    for (var i = 0; i < length; ++i) {
                        types.add(Type.byID(packet.gSmart1or2()));
                    }

                    Unpacker.setDBColumnType(id, column, types);
                    lines.add("column=" + Unpacker.formatDBColumnShort((id << 12) | (column << 4)) + "," + types.stream().map(t -> t.name).collect(Collectors.joining(",")));

                    if (hasdefault) {
                        var defaultCount = packet.gSmart1or2();

                        for (var entry = 0; entry < defaultCount; entry++) {
                            var s = "default=" + Unpacker.formatDBColumnShort((id << 12) | (column << 4));

                            for (var type : types) {
                                s += "," + switch (type.base) {
                                    case INTEGER -> Unpacker.format(type, packet.g4s());
                                    case LONG -> Long.toString(packet.g8s());
                                    case STRING -> packet.gjstr();
                                    default -> throw new IllegalStateException("invalid");
                                };
                            }

                            lines.add(s);
                        }
                    }
                }
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
