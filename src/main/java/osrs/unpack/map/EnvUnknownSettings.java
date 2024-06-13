package osrs.unpack.map;

import osrs.util.Packet;

public class EnvUnknownSettings {
    public final float unknown1;
    public final float unknown2;
    public final float unknown3;
    public final float unknown4;
    public final float unknown5;
    public final float unknown6;

    public EnvUnknownSettings(Packet packet) {
        this.unknown1 = packet.gFloat();
        this.unknown2 = packet.gFloat();
        this.unknown3 = packet.gFloat();
        this.unknown4 = packet.gFloat();
        this.unknown5 = packet.gFloat();
        this.unknown6 = packet.gFloat();
    }
}
