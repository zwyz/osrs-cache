package osrs.util;

public class CP1252 {
    public static final char[] ASCII_EXTENSION = new char[]{'€', '\u0000', '‚', 'ƒ', '„', '…', '†', '‡', 'ˆ', '‰', 'Š', '‹', 'Œ', '\u0000', 'Ž', '\u0000', '\u0000', '‘', '’', '“', '”', '•', '–', '—', '˜', '™', 'š', '›', 'œ', '\u0000', 'ž', 'Ÿ'};

    public static byte encode(char c) {
        byte result;

        if ((c > 0 && c < 128) || (c >= 160 && c <= 255)) {
            result = (byte) c;
        } else if (c == 8364) {
            result = -128;
        } else if (c == 8218) {
            result = -126;
        } else if (c == 402) {
            result = -125;
        } else if (c == 8222) {
            result = -124;
        } else if (c == 8230) {
            result = -123;
        } else if (c == 8224) {
            result = -122;
        } else if (c == 8225) {
            result = -121;
        } else if (c == 710) {
            result = -120;
        } else if (c == 8240) {
            result = -119;
        } else if (c == 352) {
            result = -118;
        } else if (c == 8249) {
            result = -117;
        } else if (c == 338) {
            result = -116;
        } else if (c == 381) {
            result = -114;
        } else if (c == 8216) {
            result = -111;
        } else if (c == 8217) {
            result = -110;
        } else if (c == 8220) {
            result = -109;
        } else if (c == 8221) {
            result = -108;
        } else if (c == 8226) {
            result = -107;
        } else if (c == 8211) {
            result = -106;
        } else if (c == 8212) {
            result = -105;
        } else if (c == 732) {
            result = -104;
        } else if (c == 8482) {
            result = -103;
        } else if (c == 353) {
            result = -102;
        } else if (c == 8250) {
            result = -101;
        } else if (c == 339) {
            result = -100;
        } else if (c == 382) {
            result = -98;
        } else if (c == 376) {
            result = -97;
        } else {
            result = 63;
        }

        return result;
    }

    public static int encode(CharSequence s, int min, int max, byte[] arr, int off) {
        var len = max - min;

        for (var i = 0; i < len; ++i) {
            arr[off + i] = encode(s.charAt(min + i));
        }

        return len;
    }

    public static String decode(byte[] arr, int off, int len) {
        var chars = new char[len];
        var j = 0;

        for (var i = 0; i < len; ++i) {
            var c = arr[off + i] & 255;

            if (c != 0) {
                if (c >= 128 && c < 160) {
                    var d = ASCII_EXTENSION[c - 128];

                    if (d == 0) {
                        d = '?';
                    }

                    c = d;
                }

                chars[j++] = (char) c;
            }
        }

        return new String(chars, 0, j);
    }
}
