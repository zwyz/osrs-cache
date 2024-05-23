package osrs.unpack.map;

import osrs.util.Packet;

public class EnvironmentG {
    public final int unknown1;
    public final float unknown2;
    public final int unknown3;
    public final float unknown4;

    public EnvironmentG(Packet packet) {
        this.unknown1 = packet.g2();
        this.unknown2 = packet.gFloat();
        this.unknown3 = packet.g2();
        this.unknown4 = packet.gFloat();
    }
}
