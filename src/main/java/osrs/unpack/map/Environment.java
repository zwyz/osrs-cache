package osrs.unpack.map;

import osrs.unpack.Vector3;
import osrs.util.Packet;

public class Environment {
    public final EnvSunSettings sun;
    public final EnvFogSettings fog;
    public final EnvScatteringSettings scattering;
    public final EnvironmentD unknown4;
    public final EnvColourGradingSettings colourGrading;
    public final EnvironmentF unknown6;
    public final int unknown7;
    public final int unknown8;
    public final boolean unknown9;
    public final EnvironmentG unknown10;
    public final float unknown11;
    public final Vector3 unknown12;

    public Environment(Packet packet) {
        this.sun = new EnvSunSettings(packet);
        this.fog = new EnvFogSettings(packet);
        this.scattering = new EnvScatteringSettings(packet);
        this.unknown4 = new EnvironmentD(packet);
        this.colourGrading = new EnvColourGradingSettings(packet);
        this.unknown6 = new EnvironmentF(packet);
        this.unknown7 = packet.g2();
        this.unknown8 = packet.g2();
        this.unknown9 = packet.g1() == 1;
        this.unknown10 = new EnvironmentG(packet);
        this.unknown11 = packet.gFloat();
        this.unknown12 = new Vector3(packet);
    }
}
