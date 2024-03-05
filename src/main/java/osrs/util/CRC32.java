package osrs.util;

public class CRC32 {
    private static final int[] CRC_32 = new int[256];

    static {
        for (var i = 0; i < 256; ++i) {
            var entry = i;

            for (var j = 0; j < 8; ++j) {
                if ((entry & 1) == 1) {
                    entry = entry >>> 1 ^ 0xedb88320;
                } else {
                    entry >>>= 1;
                }
            }

            CRC_32[i] = entry;
        }
    }

    public static int crc(byte[] data) {
        return crc(data, 0, data.length);
    }

    public static int crc(byte[] data, int offset, int length) {
        var crc = -1;

        for (var i = offset; i < length; ++i) {
            crc = crc >>> 8 ^ CRC_32[(crc ^ data[i]) & 255];
        }

        crc = ~crc;
        return crc;
    }
}
