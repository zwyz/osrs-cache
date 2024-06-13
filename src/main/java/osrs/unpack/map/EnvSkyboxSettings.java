package osrs.unpack.map;

import osrs.util.Packet;

public class EnvSkyboxSettings {
    public final int unknown1;
    public final int unknown2;
    public final boolean unknown3;

    public EnvSkyboxSettings(Packet packet) {
        this.unknown1 = packet.g2();
        this.unknown2 = packet.g2();
        this.unknown3 = packet.g1() == 1;
    }
}
