package osrs.unpack.map;

import osrs.util.Packet;

public class MapTiles {
    public final int[][][] height = new int[5][64][64];
    public final int[][][] underlay = new int[5][64][64];
    public final int[][][] overlay = new int[5][64][64];
    public final int[][][] shape = new int[5][64][64];
    public final int[][][] angle = new int[5][64][64];
    public final int[][][] flags = new int[5][64][64];
    public final boolean underwater;

    public MapTiles(Packet packet) {
        for (var level = 0; level < 4; level++) {
            decodeLevel(packet, level);
        }

        var flags = packet.g1();
        underwater = (flags & 1) != 0;

        if (underwater) {
            decodeLevel(packet, 4);
        }

        if (packet.pos != packet.arr.length) {
            throw new IllegalStateException("end of file not reached");
        }
    }

    private void decodeLevel(Packet packet, int level) {
        for (var x = 0; x < 64; x++) {
            for (var y = 0; y < 64; y++) {
                while (true) {
                    var code = packet.g2();

                    if (code == 0) {
                        break;
                    } else if (code == 1) {
                        height[level][x][y] = packet.g1();
                        break;
                    } else if (code <= 49) {
                        overlay[level][x][y] = packet.g2() & 0xffff;
                        shape[level][x][y] = (code - 2) / 4;
                        angle[level][x][y] = code - 2 & 0x3;
                    } else if (code <= 81) {
                        flags[level][x][y] = code - 49;
                    } else {
                        underlay[level][x][y] = code - 81;
                    }
                }
            }
        }
    }
}
