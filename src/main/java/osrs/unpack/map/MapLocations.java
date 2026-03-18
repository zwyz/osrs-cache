package osrs.unpack.map;

import osrs.util.Packet;

import java.util.ArrayList;

public class MapLocations {
    private final ArrayList<Location> locations;

    public MapLocations(Packet packet) {
        this.locations = new ArrayList<>();
        var type = -1;

        for (var value1 = packet.gExtended1or2(); value1 != 0; value1 = packet.gExtended1or2()) {
            type += value1;
            var coord = 0;

            for (var value2 = packet.gSmart1or2(); value2 != 0; value2 = packet.gSmart1or2()) {
                coord += value2 - 1;
                var z = coord & 63;
                var x = coord >> 6 & 63;
                var level = coord >> 12;
                var value3 = packet.g1();
                var shape = value3 >> 2;
                var angle = value3 & 3;
                this.locations.add(new Location(level, x, z, type, angle, shape));
            }
        }
    }

    private record Location(int level, int x, int z, int type, int angle, int shape) {}
}
