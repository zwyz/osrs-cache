package osrs.unpack;

public enum Js5WorldMapGroup {
    DETAILS(0),
    COMPOSITEMAP(1),
    COMPOSITETEXTURE(2),
    AREA(3),
    LABELS(4);

    public final int id;

    Js5WorldMapGroup(int id) {
        this.id = id;
    }
}
