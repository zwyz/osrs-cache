package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class VarClanSettingUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                lines.addFirst("[" + Unpacker.format(Type.VAR_CLAN_SETTING, id, false) + "]");
                return lines;
            }

            case 1 -> {
                var type = packet.g1();
                Unpacker.setVarClanSettingType(id, Type.byChar(type));
                lines.add("type=" + Unpacker.format(Type.TYPE, type));
            }

            case 2 -> lines.add("lifetime=" + packet.g1());
            case 10 -> lines.add("debugname=" + packet.gjstr2());

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
