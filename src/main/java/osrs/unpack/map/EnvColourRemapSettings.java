package osrs.unpack.map;

import osrs.util.Packet;

public class EnvColourRemapSettings {
    public final int tex1;
    public final float weighting1;
    public final int tex2;
    public final float weighting2;

    public EnvColourRemapSettings(Packet packet) {
        this.tex1 = packet.g2();
        this.weighting1 = packet.gFloat();
        this.tex2 = packet.g2();
        this.weighting2 = packet.gFloat();
    }
}
