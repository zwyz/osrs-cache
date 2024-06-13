package osrs.unpack.map;

import osrs.util.Packet;

public class EnvLightingSettings {
    public final int colour;
    public final int sunX;
    public final int sunY;
    public final int sunZ;
    public final int ambientIntensity;
    public final int sunlightIntensity;
    public final int unknown7;
    public final float unknown8;
    public final float unknown9;
    public final float shadowStrength;

    public EnvLightingSettings(Packet packet) {
        this.colour = packet.g4s();
        this.sunX = packet.g2();
        this.sunY = packet.g2();
        this.sunZ = packet.g2();
        this.ambientIntensity = packet.g2();
        this.sunlightIntensity = packet.g2();
        this.unknown7 = packet.g2();
        this.unknown8 = packet.gFloat();
        this.unknown9 = packet.gFloat();
        this.shadowStrength = packet.gFloat();
    }
}
