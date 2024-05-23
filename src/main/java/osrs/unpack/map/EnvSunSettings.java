package osrs.unpack.map;

import osrs.util.Packet;

public class EnvSunSettings {
    public final int colour;
    public final int angleX;
    public final int angleY;
    public final int angleZ;
    public final int ambient;
    public final int sunlight;
    public final int backlight;
    public final float unknown8;
    public final float unknown9;
    public final float unknown10;

    public EnvSunSettings(Packet packet) {
        this.colour = packet.g4s();
        this.angleX = packet.g2();
        this.angleY = packet.g2();
        this.angleZ = packet.g2();
        this.ambient = packet.g2();
        this.sunlight = packet.g2();
        this.backlight = packet.g2();
        this.unknown8 = packet.gFloat();
        this.unknown9 = packet.gFloat();
        this.unknown10 = packet.gFloat();
    }
}
