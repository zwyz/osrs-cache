package osrs.unpack.map;

import osrs.unpack.Vector3;
import osrs.util.Packet;

public class Environment {
    public final EnvLightingSettings lighting;
    public final EnvFogSettings fog;
    public final EnvScatteringSettings scattering;
    public final EnvVolumetricsSettings volumetrics;
    public final EnvColourGradingSettings colourGrading;
    public final EnvUnknownSettings unknown6;
    public final EnvSkyboxSettings skybox;
    public final EnvColourRemapSettings colourRemap;
    public final float unknown11;
    public final Vector3 unknown12;

    public Environment(Packet packet) {
        this.lighting = new EnvLightingSettings(packet);
        this.fog = new EnvFogSettings(packet);
        this.scattering = new EnvScatteringSettings(packet);
        this.volumetrics = new EnvVolumetricsSettings(packet);
        this.colourGrading = new EnvColourGradingSettings(packet);
        this.unknown6 = new EnvUnknownSettings(packet);
        this.skybox = new EnvSkyboxSettings(packet);
        this.colourRemap = new EnvColourRemapSettings(packet);
        this.unknown11 = packet.gFloat();
        this.unknown12 = new Vector3(packet);
    }
}
