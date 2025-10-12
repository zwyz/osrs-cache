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
        lines.add("[" + Unpacker.format(Type.DBROW, id, false) + "]");
        var table = -1;

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 3 -> {
                var a = packet.g1();

                for (var column = packet.g1(); column != 255; column = packet.g1()) {
                    var elementCount = packet.g1();
                    var types = new Type[elementCount];

                    for (var j = 0; j < elementCount; ++j) {
                        types[j] = Type.byID(packet.gSmart1or2());
                    }

                    var count = packet.gSmart1or2();

                    for (var i = 0; i < count; i++) {
                        var s = "data=" + Unpacker.formatDBColumnShort((table << 12) | (column << 4));

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

            case 4 -> {
                table = packet.gvarint2();
                lines.add("table=" + Unpacker.format(Type.DBTABLE, table));
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
