package osrs.unpack.map;

import osrs.unpack.Vector3;
import osrs.util.Packet;

public class EnvScatteringSettings {
    public final boolean enabled;
    public final float density;
    public final float startDistance;
    public final float unknown4;
    public final float unknown5;
    public final Vector3 scatteringTint;
    public final Vector3 outscatteringAmount;
    public final Vector3 inscatteringAmount;

    public EnvScatteringSettings(Packet packet) {
        this.enabled = packet.g1() == 1;
        this.density = packet.gFloat();
        this.startDistance = packet.gFloat();
        this.unknown4 = packet.gFloat();
        this.unknown5 = packet.gFloat();
        this.scatteringTint = new Vector3(packet);
        this.outscatteringAmount = new Vector3(packet);
        this.inscatteringAmount = new Vector3(packet);
    }
}
