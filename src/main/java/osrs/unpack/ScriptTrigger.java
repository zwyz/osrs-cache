package osrs.unpack;

public enum ScriptTrigger {
    // map elements
    OPWORLDMAPELEMENT1(10, Type.MAPELEMENT),
    OPWORLDMAPELEMENT2(11, Type.MAPELEMENT),
    OPWORLDMAPELEMENT3(12, Type.MAPELEMENT),
    OPWORLDMAPELEMENT4(13, Type.MAPELEMENT),
    OPWORLDMAPELEMENT5(14, Type.MAPELEMENT),
    WORLDMAPELEMENTMOUSEOVER(15, Type.MAPELEMENT),
    WORLDMAPELEMENTMOUSELEAVE(16, Type.MAPELEMENT),
    WORLDMAPELEMENTMOUSEREPEAT(17, Type.MAPELEMENT),

    // shift ops
    SHIFTOPNPC(30),
    SHIFTOPLOC(31),
    SHIFTOPOBJ(32),
    SHIFTOPPLAYER(33),
    SHIFTOPTILE(34),

    // load/unload
    LOADNPC(35, Type.NPC), // active: npc https://twitter.com/JagexAsh/status/1425523182006583297
    UNLOADNPC(36, Type.NPC), // active: npc
    LOADLOC(37, Type.LOC), // active: loc
    UNLOADLOC(38, Type.LOC), // active: loc
    LOADOBJ(39, Type.OBJ), // active: obj
    UNLOADOBJ(40, Type.OBJ), // active: obj
    LOADPLAYER(41), // active: player
    UNLOADPLAYER(42), // active: player

    // entities
    OBJSTACK(45), // active: tile
    OBJ_COUNT(46, Type.OBJ), // active: obj
    PLAYER_DESTINATION(47), // active: player, tile
    PLAYER_HOVER(48), // active: player, tile
    PLAYER_MOVE(49), // active: player, tile
    NPC_MOVE(50, Type.NPC), // active: npc, tile

    // core
    PROC(73),
    CLIENTSCRIPT(76),

    // minimenu
    TRIGGER_77(77, Type.OBJ),
    OPLOC(78),
    OPOBJ(79),
    OPNPC(80),
    OPPLAYER(81),
    TRIGGER_82(82),
    TRIGGER_83(83),
    ;

    public final int id;
    public final Type type;

    ScriptTrigger(int id, Type type) {
        this.id = id;
        this.type = type;
    }

    ScriptTrigger(int id) {
        this(id, null);
    }

    public static ScriptTrigger byID(int id) {
        for (var value : values()) {
            if (value.id == id) {
                return value;
            }
        }

        throw new IllegalArgumentException("unknown id " + id);
    }
}
