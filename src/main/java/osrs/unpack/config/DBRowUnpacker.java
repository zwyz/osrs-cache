package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class DBRowUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.DBROW, id) + "]");


        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 3 -> {
                var a = packet.g1();

                for (var value = packet.g1(); value != 255; value = packet.g1()) {
                    var column = value;
                    var elementCount = packet.g1();
                    var types = new Type[elementCount];

                    for (var j = 0; j < elementCount; ++j) {
                        types[j] = Type.byID(packet.gSmart1or2());
                    }

                    var something = packet.gSmart1or2();

                    for (var i = 0; i < something; i++) {
                        var sb = new StringBuilder("data=" + Unpacker.DBCOLUMN_NAME.getOrDefault((id << 16) | column, "col" + column));

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

            case 4 -> lines.add("table=" + Unpacker.format(Type.DBTABLE, packet.gvarint2()));

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
