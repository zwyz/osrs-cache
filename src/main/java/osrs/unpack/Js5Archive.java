package osrs.unpack;

public enum Js5Archive {
    JS5_ANIMS(0),
    JS5_BASES(1),
    JS5_CONFIG(2),
    JS5_INTERFACES(3),
    JS5_SYNTH(4),
    JS5_MAPS(5),
    JS5_SONGS(6),
    JS5_MODELS(7),
    JS5_SPRITES(8),
    JS5_TEXTURES(9),
    JS5_BINARY(10),
    JS5_JINGLES(11),
    JS5_CLIENTSCRIPTS(12),
    JS5_FONTMETRICS(13),
    JS5_VORBIS(14),
    JS5_MIDIPATCHES(15),
    JS5_DEFAULTS(17),
    JS5_WORLDMAPAREADATA(18),
    JS5_WORLDMAPDATA(19),
    JS5_WORLDMAPGROUND(20),
    JS5_DBTABLEINDEX(21),
    JS5_KEYFRAMESETS(22),
    JS5_INTERFACEDEBUGNAMES(23),
    JS5_DEBUGNAMES(24),
    JS5_MODELSRT7(25);

    public final int id;

    Js5Archive(int id) {
        this.id = id;
    }
}
