package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.List;

public class ParamUnpackHelper {
    public static void unpack(List<String> lines, Packet packet) {
        var count = packet.g1();

        for (var i = 0; i < count; i++) {
            var type = packet.g1();
            var param = packet.g3();

            switch (type) {
                case 0 -> lines.add("param=" + Unpacker.format(Type.PARAM, param) + "," + Unpacker.format(Unpacker.getParamType(param), packet.g4s()));
                case 1 -> lines.add("param=" + Unpacker.format(Type.PARAM, param) + "," + packet.gjstr());
                case 2 -> lines.add("param=" + Unpacker.format(Type.PARAM, param) + "," + Unpacker.format(Unpacker.getParamType(param), packet.g8s()));
                default -> throw new IllegalStateException("unknown type");
            }
        }
    }
}
