package osrs.unpack;

public enum Js5Archive {
    JS5_ANIMS(0),
    JS5_BASES(1),
    JS5_CONFIG(2), // ok
    JS5_INTERFACES(3), // ok
    JS5_SYNTH(4), // todo
    JS5_MAPS(5),
    JS5_SONGS(6), // midi
    JS5_MODELS(7),
    JS5_SPRITES(8),
    JS5_TEXTURES(9), // ok
    JS5_BINARY(10),
    JS5_JINGLES(11), // midi
    JS5_CLIENTSCRIPTS(12),
    JS5_FONTMETRICS(13),
    JS5_VORBIS(14),
    JS5_MIDIPATCHES(15), // midi patch
    JS5_DEFAULTS(17),
    JS5_WORLDMAPAREADATA(18), // ok
    JS5_WORLDMAPDATA(19), // ok
    JS5_WORLDMAPGROUND(20), // ok
    JS5_DBTABLEINDEX(21),
    JS5_UNKNOWN22(22),
    JS5_UNKNOWN23(23),
    ;

    public final int id;

    Js5Archive(int id) {
        this.id = id;
    }
}
