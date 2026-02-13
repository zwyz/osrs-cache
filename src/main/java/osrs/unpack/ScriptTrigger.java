package osrs.unpack;

public enum ScriptTrigger { // all are official names from 233.2
    // map elements
    OPWORLDMAPELEMENT1(10, Type.MAPELEMENT),
    OPWORLDMAPELEMENT2(11, Type.MAPELEMENT),
    OPWORLDMAPELEMENT3(12, Type.MAPELEMENT),
    OPWORLDMAPELEMENT4(13, Type.MAPELEMENT),
    OPWORLDMAPELEMENT5(14, Type.MAPELEMENT),
    WORLDMAPELEMENTMOUSEOVER(15, Type.MAPELEMENT),
    WORLDMAPELEMENTMOUSELEAVE(16, Type.MAPELEMENT),
    WORLDMAPELEMENTMOUSEREPEAT(17, Type.MAPELEMENT),
    INV_CHANGED(18, Type.INV),

    // client ops
    GCLIENTCLICKNPC(30),
    GCLIENTCLICKLOC(31),
    GCLIENTCLICKOBJ(32),
    GCLIENTCLICKPLAYER(33),
    GCLIENTCLICKTILE(34),

    // load/unload
    LOADNPC(35, Type.NPC), // active: npc
    UNLOADNPC(36, Type.NPC), // active: npc
    LOADLOC(37, Type.LOC), // active: loc
    LOCCHANGE(38, Type.LOC), // active: loc
    LOADOBJ(39, Type.OBJ), // active: obj
    UNLOADOBJ(40, Type.OBJ), // active: obj
    LOADPLAYER(41), // active: player
    UNLOADPLAYER(42), // active: player

    // entities
    UPDATEOBJSTACK(45), // active: tile
    UPDATEOBJCOUNT(46, Type.OBJ), // active: obj
    COORDDESTINATION(47), // active: player, tile
    COORDMOUSEOVER(48), // active: player, tile
    PLAYERROUTEUPDATE(49), // active: player, tile
    NPCROUTEUPDATE(50, Type.NPC), // active: npc, tile

    // core
    PROC(73),
    CLIENTSCRIPT(76),

    // minimenu
    HELDOBJOVERLAY(77, Type.OBJ),
    ONCLICKLOC(78, Type.LOC),
    ONCLICKOBJ(79, Type.OBJ),
    ONCLICKNPC(80, Type.NPC),
    ONCLICKPLAYER(81),
    ONMINIMENUOPEN(82),
    ONMINIMENUCLOSE(83);

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
