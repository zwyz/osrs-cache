package osrs.unpack;

public enum Js5DebugNamesGroup {
    OBJTYPES(0),
    NPCTYPES(1),
    INVTYPES(2),
    VARPTYPES(3),
    VARBITTYPES(4),
    LOCTYPES(6),
    SEQTYPES(7),
    SPOTTYPES(8),
    ROWTYPES(9),
    TABLETYPES(10),
    SOUNDTYPES(11),
    SPRITETYPES(12),
    IFTYPES(13);

    public final int id;

    Js5DebugNamesGroup(int id) {
        this.id = id;
    }
}
