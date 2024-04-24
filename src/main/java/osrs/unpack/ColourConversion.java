package osrs.unpack;

import java.util.Arrays;

public class ColourConversion {
    public static int[] RGB_TO_HSL = new int[0x8000];
    public static int[] REVERSE_RGB_TO_HSL = new int[0x10000];

    static {
        Arrays.fill(REVERSE_RGB_TO_HSL, -1);

        for (var rgb = 0; rgb < 32768; ++rgb) {
            var hsl = convertRGBToHSL(rgb);
            RGB_TO_HSL[rgb] = hsl;
            REVERSE_RGB_TO_HSL[hsl] = rgb;
        }
    }

    private static int convertRGBToHSL(int rgb) {
        var red = (double) (rgb >> 10 & 31) / 31.0D;
        var green = (double) (rgb >> 5 & 31) / 31.0D;
        var blue = (double) (rgb & 31) / 31.0D;
        var min = red;

        if (green < min) {
            min = green;
        }

        if (blue < min) {
            min = blue;
        }

        var max = red;

        if (green > max) {
            max = green;
        }

        if (blue > max) {
            max = blue;
        }

        var hueNormalised = 0.0;
        var saturationNormalised = 0.0;
        var lightnessNormalised = (max + min) / 2.0;

        if (max != min) {
            if (lightnessNormalised < 0.5) {
                saturationNormalised = (max - min) / (min + max);
            }

            if (lightnessNormalised >= 0.5) {
                saturationNormalised = (max - min) / (2.0 - max - min);
            }

            if (red == max) {
                hueNormalised = (green - blue) / (max - min);
            } else if (green == max) {
                hueNormalised = (blue - red) / (max - min) + 2.0;
            } else if (max == blue) {
                hueNormalised = (red - green) / (max - min) + 4.0;
            }
        }

        hueNormalised /= 6.0;
        var hue = (int) (hueNormalised * 256.0);
        var saturation = (int) (saturationNormalised * 256.0);
        var lightness = (int) (lightnessNormalised * 256.0);

        if (saturation < 0) {
            saturation = 0;
        } else if (saturation > 255) {
            saturation = 255;
        }

        if (lightness < 0) {
            lightness = 0;
        } else if (lightness > 255) {
            lightness = 255;
        }

        if (lightness > 243) {
            saturation >>= 4;
        } else if (lightness > 217) {
            saturation >>= 3;
        } else if (lightness > 192) {
            saturation >>= 2;
        } else if (lightness > 179) {
            saturation >>= 1;
        }

        return (lightness >> 1) + (saturation >> 5 << 7) + ((hue & 0xff) >> 2 << 10);
    }

    public static int reverseRGBFromHSL(int value) {
        var reverse = REVERSE_RGB_TO_HSL[value];

        if (reverse == -1) {
            throw new IllegalStateException("hsl " + value + " unobtainable from rgb");
        }

        return reverse;
    }
}
