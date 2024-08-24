package osrs.util;

import java.math.BigInteger;

public class Packet {
    public static int[] CRC32 = new int[256];
    public static long[] CRC64;
    public static final int[] field5134 = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535, 131071, 262143, 524287, 1048575, 2097151, 4194303, 8388607, 16777215, 33554431, 67108863, 134217727, 268435455, 536870911, 1073741823, Integer.MAX_VALUE, -1};

    static {
        for (var var0 = 0; var0 < 256; ++var0) {
            var var1 = var0;

            for (var var2 = 0; var2 < 8; ++var2) {
                if ((var1 & 1) == 1) {
                    var1 = var1 >>> 1 ^ -306674912;
                } else {
                    var1 >>>= 1;
                }
            }

            CRC32[var0] = var1;
        }

        CRC64 = new long[256];

        for (var var3 = 0; var3 < 256; ++var3) {
            var var4 = (long) var3;

            for (var var6 = 0; var6 < 8; ++var6) {
                if ((var4 & 1L) == 1L) {
                    var4 = var4 >>> 1 ^ -3932672073523589310L;
                } else {
                    var4 >>>= 1;
                }
            }

            CRC64[var3] = var4;
        }
    }

    public byte[] arr;
    public int pos;
    public int bitpos;

    public static int computeCRC(byte[] var0, int var1, int var2) {
        var var3 = -1;

        for (var var4 = var1; var4 < var2; ++var4) {
            var3 = var3 >>> 8 ^ CRC32[(var3 ^ var0[var4]) & 255];
        }

        var var5 = ~var3;
        return var5;
    }

    public static int computeCRC(byte[] var0, int var1) {
        return computeCRC(var0, 0, var1);
    }

    public Packet(int var1) {
        arr = new byte[var1];
        pos = 0;
    }

    public Packet(byte[] var1) {
        arr = var1;
        pos = 0;
    }

    public static Packet create(int i) {
        return new Packet(i);
    }

    public void p1(int var1) {
        arr[++pos - 1] = (byte) var1;
    }

    public void p2(int var1) {
        arr[++pos - 1] = (byte) (var1 >> 8);
        arr[++pos - 1] = (byte) var1;
    }

    public void p3(int var1) {
        arr[++pos - 1] = (byte) (var1 >> 16);
        arr[++pos - 1] = (byte) (var1 >> 8);
        arr[++pos - 1] = (byte) var1;
    }

    public void p4(int var1) {
        arr[++pos - 1] = (byte) (var1 >> 24);
        arr[++pos - 1] = (byte) (var1 >> 16);
        arr[++pos - 1] = (byte) (var1 >> 8);
        arr[++pos - 1] = (byte) var1;
    }

    public void p6(long var1) {
        arr[++pos - 1] = (byte) (int) (var1 >> 40);
        arr[++pos - 1] = (byte) (int) (var1 >> 32);
        arr[++pos - 1] = (byte) (int) (var1 >> 24);
        arr[++pos - 1] = (byte) (int) (var1 >> 16);
        arr[++pos - 1] = (byte) (int) (var1 >> 8);
        arr[++pos - 1] = (byte) (int) var1;
    }

    public void p8(long var1) {
        arr[++pos - 1] = (byte) (int) (var1 >> 56);
        arr[++pos - 1] = (byte) (int) (var1 >> 48);
        arr[++pos - 1] = (byte) (int) (var1 >> 40);
        arr[++pos - 1] = (byte) (int) (var1 >> 32);
        arr[++pos - 1] = (byte) (int) (var1 >> 24);
        arr[++pos - 1] = (byte) (int) (var1 >> 16);
        arr[++pos - 1] = (byte) (int) (var1 >> 8);
        arr[++pos - 1] = (byte) (int) var1;
    }

    public void pBoolean(boolean var1) {
        p1(var1 ? 1 : 0);
    }

    public static int sizejstr(String var0) {
        return var0.length() + 1;
    }

    public void pjstr(String var1) {
        var var2 = var1.indexOf(0);
        if (var2 >= 0) {
            throw new IllegalArgumentException("");
        } else {
            pos += CP1252.encode(var1, 0, var1.length(), arr, pos);
            arr[++pos - 1] = 0;
        }
    }

    public void pjstr2(String var1) {
        var var2 = var1.indexOf(0);
        if (var2 >= 0) {
            throw new IllegalArgumentException("");
        } else {
            arr[++pos - 1] = 0;
            pos += CP1252.encode(var1, 0, var1.length(), arr, pos);
            arr[++pos - 1] = 0;
        }
    }

    public void pUTF8(CharSequence var1) {
        var var2 = var1.length();
        var var3 = 0;

        for (var var4 = 0; var4 < var2; ++var4) {
            var var5 = var1.charAt(var4);
            if (var5 <= 127) {
                ++var3;
            } else if (var5 <= 2047) {
                var3 += 2;
            } else {
                var3 += 3;
            }
        }

        arr[++pos - 1] = 0;
        pVarInt(var3);
        var var8 = pos;
        var var9 = arr;
        var var10 = pos;
        var var11 = var1.length();
        var var12 = var10;

        for (var var13 = 0; var13 < var11; ++var13) {
            var var14 = var1.charAt(var13);
            if (var14 <= 127) {
                var9[var12++] = (byte) var14;
            } else if (var14 <= 2047) {
                var9[var12++] = (byte) (192 | var14 >> 6);
                var9[var12++] = (byte) (128 | var14 & 63);
            } else {
                var9[var12++] = (byte) (224 | var14 >> 12);
                var9[var12++] = (byte) (128 | var14 >> 6 & 63);
                var9[var12++] = (byte) (128 | var14 & 63);
            }
        }

        var var15 = var12 - var10;
        pos = var15 + var8;
    }

    public void pdata(byte[] var1, int var2, int var3) {
        for (var var4 = var2; var4 < var2 + var3; ++var4) {
            arr[++pos - 1] = var1[var4];
        }

    }

    public void pdata(Packet var1) {
        pdata(var1.arr, 0, var1.pos);
    }

    public void psize4(int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        } else {
            arr[pos - size - 4] = (byte) (size >> 24);
            arr[pos - size - 3] = (byte) (size >> 16);
            arr[pos - size - 2] = (byte) (size >> 8);
            arr[pos - size - 1] = (byte) size;
        }
    }

    public void psize2(int var1) {
        if (var1 >= 0 && var1 <= 65535) {
            arr[pos - var1 - 2] = (byte) (var1 >> 8);
            arr[pos - var1 - 1] = (byte) var1;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void psize1(int var1) {
        if (var1 >= 0 && var1 <= 255) {
            arr[pos - var1 - 1] = (byte) var1;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void pSmart1or2(int var1) {
        if (var1 >= 0 && var1 < 128) {
            p1(var1);
        } else if (var1 >= 0 && var1 < 32768) {
            p2(var1 + '耀');
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void pVarInt(int var1) {
        if ((var1 & -128) != 0) {
            if ((var1 & -16384) != 0) {
                if ((var1 & -2097152) != 0) {
                    if ((var1 & -268435456) != 0) {
                        p1(var1 >>> 28 | 128);
                    }

                    p1(var1 >>> 21 | 128);
                }

                p1(var1 >>> 14 | 128);
            }

            p1(var1 >>> 7 | 128);
        }

        p1(var1 & 127);
    }

    public int g1() {
        return arr[++pos - 1] & 255;
    }

    public byte g1s() {
        return arr[++pos - 1];
    }

    public int g2() {
        pos += 2;
        return ((arr[pos - 2] & 255) << 8) + (arr[pos - 1] & 255);
    }

    public int g2null() {
        var result = g2();
        return result == 0xffff ? -1 : result;
    }

    public int g2s() {
        pos += 2;
        var var1 = ((arr[pos - 2] & 255) << 8) + (arr[pos - 1] & 255);
        if (var1 > 32767) {
            var1 -= 65536;
        }

        return var1;
    }

    public int g3() {
        pos += 3;
        return (arr[pos - 1] & 255) + ((arr[pos - 3] & 255) << 16) + ((arr[pos - 2] & 255) << 8);
    }

    public int g3s() {
        pos += 3;
        var var1 = (arr[pos - 1] & 255) + ((arr[pos - 3] & 255) << 16) + ((arr[pos - 2] & 255) << 8);
        if (var1 > 8388607) {
            var1 -= 16777216;
        }

        return var1;
    }

    public int g4s() {
        pos += 4;
        return (arr[pos - 1] & 255) + ((arr[pos - 2] & 255) << 8) + ((arr[pos - 4] & 255) << 24) + ((arr[pos - 3] & 255) << 16);
    }

    public long g8s() {
        var var1 = (long) g4s() & 4294967295L;
        var var3 = (long) g4s() & 4294967295L;
        return (var1 << 32) + var3;
    }

    public float gFloat() {
        return Float.intBitsToFloat(g4s());
    }

    public void pFloat(float x) {
        p4(Float.floatToIntBits(x));
    }

    public boolean gBoolean() {
        return (g1() & 1) == 1;
    }

    public String gjstrnull() {
        if (arr[pos] == 0) {
            ++pos;
            return null;
        } else {
            return gjstr();
        }
    }

    public String gjstr() {
        var var1 = pos;

        while (arr[++pos - 1] != 0) {
        }

        var var2 = pos - var1 - 1;
        return var2 == 0 ? "" : CP1252.decode(arr, var1, var2);
    }

    public String gjstr2() {
        var var1 = arr[++pos - 1];
        if (var1 != 0) {
            throw new IllegalStateException("");
        } else {
            var var2 = pos;

            while (arr[++pos - 1] != 0) {
            }

            var var3 = pos - var2 - 1;
            return var3 == 0 ? "" : CP1252.decode(arr, var2, var3);
        }
    }

    public String gUTF8() {
        var var1 = arr[++pos - 1];
        if (var1 != 0) {
            throw new IllegalStateException("");
        } else {
            var var2 = gvarint();
            if (pos + var2 > arr.length) {
                throw new IllegalStateException("");
            } else {
                var var3 = arr;
                var var4 = pos;
                var var5 = new char[var2];
                var var6 = 0;
                var var7 = var4;

                int var10;
                for (var var8 = var2 + var4; var7 < var8; var5[var6++] = (char) var10) {
                    var var9 = var3[var7++] & 255;
                    if (var9 < 128) {
                        if (var9 == 0) {
                            var10 = 65533;
                        } else {
                            var10 = var9;
                        }
                    } else if (var9 < 192) {
                        var10 = 65533;
                    } else if (var9 < 224) {
                        if (var7 < var8 && (var3[var7] & 192) == 128) {
                            var10 = (var9 & 31) << 6 | var3[var7++] & 63;
                            if (var10 < 128) {
                                var10 = 65533;
                            }
                        } else {
                            var10 = 65533;
                        }
                    } else if (var9 < 240) {
                        if (var7 + 1 < var8 && (var3[var7] & 192) == 128 && (var3[var7 + 1] & 192) == 128) {
                            var10 = (var9 & 15) << 12 | (var3[var7++] & 63) << 6 | var3[var7++] & 63;
                            if (var10 < 2048) {
                                var10 = 65533;
                            }
                        } else {
                            var10 = 65533;
                        }
                    } else if (var9 < 248) {
                        if (var7 + 2 < var8 && (var3[var7] & 192) == 128 && (var3[var7 + 1] & 192) == 128 && (var3[var7 + 2] & 192) == 128) {
                            var var11 = (var9 & 7) << 18 | (var3[var7++] & 63) << 12 | (var3[var7++] & 63) << 6 | var3[var7++] & 63;
                            if (var11 >= 65536 && var11 <= 1114111) {
                                var10 = 65533;
                            } else {
                                var10 = 65533;
                            }
                        } else {
                            var10 = 65533;
                        }
                    } else {
                        var10 = 65533;
                    }
                }

                var var12 = new String(var5, 0, var6);
                pos += var2;
                return var12;
            }
        }
    }

    public void gdata(byte[] var1, int var2, int var3) {
        for (var var4 = var2; var4 < var2 + var3; ++var4) {
            var1[var4] = arr[++pos - 1];
        }

    }

    public byte[] gdata(int length) {
        var var1 = new byte[length];

        for (var var4 = 0; var4 < length; ++var4) {
            var1[var4] = arr[++pos - 1];
        }

        return var1;
    }

    public int gSmart1or2s() {
        var var1 = arr[pos] & 255;
        return var1 < 128 ? g1() - 64 : g2() - '쀀';
    }

    public int gSmart1or2() {
        var var1 = arr[pos] & 255;
        return var1 < 128 ? g1() : g2() - '耀';
    }

    public int gSmart1or2null() {
        var var1 = arr[pos] & 255;
        return var1 < 128 ? g1() - 1 : g2() - '老';
    }

    public int gExtended1or2() {
        var var1 = 0;

        int var2;
        for (var2 = gSmart1or2(); var2 == 32767; var2 = gSmart1or2()) {
            var1 += 32767;
        }

        return var1 + var2;
    }

    public int gSmart2or4s() {
        return arr[pos] < 0 ? g4s() & Integer.MAX_VALUE : g2();
    }

    public int gSmart2or4null() {
        if (arr[pos] < 0) {
            return g4s() & Integer.MAX_VALUE;
        } else {
            var var1 = g2();
            return var1 == 32767 ? -1 : var1;
        }
    }

    public int gvarint() {
        var var1 = arr[++pos - 1];

        int var2;
        for (var2 = 0; var1 < 0; var1 = arr[++pos - 1]) {
            var2 = (var2 | var1 & 127) << 7;
        }

        return var2 | var1;
    }

    public int gvarint2() {
        var var1 = 0;
        var var2 = 0;

        int var3;
        do {
            var3 = g1();
            var1 |= (var3 & 127) << var2;
            var2 += 7;
        } while (var3 > 127);

        return var1;
    }

    public void tinyKeyEncrypt(int[] var1) {
        var var2 = pos / 8;
        pos = 0;

        for (var var3 = 0; var3 < var2; ++var3) {
            var var4 = g4s();
            var var5 = g4s();
            var var6 = 0;
            var var7 = -1640531527;

            for (var var8 = 32; var8-- > 0; var5 += (var4 << 4 ^ var4 >>> 5) + var4 ^ var1[var6 >>> 11 & 3] + var6) {
                var4 += (var5 << 4 ^ var5 >>> 5) + var5 ^ var1[var6 & 3] + var6;
                var6 += var7;
            }

            pos -= 8;
            p4(var4);
            p4(var5);
        }

    }

    public void tinyKeyDecrypt(int[] var1) {
        var var2 = pos / 8;
        pos = 0;

        for (var var3 = 0; var3 < var2; ++var3) {
            var var4 = g4s();
            var var5 = g4s();
            var var6 = -957401312;
            var var7 = -1640531527;

            for (var var8 = 32; var8-- > 0; var4 -= (var5 << 4 ^ var5 >>> 5) + var5 ^ var1[var6 & 3] + var6) {
                var5 -= (var4 << 4 ^ var4 >>> 5) + var4 ^ var1[var6 >>> 11 & 3] + var6;
                var6 -= var7;
            }

            pos -= 8;
            p4(var4);
            p4(var5);
        }

    }

    public void tinyKeyEncrypt(int[] var1, int var2, int var3) {
        var var4 = pos;
        pos = var2;
        var var5 = (var3 - var2) / 8;

        for (var var6 = 0; var6 < var5; ++var6) {
            var var7 = g4s();
            var var8 = g4s();
            var var9 = 0;
            var var10 = -1640531527;

            for (var var11 = 32; var11-- > 0; var8 += (var7 << 4 ^ var7 >>> 5) + var7 ^ var1[var9 >>> 11 & 3] + var9) {
                var7 += (var8 << 4 ^ var8 >>> 5) + var8 ^ var1[var9 & 3] + var9;
                var9 += var10;
            }

            pos -= 8;
            p4(var7);
            p4(var8);
        }

        pos = var4;
    }

    public void tinyKeyDecrypt(int[] var1, int var2, int var3) {
        var var4 = pos;
        pos = var2;
        var var5 = (var3 - var2) / 8;

        for (var var6 = 0; var6 < var5; ++var6) {
            var var7 = g4s();
            var var8 = g4s();
            var var9 = -957401312;
            var var10 = -1640531527;

            for (var var11 = 32; var11-- > 0; var7 -= (var8 << 4 ^ var8 >>> 5) + var8 ^ var1[var9 & 3] + var9) {
                var8 -= (var7 << 4 ^ var7 >>> 5) + var7 ^ var1[var9 >>> 11 & 3] + var9;
                var9 -= var10;
            }

            pos -= 8;
            p4(var7);
            p4(var8);
        }

        pos = var4;
    }

    public void rsaEncrypt(BigInteger var1, BigInteger var2) {
        var var3 = pos;
        pos = 0;
        var var4 = new byte[var3];
        gdata(var4, 0, var3);
        var var5 = new BigInteger(var4);
        var var6 = var5.modPow(var1, var2);
        var var7 = var6.toByteArray();
        pos = 0;
        p2(var7.length);
        pdata(var7, 0, var7.length);
    }

    public int addCRC(int var1) {
        var var2 = computeCRC(arr, var1, pos);
        p4(var2);
        return var2;
    }

    public boolean checkCRC() {
        pos -= 4;
        var var1 = computeCRC(arr, 0, pos);
        var var2 = g4s();
        return var1 == var2;
    }

    public void p1_alt1(int var1) {
        arr[++pos - 1] = (byte) (var1 + 128);
    }

    public void p1_alt2(int var1) {
        arr[++pos - 1] = (byte) -var1;
    }

    public void p1_alt3(int var1) {
        arr[++pos - 1] = (byte) (128 - var1);
    }

    public int g1_alt1() {
        return arr[++pos - 1] - 128 & 255;
    }

    public int g1_alt2() {
        return -arr[++pos - 1] & 255;
    }

    public int g1_alt3() {
        return 128 - arr[++pos - 1] & 255;
    }

    public byte g1s_alt1() {
        return (byte) (arr[++pos - 1] - 128);
    }

    public byte g1s_alt2() {
        return (byte) -arr[++pos - 1];
    }

    public byte g1s_alt3() {
        return (byte) (128 - arr[++pos - 1]);
    }

    public void p2_alt1(int var1) {
        arr[++pos - 1] = (byte) var1;
        arr[++pos - 1] = (byte) (var1 >> 8);
    }

    public void p2_alt2(int var1) {
        arr[++pos - 1] = (byte) (var1 >> 8);
        arr[++pos - 1] = (byte) (var1 + 128);
    }

    public void p2_alt3(int var1) {
        arr[++pos - 1] = (byte) (var1 + 128);
        arr[++pos - 1] = (byte) (var1 >> 8);
    }

    public int g2_alt1() {
        pos += 2;
        return ((arr[pos - 1] & 255) << 8) + (arr[pos - 2] & 255);
    }

    public int g2_alt2() {
        pos += 2;
        return ((arr[pos - 2] & 255) << 8) + (arr[pos - 1] - 128 & 255);
    }

    public int g2_alt3() {
        pos += 2;
        return ((arr[pos - 1] & 255) << 8) + (arr[pos - 2] - 128 & 255);
    }

    public int g2s_alt2() {
        pos += 2;
        var var1 = ((arr[pos - 2] & 255) << 8) + (arr[pos - 1] - 128 & 255);
        if (var1 > 32767) {
            var1 -= 65536;
        }

        return var1;
    }

    public int g2s_alt3() {
        pos += 2;
        var var1 = ((arr[pos - 1] & 255) << 8) + (arr[pos - 2] - 128 & 255);
        if (var1 > 32767) {
            var1 -= 65536;
        }

        return var1;
    }

    public void p3_altTODO(int var1) {
        arr[++pos - 1] = (byte) (var1 >> 8);
        arr[++pos - 1] = (byte) (var1 >> 16);
        arr[++pos - 1] = (byte) var1;
    }

    public int g3_alt1() {
        pos += 3;
        var b0 = arr[pos - 3] & 255;
        var b1 = arr[pos - 2] & 255;
        var b2 = arr[pos - 1] & 255;
        return (b2 << 16) + (b1 << 8) + b0;
    }

    public int g3_altTODO2() {
        pos += 3;
        var b0 = arr[pos - 3] & 255;
        var b1 = arr[pos - 2] & 255;
        var b2 = arr[pos - 1] & 255;
        return (b1 << 16) + (b0 << 8) + b2;
    }

    public int g3s_altTODO() {
        pos += 3;

        var var1 = (arr[pos - 2] & 255) + ((arr[pos - 3] & 255) << 16) + ((arr[pos - 1] & 255) << 8);
        if (var1 > 8388607) {
            var1 -= 16777216;
        }

        return var1;
    }

    public void p4_alt1(int var1) {
        arr[++pos - 1] = (byte) var1;
        arr[++pos - 1] = (byte) (var1 >> 8);
        arr[++pos - 1] = (byte) (var1 >> 16);
        arr[++pos - 1] = (byte) (var1 >> 24);
    }

    public void p4_alt2(int var1) {
        arr[++pos - 1] = (byte) (var1 >> 8);
        arr[++pos - 1] = (byte) var1;
        arr[++pos - 1] = (byte) (var1 >> 24);
        arr[++pos - 1] = (byte) (var1 >> 16);
    }

    public void p4_alt3(int var1) {
        arr[++pos - 1] = (byte) (var1 >> 16);
        arr[++pos - 1] = (byte) (var1 >> 24);
        arr[++pos - 1] = (byte) var1;
        arr[++pos - 1] = (byte) (var1 >> 8);
    }

    public int g4s_alt1() {
        pos += 4;
        return ((arr[pos - 1] & 255) << 24) + ((arr[pos - 2] & 255) << 16) + ((arr[pos - 3] & 255) << 8) + (arr[pos - 4] & 255);
    }

    public int g4s_alt2() {
        pos += 4;
        return (arr[pos - 3] & 255) + ((arr[pos - 4] & 255) << 8) + ((arr[pos - 1] & 255) << 16) + ((arr[pos - 2] & 255) << 24);
    }

    public int g4s_alt3() {
        pos += 4;
        return (arr[pos - 2] & 255) + ((arr[pos - 1] & 255) << 8) + ((arr[pos - 4] & 255) << 16) + ((arr[pos - 3] & 255) << 24);
    }

    public void gdata_altTODO(byte[] var1, int var2, int var3) {
        for (var var4 = var2 + var3 - 1; var4 >= var2; --var4) {
            var1[var4] = (byte) (arr[++pos - 1] - 128);
        }

    }

    public void clear() {
        pos = 0;
    }

    public void enterBitMode() {
        this.bitpos = this.pos * 8;
    }

    public int gBit(int var1) {
        var var2 = this.bitpos >> 3;
        var var3 = 8 - (this.bitpos & 7);
        var var4 = 0;

        for (this.bitpos += var1; var1 > var3; var3 = 8) {
            var4 += (this.arr[var2++] & field5134[var3]) << var1 - var3;
            var1 -= var3;
        }

        int var5;
        if (var1 == var3) {
            var5 = (this.arr[var2] & field5134[var3]) + var4;
        } else {
            var5 = (this.arr[var2] >> var3 - var1 & field5134[var1]) + var4;
        }

        return var5;
    }

    public void leaveBitMode() {
        this.pos = (this.bitpos + 7) / 8;
    }

    public int remainingBits(int length) {
        return length * 8 - this.bitpos;
    }
}
