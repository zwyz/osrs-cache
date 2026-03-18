package osrs.unpack.map;

import osrs.util.Packet;

public class MapSquare {
    public MapTiles tiles;
    public MapLocations locations;
    public Environment environment;

    public MapSquare(byte[] m, byte[] l, byte[] e, byte[] t, byte[] w) {
        if (m != null) {
            this.tiles = new MapTiles(new Packet(m));
        }

        if (l != null) {
            this.locations = new MapLocations(new Packet(l));
        }

        if (e != null) {
            var packet = new Packet(e);

            if (packet.g1() != 0) {
                var environment = new Environment(packet);
                packet.g4s(); // todo: osrs-only
                this.environment = environment;
            }
        }

        if (t != null) {
            // todo
        }

        if (w != null) {
            // todo
        }
    }
}
