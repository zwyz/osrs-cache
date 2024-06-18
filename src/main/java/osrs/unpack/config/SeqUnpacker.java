package osrs.unpack.config;

import osrs.Unpack;
import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeqUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.SEQ, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            case 1 -> {
                var count = packet.g2();
                var delay = new int[count];
                var frame = new int[count];
                var anim = new int[count];

                for (var i = 0; i < count; i++) {
                    delay[i] = packet.g2();
                }

                for (var i = 0; i < count; i++) {
                    frame[i] = packet.g2();
                }

                for (var i = 0; i < count; i++) {
                    anim[i] = packet.g2();
                }

                for (var i = 0; i < count; i++) {
                    lines.add("frame" + (i + 1) + "=anim_" + anim[i] + "_f" + (frame[i] + 1));
                    lines.add("delay" + (i + 1) + "=" + delay[i]);
                }
            }

            case 2 -> lines.add("loopframes=" + packet.g2());

            case 3 -> {
                var count = packet.g1();
                var result = new ArrayList<String>();

                for (var i = 0; i < count; i++) {
                    result.add("label_" + packet.g1());
                }

                lines.add("walkmerge=" + String.join(",", result));
            }

            case 4 -> lines.add("stretches=yes");
            case 5 -> lines.add("priority=" + packet.g1());
            case 6 -> lines.add("lefthand=" + packet.g2());
            case 7 -> lines.add("righthand=" + packet.g2());
            case 8 -> lines.add("loopcount=" + packet.g1());
            case 9 -> lines.add("preanim_move=" + Unpacker.getPreanimMoveName(packet.g1()));
            case 10 -> lines.add("postanim_move=" + Unpacker.getPostanimMoveName(packet.g1()));
            case 11 -> lines.add("replacemode=" + Unpacker.getReplaceModeName(packet.g1()));

            case 12 -> {
                var count = packet.g1();
                var frame = new int[count];
                var anim = new int[count];

                for (var i = 0; i < count; i++) {
                    frame[i] = packet.g2();
                }

                for (var i = 0; i < count; i++) {
                    anim[i] += packet.g2();
                }

                for (var i = 0; i < count; i++) {
                    lines.add("iframe" + (i + 1) + "=anim_" + anim[i] + "_f" + (frame[i] + 1));
                }
            }

            case 13 -> {
                var count = packet.g1();

                // workaround for bug in jagex packer, count overflows
                var hash = Arrays.hashCode(data);

                if (hash == 0xE71C2CA5 || hash == 0x1C8AA6F3 || hash == 0x8A48B202 || hash == 0x48D0CD51) {
                    count += 256;
                }

                if (Unpack.VERSION < 220) {
                    for (var i = 0; i < count; i++) {
                        var value = packet.g3();

                        if (value != 0) {
                            var type = value >> 8;
                            var loops = value >> 4 & 7;
                            var range = value & 15;
                            lines.add("sound" + i + "=" + Unpacker.format(Type.SYNTH, type) + "," + loops + "," + range);
                        }
                    }
                } else {
                    for (var i = 0; i < count; i++) {
                        var type = packet.g2();
                        var loops = packet.g1();
                        var range = packet.g1();
                        var size = packet.g1();

                        if (type != 0 || loops != 0 || range != 0 || size != 0) {
                            lines.add("sound" + i + "=" + Unpacker.format(Type.SYNTH, type) + "," + loops + "," + range + "," + size);
                        }
                    }
                }
            }

            case 14 -> {
                lines.add("keyframeset=" + packet.g4s());
            }

            case 15 -> {
                if (Unpack.VERSION < 220) {
                    var count = packet.g2();

                    for (var i = 0; i < count; i++) {
                        var index = packet.g2();
                        var value = packet.g3();
                        var type = value >> 8;
                        var loops = value >> 4 & 7;
                        var range = value & 15;
                        lines.add("keyframesound" + index + "=" + Unpacker.format(Type.SYNTH, type) + "," + loops + "," + range);
                    }
                } else {
                    var count = packet.g2();

                    for (var i = 0; i < count; i++) {
                        lines.add("keyframesound" + packet.g2() + "=" + Unpacker.format(Type.SYNTH, packet.g2null()) + "," + packet.g1() + "," + packet.g1() + "," + packet.g1());
                    }
                }
            }

            case 16 -> lines.add("keyframerange=" + packet.g2() + "," + packet.g2());

            case 17 -> {
                var count = packet.g1();

                for (var i = 0; i < count; i++) {
                    lines.add("keyframewalkmerge=" + packet.g1());
                }
            }

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
