package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class AmbienceUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.AMBIENCE, id, false) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> { // play one after another in a loop
                var count = packet.g1();
                var sounds = new ArrayList<String>();

                for (var i = 0; i < count; i++) {
                    sounds.add(Unpacker.format(Type.SYNTH, packet.g2()));
                }

                lines.add("sound=" + String.join(",", sounds));
            }

            case 2 -> { // extra random sound every once in a while
                var delayMin = packet.g2();
                var delayMax = packet.g2();
                var count = packet.g1();
                var sounds = new ArrayList<String>();

                for (var i = 0; i < count; i++) {
                    sounds.add(Unpacker.format(Type.SYNTH, packet.g2()));
                }

                lines.add("randomsound=" + delayMin + "," + delayMax + "," + String.join(",", sounds));
            }

            case 3 -> lines.add("fadein=" + packet.g1() + "," + packet.g2());
            case 4 -> lines.add("fadeout=" + packet.g1() + "," + packet.g2());

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
