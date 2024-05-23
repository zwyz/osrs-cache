package osrs.unpack.map;

import osrs.util.Packet;

public class EnvColourGradingSettings {
    public final boolean toneMapEnabled;
    public final int toneMapOperator;
    public final float minBlackLum;
    public final float maxWhiteLum;
    public final float exposureKey;
    public final float maxAutoExposure;
    public final float unknown7;

    public EnvColourGradingSettings(Packet packet) {
        this.toneMapEnabled = packet.g1() == 1;
        this.toneMapOperator = packet.g1();
        this.minBlackLum = packet.gFloat();
        this.maxWhiteLum = packet.gFloat();
        this.exposureKey = packet.gFloat();
        this.maxAutoExposure = packet.gFloat();
        this.unknown7 = packet.gFloat();
    }
}
