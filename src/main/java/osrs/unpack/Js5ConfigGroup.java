package osrs.unpack;

public enum Js5ConfigGroup {
    FLUTYPE(1),
    HUNTTYPE(2),
    IDKTYPE(3),
    FLOTYPE(4),
    INVTYPE(5),
    LOCTYPE(6),
    MESANIMTYPE(7),
    ENUMTYPE(8),
    NPCTYPE(9),
    OBJTYPE(10),
    PARAMTYPE(11),
    SEQTYPE(12),
    SPOTTYPE(13),
    VARBIT(14),
    VARCLIENTSTR(15),
    VARPLAYER(16),
    CATEGORY(17),
    AREATYPE(18),
    VARCLIENT(19),
    VAROBJ(20),
    VARSHARED(22),
    VARSHAREDSTR(23),
    VARNPC(24),
    VARNPCBIT(25),
    ITEMCODETYPE(26),
    VARGLOBAL(27),
    CONTROLLERTYPE(28),
    VARCONTROLLER(29),
    VARCONTROLLERBIT(30),
    UNKNOWN_31(31),
    HITMARKTYPE(32),
    HEADBARTYPE(33),
    STRUCTTYPE(34),
    MELTYPE(35),
    STRINGVECTORTYPE(37),
    DBROWTYPE(38),
    DBTABLETYPE(39),
    VAR_CLAN(47),
    VAR_CLAN_SETTING(54),
    GAMELOGEVENT(70),
    ;

    public final int id;

    Js5ConfigGroup(int id) {
        this.id = id;
    }
}
