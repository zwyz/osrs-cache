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
        lines.add("[" + Unpacker.format(Type.DBTABLE, id) + "]");

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
                    lines.add("column=dbcolumn_" + column + "," + types.stream().map(t -> t.name).collect(Collectors.joining(",")));

                    if (hasdefault) {
                        var defaultCount = packet.gSmart1or2();

                        for (var entry = 0; entry < defaultCount; entry++) {
                            var sb = new StringBuilder("default=dbcolumn_" + column);

                            for (var type : types) {
                                sb.append(",").append(switch (type.baseType) {
                                    case INTEGER -> Unpacker.format(type, packet.g4s());
                                    case LONG -> Unpacker.format(type, packet.g8s());
                                    case STRING -> Unpacker.format(type, packet.gjstr());
                                    default -> throw new IllegalStateException();
                                });
                            }

                            lines.add(sb.toString());
                        }
                    }
                }
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}