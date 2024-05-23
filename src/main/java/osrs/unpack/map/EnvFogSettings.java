package osrs.unpack.map;

import osrs.util.Packet;

public class EnvFogSettings {
    public final int colour;
    public final int depth;
    public final boolean enabled;
    public final float maxFogFarPlane;
    public final float depthAngleFalloff;
    public final float depthAngleOffset;
    public final float angleOffset;

    public EnvFogSettings(Packet packet) {
        this.colour = packet.g4s();
        this.depth = packet.g2();
        this.enabled = packet.g1() == 1;
        this.maxFogFarPlane = packet.gFloat();
        this.depthAngleFalloff = packet.gFloat();
        this.depthAngleOffset = packet.gFloat();
        this.angleOffset = packet.gFloat();
    }
}
