package osrs.unpack;

import osrs.util.Packet;

public class Vector3 {
    public final float x;
    public final float y;
    public final float z;

    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Packet packet) {
        this.x = packet.gFloat();
        this.y = packet.gFloat();
        this.z = packet.gFloat();
    }
}
