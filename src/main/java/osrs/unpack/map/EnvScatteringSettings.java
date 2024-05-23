package osrs.unpack.map;

import osrs.unpack.Vector3;
import osrs.util.Packet;

public class EnvScatteringSettings {
    public final boolean enabled;
    public final float parametersX;
    public final float parametersY;
    public final float parametersZ;
    public final float parametersW;
    public final Vector3 tint;
    public final Vector3 outscatteringAmount;
    public final Vector3 inscatteringAmount;

    public EnvScatteringSettings(Packet packet) {
        this.enabled = packet.g1() == 1;
        this.parametersX = packet.gFloat(); // todo: vector4
        this.parametersY = packet.gFloat();
        this.parametersZ = packet.gFloat();
        this.parametersW = packet.gFloat();
        this.tint = new Vector3(packet);
        this.outscatteringAmount = new Vector3(packet);
        this.inscatteringAmount = new Vector3(packet);
    }
}
