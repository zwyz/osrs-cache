package osrs.unpack.map;

import osrs.util.Packet;

public class EnvironmentD {
    public final float unknown1;
    public final float unknown2;
    public final float unknown3;
    public final float unknown4;
    public final float unknown5;
    public final float unknown6;
    public final float unknown7;
    public final float unknown8;
    public final int unknown9;
    public final int unknown10;

    public EnvironmentD(Packet packet) {
        this.unknown1 = packet.gFloat();
        this.unknown2 = packet.gFloat();
        this.unknown3 = packet.gFloat();
        this.unknown4 = packet.gFloat();
        this.unknown5 = packet.gFloat();
        this.unknown6 = packet.gFloat();
        this.unknown7 = packet.gFloat();
        this.unknown8 = packet.gFloat();
        this.unknown9 = packet.g4s();
        this.unknown10 = packet.g4s();
    }
}
