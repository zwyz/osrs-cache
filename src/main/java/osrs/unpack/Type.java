package osrs.unpack;

import osrs.Unpack;
import osrs.util.Lattice;

import java.util.*;

// todo: clean this up
public class Type {
    private static final List<Type> TYPES = new ArrayList<>();
    private static final Map<String, Type> BY_NAME = new HashMap<>();

    public static final Type INT = new Type("int", BaseVarType.INTEGER);
    public static final Type BOOLEAN = new Type("boolean", BaseVarType.INTEGER);
    public static final Type HASH32 = new Type("hash32", BaseVarType.INTEGER);
    public static final Type QUEST = new Type("quest", BaseVarType.INTEGER);
    public static final Type QUESTHELP = new Type("questhelp", BaseVarType.INTEGER);
    public static final Type CURSOR = new Type("cursor", BaseVarType.INTEGER);
    public static final Type SEQ = new Type("seq", BaseVarType.INTEGER);
    public static final Type COLOUR = new Type("colour", BaseVarType.INTEGER);
    public static final Type LOC_SHAPE = new Type("locshape", BaseVarType.INTEGER);
    public static final Type COMPONENT = new Type("component", BaseVarType.INTEGER);
    public static final Type IDKIT = new Type("idkit", BaseVarType.INTEGER);
    public static final Type MIDI = new Type("midi", BaseVarType.INTEGER);
    public static final Type NPC_MODE = new Type("npc_mode", BaseVarType.INTEGER);
    public static final Type SYNTH = new Type("synth", BaseVarType.INTEGER);
    public static final Type AI_QUEUE = new Type("ai_queue", BaseVarType.INTEGER);
    public static final Type AREA = new Type("area", BaseVarType.INTEGER);
    public static final Type STAT = new Type("stat", BaseVarType.INTEGER);
    public static final Type NPC_STAT = new Type("npc_stat", BaseVarType.INTEGER);
    public static final Type WRITEINV = new Type("writeinv", BaseVarType.INTEGER);
    public static final Type MESH = new Type("mesh", BaseVarType.INTEGER);
    public static final Type MAPAREA = new Type("wma", BaseVarType.INTEGER);
    public static final Type COORDGRID = new Type("coord", BaseVarType.INTEGER);
    public static final Type GRAPHIC = new Type("graphic", BaseVarType.INTEGER);
    public static final Type CHATPHRASE = new Type("chatphrase", BaseVarType.INTEGER);
    public static final Type FONTMETRICS = new Type("fontmetrics", BaseVarType.INTEGER);
    public static final Type ENUM = new Type("enum", BaseVarType.INTEGER);
    public static final Type HUNT = new Type("hunt", BaseVarType.INTEGER);
    public static final Type JINGLE = new Type("jingle", BaseVarType.INTEGER);
    public static final Type CHATCAT = new Type("chatcat", BaseVarType.INTEGER);
    public static final Type LOC = new Type("loc", BaseVarType.INTEGER);
    public static final Type MODEL = new Type("model", BaseVarType.INTEGER);
    public static final Type NPC = new Type("npc", BaseVarType.INTEGER);
    public static final Type OBJ = new Type("obj", BaseVarType.INTEGER);
    public static final Type NAMEDOBJ = new Type("namedobj", BaseVarType.INTEGER);
    public static final Type PLAYER_UID = new Type("player_uid", BaseVarType.INTEGER);
    public static final Type REGION_UID = new Type("region_uid", BaseVarType.INTEGER);
    public static final Type STRING = new Type("string", BaseVarType.STRING);
    public static final Type SPOTANIM = new Type("spotanim", BaseVarType.INTEGER);
    public static final Type NPC_UID = new Type("npc_uid", BaseVarType.INTEGER);
    public static final Type INV = new Type("inv", BaseVarType.INTEGER);
    public static final Type TEXTURE = new Type("texture", BaseVarType.INTEGER);
    public static final Type CATEGORY = new Type("category", BaseVarType.INTEGER);
    public static final Type CHAR = new Type("char", BaseVarType.INTEGER);
    public static final Type LASER = new Type("laser", BaseVarType.INTEGER);
    public static final Type BAS = new Type("bas", BaseVarType.INTEGER);
    public static final Type CONTROLLER = new Type("controller", BaseVarType.INTEGER);
    public static final Type COLLISION_GEOMETRY = new Type("collision_geometry", BaseVarType.INTEGER);
    public static final Type PHYSICS_MODEL = new Type("physics_model", BaseVarType.INTEGER);
    public static final Type PHYSICS_CONTROL_MODIFIER = new Type("physics_control_modifier", BaseVarType.INTEGER);
    public static final Type CLANHASH = new Type("clanhash", BaseVarType.LONG);
    public static final Type CUTSCENE = new Type("cutscene", BaseVarType.INTEGER);
    public static final Type ITEMCODE = new Type("itemcode", BaseVarType.INTEGER);
    public static final Type PVPKILLS = new Type("pvpkills", BaseVarType.INTEGER);
    public static final Type MAPSCENEICON = new Type("msi", BaseVarType.INTEGER);
    public static final Type CLANFORUMQFC = new Type("clanforumqfc", BaseVarType.LONG);
    public static final Type VORBIS = new Type("vorbis", BaseVarType.INTEGER);
    public static final Type VERIFY_OBJECT = new Type("verifyobj", BaseVarType.INTEGER);
    public static final Type MAPELEMENT = new Type("mapelement", BaseVarType.INTEGER);
    public static final Type CATEGORYTYPE = new Type("categorytype", BaseVarType.INTEGER);
    public static final Type SOCIAL_NETWORK = new Type("socialnetwork", BaseVarType.INTEGER);
    public static final Type HITMARK = new Type("hitmark", BaseVarType.INTEGER);
    public static final Type PACKAGE = new Type("package", BaseVarType.INTEGER);
    public static final Type PARTICLE_EFFECTOR = new Type("pef", BaseVarType.INTEGER);
    public static final Type CONTROLLER_UID = new Type("controller_uid", BaseVarType.INTEGER);
    public static final Type PARTICLE_EMITTER = new Type("pem", BaseVarType.INTEGER);
    public static final Type PLOGTYPE = new Type("plog", BaseVarType.INTEGER);
    public static final Type UNSIGNED_INT = new Type("unsigned_int", BaseVarType.INTEGER);
    public static final Type SKYBOX = new Type("skybox", BaseVarType.INTEGER);
    public static final Type SKYDECOR = new Type("skydecor", BaseVarType.INTEGER);
    public static final Type HASH64 = new Type("hash64", BaseVarType.LONG);
    public static final Type INPUTTYPE = new Type("inputtype", BaseVarType.INTEGER);
    public static final Type STRUCT = new Type("struct", BaseVarType.INTEGER);
    public static final Type DBROW = new Type("dbrow", BaseVarType.INTEGER);
    public static final Type STORABLELABEL = new Type("storablelabel", BaseVarType.INTEGER);
    public static final Type STORABLEPROC = new Type("storableproc", BaseVarType.INTEGER);
    public static final Type GAMELOGEVENT = new Type("gamelogevent", BaseVarType.INTEGER);
    public static final Type ANIMATIONCLIP = new Type("animationclip", BaseVarType.INTEGER);
    public static final Type SKELETON = new Type("skeleton", BaseVarType.INTEGER);
    public static final Type REGIONVISIBILITY = new Type("region_visibility", BaseVarType.INTEGER);
    public static final Type FMODHANDLE = new Type("fmodhandle", BaseVarType.INTEGER);
    public static final Type REGION_ALLOWLOGIN = new Type("region_allowlogin", BaseVarType.INTEGER);
    public static final Type REGION_INFO = new Type("region_info", BaseVarType.INTEGER);
    public static final Type REGION_INFO_FAILURE = new Type("region_info_failure", BaseVarType.INTEGER);
    public static final Type SERVER_ACCOUNT_CREATION_STEP = new Type("server_account_creation_step", BaseVarType.INTEGER);
    public static final Type CLIENT_ACCOUNT_CREATION_STEP = new Type("client_account_creation_step", BaseVarType.INTEGER);
    public static final Type LOBBY_ACCOUNT_CREATION_STEP = new Type("lobby_account_creation_step", BaseVarType.INTEGER);
    public static final Type GWC_PLATFORM = new Type("gwc_platform", BaseVarType.INTEGER);
    public static final Type CURRENCY = new Type("currency", BaseVarType.INTEGER);
    public static final Type KEYBOARD_KEY = new Type("keyboard_key", BaseVarType.INTEGER);
    public static final Type MOUSEEVENT = new Type("mouseevent", BaseVarType.INTEGER);
    public static final Type HEADBAR = new Type("headbar", BaseVarType.INTEGER);
    public static final Type BUG_TEMPLATE = new Type("bugtemplate", BaseVarType.INTEGER);
    public static final Type BILLING_AUTH_FLAG = new Type("billingauthflag", BaseVarType.INTEGER);
    public static final Type ACCOUNT_FEATURE_FLAG = new Type("accountfeatureflag", BaseVarType.INTEGER);
    public static final Type INTERFACE = new Type("interface", BaseVarType.INTEGER);
    public static final Type TOPLEVELINTERFACE = new Type("toplevelinterface", BaseVarType.INTEGER);
    public static final Type OVERLAYINTERFACE = new Type("overlayinterface", BaseVarType.INTEGER);
    public static final Type CLIENTINTERFACE = new Type("clientinterface", BaseVarType.INTEGER);
    public static final Type MOVESPEED = new Type("movespeed", BaseVarType.INTEGER);
    public static final Type MATERIAL = new Type("material", BaseVarType.INTEGER);
    public static final Type SEQGROUP = new Type("seqgroup", BaseVarType.INTEGER);
    public static final Type TEMP_HISCORE = new Type("TEMPHISCORE", BaseVarType.INTEGER);
    public static final Type TEMP_HISCORE_LENGTH_TYPE = new Type("temphiscorelengthtype", BaseVarType.INTEGER);
    public static final Type TEMP_HISCORE_DISPLAY_TYPE = new Type("temphiscoretype", BaseVarType.INTEGER);
    public static final Type TEMP_HISCORE_CONTRIBUTE_RESULT = new Type("temphiscorecontributeresult", BaseVarType.INTEGER);
    public static final Type AUDIOGROUP = new Type("audiogroup", BaseVarType.INTEGER);
    public static final Type AUDIOMIXBUSS = new Type("audiobuss", BaseVarType.INTEGER);
    public static final Type LONG = new Type("long", BaseVarType.LONG);
    public static final Type CRM_CHANNEL = new Type("crm_channel", BaseVarType.INTEGER);
    public static final Type HTTP_IMAGE = new Type("http_image", BaseVarType.INTEGER);
    public static final Type POP_UP_DISPLAY_BEHAVIOUR = new Type("popupdisplaybehaviour", BaseVarType.INTEGER);
    public static final Type POLL = new Type("poll", BaseVarType.INTEGER);
    public static final Type MTXN_PACKAGE = new Type("mtxn_package", BaseVarType.LONG);
    public static final Type MTXN_PRICE_POINT = new Type("mtxn_price_point", BaseVarType.LONG);
    public static final Type ENTITYOVERLAY = new Type("entityoverlay", BaseVarType.INTEGER);
    public static final Type DBTABLE = new Type("dbtable", BaseVarType.INTEGER);

    // Group 2
    public static final Type LABEL = new Type("label", BaseVarType.INTEGER);
    public static final Type QUEUE = new Type("queue", BaseVarType.INTEGER);
    public static final Type TIMER = new Type("timer", BaseVarType.INTEGER);
    public static final Type WEAKQUEUE = new Type("weakqueue", BaseVarType.INTEGER);
    public static final Type SOFTTIMER = new Type("softtimer", BaseVarType.INTEGER);
    public static final Type OBJVAR = new Type("objvar", BaseVarType.INTEGER);
    public static final Type WALKTRIGGER = new Type("walktrigger", BaseVarType.INTEGER);

    // Group 3
    public static final Type UNKNOWN1 = new Type("unknown1", BaseVarType.INTEGER);
    public static final Type UNKNOWN2 = new Type("unknown2", BaseVarType.INTEGER);
    public static final Type UNKNOWN3 = new Type("unknown3", BaseVarType.INTEGER);
    public static final Type UNKNOWN4 = new Type("unknown4", BaseVarType.INTEGER);
    public static final Type UNKNOWN5 = new Type("unknown5", BaseVarType.INTEGER);
    public static final Type UNKNOWN6 = new Type("unknown6", BaseVarType.INTEGER);
    public static final Type UNKNOWN7 = new Type("unknown7", BaseVarType.INTEGER);
    public static final Type VARP = new Type("varp", BaseVarType.INTEGER); // 214
    public static final Type UNKNOWN9 = new Type("unknown9", BaseVarType.INTEGER);
    public static final Type UNKNOWN10 = new Type("unknown10", BaseVarType.INTEGER);
    public static final Type UNKNOWN11 = new Type("unknown11", BaseVarType.INTEGER);
    public static final Type UNKNOWN12 = new Type("unknown12", BaseVarType.INTEGER);
    public static final Type UNKNOWN13 = new Type("unknown13", BaseVarType.INTEGER);
    public static final Type UNKNOWN14 = new Type("unknown14", BaseVarType.INTEGER);
    public static final Type UNKNOWN15 = new Type("unknown15", BaseVarType.INTEGER);
    public static final Type UNKNOWN16 = new Type("unknown16", BaseVarType.INTEGER);
    public static final Type UNKNOWN17 = new Type("unknown17", BaseVarType.INTEGER);
    public static final Type UNKNOWN18 = new Type("unknown18", BaseVarType.INTEGER);
    public static final Type UNKNOWN19 = new Type("unknown19", BaseVarType.INTEGER);
    public static final Type UNKNOWN20 = new Type("unknown20", BaseVarType.INTEGER);
    public static final Type UNKNOWN21 = new Type("unknown21", BaseVarType.INTEGER);
    public static final Type UNKNOWN22 = new Type("unknown22", BaseVarType.INTEGER);
    public static final Type UNKNOWN23 = new Type("unknown23", BaseVarType.INTEGER);
    public static final Type UNKNOWN24 = new Type("unknown24", BaseVarType.INTEGER);
    public static final Type UNKNOWN25 = new Type("unknown25", BaseVarType.INTEGER);
    public static final Type TRANSMIT_LIST = new Type("transmit_list", BaseVarType.INTEGER);
    public static final Type UNKNOWN27 = new Type("unknown27", BaseVarType.INTEGER);
    public static final Type UNKNOWN28 = new Type("unknown28", BaseVarType.INTEGER);
    public static final Type UNKNOWN29 = new Type("unknown29", BaseVarType.INTEGER);
    public static final Type UNKNOWN30 = new Type("unknown30", BaseVarType.INTEGER);
    public static final Type UNKNOWN31 = new Type("unknown31", BaseVarType.INTEGER);
    public static final Type UNKNOWN32 = new Type("unknown32", BaseVarType.INTEGER);
    public static final Type UNKNOWN33 = new Type("unknown33", BaseVarType.INTEGER);
    public static final Type UNKNOWN34 = new Type("unknown34", BaseVarType.INTEGER);
    public static final Type UNKNOWN35 = new Type("unknown35", BaseVarType.INTEGER);
    public static final Type UNKNOWN36 = new Type("unknown36", BaseVarType.INTEGER);
    public static final Type UNKNOWN37 = new Type("unknown37", BaseVarType.INTEGER);
    public static final Type UNKNOWN38 = new Type("unknown38", BaseVarType.INTEGER);
    public static final Type UNKNOWN39 = new Type("unknown39", BaseVarType.INTEGER);
    public static final Type UNKNOWN40 = new Type("unknown40", BaseVarType.INTEGER);
    public static final Type UNKNOWN41 = new Type("unknown41", BaseVarType.INTEGER);
    public static final Type UNKNOWN42 = new Type("unknown42", BaseVarType.INTEGER);
    public static final Type UNKNOWN43 = new Type("unknown43", BaseVarType.INTEGER);
    public static final Type UNKNOWN44 = new Type("unknown44", BaseVarType.INTEGER);
    public static final Type UNKNOWN45 = new Type("unknown45", BaseVarType.INTEGER);
    public static final Type UNKNOWN46 = new Type("unknown46");
    public static final Type UNKNOWN47 = new Type("unknown47");
    public static final Type UNKNOWN48 = new Type("unknown48");
    public static final Type UNKNOWN49 = new Type("unknown49");
    public static final Type UNKNOWN50 = new Type("unknown50");

    // unknown id
    public static final Type STRINGVECTOR = new Type("stringvector", BaseVarType.INTEGER); // added in 202
    public static final Type WORLDENTITY = new Type("worldentity", BaseVarType.INTEGER);
    public static final Type CONFIG71 = new Type("config71", BaseVarType.INTEGER);
    public static final Type MESANIM = new Type("mesanim", BaseVarType.INTEGER);
    public static final Type UNDERLAY = new Type("underlay", BaseVarType.INTEGER);
    public static final Type OVERLAY = new Type("overlay", BaseVarType.INTEGER);
    public static final Type WORLD_AREA = new Type("world_area", BaseVarType.INTEGER);
    public static final Type WATER = new Type("water", BaseVarType.INTEGER);

    // special
    public static final Type TYPE = new Type("type", BaseVarType.INTEGER);
    public static final Type BASEVARTYPE = new Type("basevartype", BaseVarType.INTEGER);
    public static final Type PARAM = new Type("param", BaseVarType.INTEGER);
    public static final Type CLIENTSCRIPT = new Type("clientscript", BaseVarType.INTEGER);
    public static final Type GCLIENTCLICKNPC = new Type("gclientclicknpc", BaseVarType.INTEGER);
    public static final Type GCLIENTCLICKLOC = new Type("gclientclickloc", BaseVarType.INTEGER);
    public static final Type GCLIENTCLICKOBJ = new Type("gclientclickobj", BaseVarType.INTEGER);
    public static final Type GCLIENTCLICKPLAYER = new Type("gclientclickplayer", BaseVarType.INTEGER);
    public static final Type GCLIENTCLICKTILE = new Type("gclientclicktile", BaseVarType.INTEGER);
    public static final Type IF_SCRIPT = new Type("if_script", BaseVarType.INTEGER);
    public static final Type DBCOLUMN = new Type("dbcolumn", BaseVarType.INTEGER);
    public static final Type VAR_PLAYER = new Type("var_player", BaseVarType.INTEGER);
    public static final Type VAR_PLAYER_BIT = new Type("var_player_bit", BaseVarType.INTEGER);
    public static final Type VAR_CLIENT = new Type("var_client", BaseVarType.INTEGER);
    public static final Type VAR_CLIENT_STRING = new Type("var_client_string", BaseVarType.INTEGER);
    public static final Type VAR_CLAN_SETTING = new Type("var_clan_setting", BaseVarType.INTEGER);
    public static final Type VAR_CLAN = new Type("var_clan", BaseVarType.INTEGER);
    public static final Type VAR_CONTROLLER = new Type("var_controller", BaseVarType.INTEGER);
    public static final Type VAR_CONTROLLER_BIT = new Type("var_controller_bit", BaseVarType.INTEGER);
    public static final Type VAR_GLOBAL = new Type("var_global", BaseVarType.INTEGER);
    public static final Type VAR_NPC = new Type("var_npc", BaseVarType.INTEGER);
    public static final Type VAR_NPC_BIT = new Type("var_npc_bit", BaseVarType.INTEGER);
    public static final Type VAR_OBJ = new Type("var_obj", BaseVarType.INTEGER);
    public static final Type VAR_SHARED = new Type("var_shared", BaseVarType.INTEGER);
    public static final Type VAR_SHARED_STRING = new Type("var_shared_string", BaseVarType.INTEGER);

    // type aliases (act as normal types, except that on conflict they propagate int_int, and type name is formatted as base type)
    public static final Type INT_INT = new Type("int", Type.INT);
    public static final Type INT_BOOLEAN = new Type("intbool", Type.INT);
    public static final Type INT_CHATFILTER = new Type("chatfilter", Type.INT);
    public static final Type INT_CHATTYPE = new Type("chattype", Type.INT);
    public static final Type INT_CLIENTTYPE = new Type("clienttype", Type.INT);
    public static final Type INT_PLATFORMTYPE = new Type("platformtype", Type.INT);
    public static final Type INT_IFTYPE = new Type("iftype", Type.INT);
    public static final Type INT_KEY = new Type("key", Type.INT);
    public static final Type INT_SETPOSH = new Type("setposh", Type.INT);
    public static final Type INT_SETPOSV = new Type("setposv", Type.INT);
    public static final Type INT_SETSIZE = new Type("setsize", Type.INT);
    public static final Type INT_SETTEXTALIGNH = new Type("settextalignh", Type.INT);
    public static final Type INT_SETTEXTALIGNV = new Type("settextalignv", Type.INT);
    public static final Type INT_WINDOWMODE = new Type("windowmode", Type.INT);
    public static final Type INT_GAMEOPTION = new Type("gameoption", Type.INT);
    public static final Type INT_DEVICEOPTION = new Type("deviceoption", Type.INT);
    public static final Type INT_MENUENTRYTYPE = new Type("menuentrytype", Type.INT);
    public static final Type INT_BLENDMODE = new Type("blendmode", Type.INT);
    public static final Type INT_OBJOWNER = new Type("objowner", Type.INT);
    public static final Type INT_RGB = new Type("rgb", Type.INT);
    public static final Type INT_OPKIND = new Type("opkind", Type.INT);
    public static final Type INT_OPMODE = new Type("opmode", Type.INT);
    public static final Type INT_CLAN = new Type("clan", Type.INT);
    public static final Type INT_OVERLAYTYPE = new Type("overlaytype", Type.INT);

    // type sets
    public static final Type UNKNOWN = new Type("unknown"); // any type
    public static final Type UNKNOWN_INT = new Type("unknown_int"); // any int stack type
    public static final Type UNKNOWN_INT_NOTBOOLEAN = new Type("unknown_int_notboolean"); // any int stack type except boolean
    public static final Type UNKNOWN_INT_NOTINT = new Type("unknown_int_notint"); // any int stack type except int
    public static final Type UNKNOWN_INT_NOTINT_NOTBOOLEAN = new Type("unknown_int_notint_notboolean"); // any int stack type except int or boolean
    public static final Type UNKNOWN_LONG = new Type("unknown_long"); // any long stack type
    public static final Type UNKNOWN_OBJECT = new Type("unknown_object"); // any object stack type
    public static final Type CONFLICT = new Type("conflict"); // no type possible

    // a few arrays referenced by unpacker
    public static final Type UNKNOWNARRAY = UNKNOWN.array(); // new Type("unknownarray", List.of(Unpack.VERSION < 231 ? UNKNOWN_INT : UNKNOWN_OBJECT)); // any array type
    public static final Type UNKNOWN_INTARRAY = UNKNOWN_INT.array();
    public static final Type INTARRAY = INT.array();
    public static final Type COMPONENTARRAY = COMPONENT.array();
    public static final Type STRINGARRAY = STRING.array();

    // fake types used by unpacker
    public static final Type ARGUMENT_LIST = new Type("argument_list");
    public static final Type CONDITION = new Type("condition");

    // information about which types are more specific than other types to pick which
    // to propagate during type inference (this is not a subtyping relation, namedobjarray
    // is more specific than objarray, but not a subtype)
    public static final Lattice<Type> LATTICE = new Lattice<>();

    static {
        for (var type : TYPES) {
            if (type.base == BaseVarType.INTEGER) {
                if (type.alias == INT) {
                    LATTICE.add(type, INT);
                    LATTICE.add(INT_INT, type);
                } else if (type == INT) {
                    LATTICE.add(type, UNKNOWN_INT_NOTBOOLEAN);
                } else if (type == BOOLEAN) {
                    LATTICE.add(type, UNKNOWN_INT_NOTINT);
                } else {
                    LATTICE.add(type, UNKNOWN_INT_NOTINT_NOTBOOLEAN);
                }
            } else if (type.base == BaseVarType.LONG) {
                LATTICE.add(type, UNKNOWN_LONG);
            } else {
                LATTICE.add(type, UNKNOWN);
            }
        }

        LATTICE.add(UNKNOWN_INT, UNKNOWN);
        LATTICE.add(UNKNOWN_OBJECT, UNKNOWN);
        LATTICE.add(UNKNOWN_LONG, UNKNOWN);
        LATTICE.add(UNKNOWNARRAY, Unpack.VERSION >= 231 ? UNKNOWN_OBJECT : UNKNOWN_INT);
        LATTICE.add(UNKNOWN_INT_NOTINT, UNKNOWN_INT);
        LATTICE.add(UNKNOWN_INT_NOTBOOLEAN, UNKNOWN_INT);
        LATTICE.add(UNKNOWN_INT_NOTINT_NOTBOOLEAN, UNKNOWN_INT_NOTINT);
        LATTICE.add(UNKNOWN_INT_NOTINT_NOTBOOLEAN, UNKNOWN_INT_NOTBOOLEAN);
        LATTICE.add(STRING, UNKNOWN_OBJECT);
        LATTICE.add(NAMEDOBJ, OBJ);

        // mirror the lattice for arrays
        for (var x : TYPES) {
            for (var y : LATTICE.upper(x)) {
                LATTICE.add(x.array(), y.array());
            }
        }

        // add bottom type
        for (var type : TYPES) {
            LATTICE.add(CONFLICT, type);
        }
    }

    public final String name;
    public final Type alias;
    public final BaseVarType base; // only defined for real types
    public final Type array;
    public final Type element;

    private Type(String name) {
        this.name = name;
        this.alias = null;
        this.base = null;
        this.array = new Type(this);
        this.element = null;
        BY_NAME.put(name, this);
        TYPES.add(this);
    }

    private Type(Type element) {
        this.name = element.name + "array";
        this.alias = null;
        this.base = Unpack.VERSION < 231 ? BaseVarType.INTEGER : BaseVarType.ARRAY;
        this.array = null;
        this.element = element;
        BY_NAME.put(name, this);
        TYPES.add(this);
    }

    private Type(String name, BaseVarType base) {
        this.name = name;
        this.alias = null;
        this.base = base;
        this.array = new Type(this);
        this.element = null;
        BY_NAME.put(name, this);
        TYPES.add(this);
    }

    private Type(String name, Type alias) {
        this.name = name;
        this.alias = alias;
        this.base = alias.base;
        this.array = new Type(this);
        this.element = null;
        BY_NAME.put(name, this);
        TYPES.add(this);
    }

    public Type array() {
        return array;
    }

    public Type element() {
        return element;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Type byName(String name) {
        return BY_NAME.get(name);
    }

    /**
     * Version of {@link #byName(String)} that returns an alias-enabled version of `int` and `intarray`.
     */
    public static Type byNameAlias(String name) {
        if (name.equals("int")) {
            return Type.INT;
        } else if (name.equals("intarray")) {
            return Type.INTARRAY;
        }

        return byName(name);
    }

    public static Type byID(int id) {
        return switch (id) {
            case 0 -> INT;
            case 1 -> BOOLEAN;
            case 2 -> HASH32;
            case 3 -> QUEST;
            case 4 -> QUESTHELP;
            case 5 -> CURSOR;
            case 6 -> SEQ;
            case 7 -> COLOUR;
            case 8 -> LOC_SHAPE;
            case 9 -> COMPONENT;
            case 10 -> IDKIT;
            case 11 -> MIDI;
            case 12 -> NPC_MODE;
            case 13 -> NAMEDOBJ;
            case 14 -> SYNTH;
            case 15 -> AI_QUEUE;
            case 16 -> AREA;
            case 17 -> STAT;
            case 18 -> NPC_STAT;
            case 19 -> WRITEINV;
            case 20 -> MESH;
            case 21 -> MAPAREA;
            case 22 -> COORDGRID;
            case 23 -> GRAPHIC;
            case 24 -> CHATPHRASE;
            case 25 -> FONTMETRICS;
            case 26 -> ENUM;
            case 27 -> HUNT;
            case 28 -> JINGLE;
            case 29 -> CHATCAT;
            case 30 -> LOC;
            case 31 -> MODEL;
            case 32 -> NPC;
            case 33 -> OBJ;
            case 34 -> PLAYER_UID;
            case 35 -> REGION_UID;
            case 36 -> STRING;
            case 37 -> SPOTANIM;
            case 38 -> NPC_UID;
            case 39 -> INV;
            case 40 -> TEXTURE;
            case 41 -> CATEGORY;
            case 42 -> CHAR;
            case 43 -> LASER;
            case 44 -> BAS;
            case 45 -> CONTROLLER;
            case 46 -> COLLISION_GEOMETRY;
            case 47 -> PHYSICS_MODEL;
            case 48 -> PHYSICS_CONTROL_MODIFIER;
            case 49 -> CLANHASH;
            case 51 -> CUTSCENE;
            case 53 -> ITEMCODE;
            case 54 -> PVPKILLS;
            case 55 -> MAPSCENEICON;
            case 56 -> CLANFORUMQFC;
            case 57 -> VORBIS;
            case 58 -> VERIFY_OBJECT;
            case 59 -> MAPELEMENT;
            case 60 -> CATEGORYTYPE;
            case 61 -> SOCIAL_NETWORK;
            case 62 -> HITMARK;
            case 63 -> PACKAGE;
            case 64 -> PARTICLE_EFFECTOR;
            case 65 -> CONTROLLER_UID;
            case 66 -> PARTICLE_EMITTER;
            case 67 -> PLOGTYPE;
            case 68 -> UNSIGNED_INT;
            case 69 -> SKYBOX;
            case 70 -> SKYDECOR;
            case 71 -> HASH64;
            case 72 -> INPUTTYPE;
            case 73 -> STRUCT;
            case 74 -> DBROW;
            case 75 -> STORABLELABEL;
            case 76 -> STORABLEPROC;
            case 77 -> GAMELOGEVENT;
            case 78 -> ANIMATIONCLIP;
            case 79 -> SKELETON;
            case 80 -> REGIONVISIBILITY;
            case 81 -> FMODHANDLE;
            case 83 -> REGION_ALLOWLOGIN;
            case 84 -> REGION_INFO;
            case 85 -> REGION_INFO_FAILURE;
            case 86 -> SERVER_ACCOUNT_CREATION_STEP;
            case 87 -> CLIENT_ACCOUNT_CREATION_STEP;
            case 88 -> LOBBY_ACCOUNT_CREATION_STEP;
            case 89 -> GWC_PLATFORM;
            case 90 -> CURRENCY;
            case 91 -> KEYBOARD_KEY;
            case 92 -> MOUSEEVENT;
            case 93 -> HEADBAR;
            case 94 -> BUG_TEMPLATE;
            case 95 -> BILLING_AUTH_FLAG;
            case 96 -> ACCOUNT_FEATURE_FLAG;
            case 97 -> INTERFACE;
            case 98 -> TOPLEVELINTERFACE;
            case 99 -> OVERLAYINTERFACE;
            case 100 -> CLIENTINTERFACE;
            case 101 -> MOVESPEED;
            case 102 -> MATERIAL;
            case 103 -> SEQGROUP;
            case 104 -> TEMP_HISCORE;
            case 105 -> TEMP_HISCORE_LENGTH_TYPE;
            case 106 -> TEMP_HISCORE_DISPLAY_TYPE;
            case 107 -> TEMP_HISCORE_CONTRIBUTE_RESULT;
            case 108 -> AUDIOGROUP;
            case 109 -> AUDIOMIXBUSS;
            case 110 -> LONG;
            case 111 -> CRM_CHANNEL;
            case 112 -> HTTP_IMAGE;
            case 113 -> POP_UP_DISPLAY_BEHAVIOUR;
            case 114 -> POLL;
            case 115 -> MTXN_PACKAGE;
            case 116 -> MTXN_PRICE_POINT;
            case 117 -> ENTITYOVERLAY;
            case 118 -> DBTABLE;
            case 200 -> COMPONENTARRAY;
            case 201 -> INTARRAY;
            case 202 -> LABEL;
            case 203 -> QUEUE;
            case 204 -> TIMER;
            case 205 -> WEAKQUEUE;
            case 206 -> SOFTTIMER;
            case 207 -> OBJVAR;
            case 208 -> WALKTRIGGER;
            case 209 -> VARP;
            default -> throw new IllegalArgumentException("unknown type id " + id);
        };
    }

    public static Type byChar(int id) {
        return switch (id) {
            case 'i' -> INT;
            case '1' -> BOOLEAN;
            case '2' -> HASH32;
            case ':' -> QUEST;
            case ';' -> QUESTHELP;
            case '@' -> CURSOR;
            case 'A' -> SEQ;
            case 'C' -> COLOUR;
            case 'H' -> LOC_SHAPE;
            case 'I' -> COMPONENT;
            case 'K' -> IDKIT;
            case 'M' -> MIDI;
            case 'N' -> NPC_MODE;
            case 'O' -> NAMEDOBJ;
            case 'P' -> SYNTH;
            case 'Q' -> AI_QUEUE;
            case 'R' -> AREA;
            case 'S' -> STAT;
            case 'T' -> NPC_STAT;
            case 'V' -> WRITEINV;
            case '^' -> MESH;
            case '`' -> MAPAREA;
            case 'c' -> COORDGRID;
            case 'd' -> GRAPHIC;
            case 'e' -> CHATPHRASE;
            case 'f' -> FONTMETRICS;
            case 'g' -> ENUM;
            case 'h' -> HUNT;
            case 'j' -> JINGLE;
            case 'k' -> CHATCAT;
            case 'l' -> LOC;
            case 'm' -> MODEL;
            case 'n' -> NPC;
            case 'o' -> OBJ;
            case 'p' -> PLAYER_UID;
            case 'r' -> REGION_UID;
            case 's' -> STRING;
            case 't' -> SPOTANIM;
            case 'u' -> NPC_UID;
            case 'v' -> INV;
            case 'x' -> TEXTURE;
            case 'y' -> CATEGORY;
            case 'z' -> CHAR;
            case '|' -> LASER;
            case '€' -> BAS;
            case 'ƒ' -> CONTROLLER;
            case '‡' -> COLLISION_GEOMETRY;
            case '‰' -> PHYSICS_MODEL;
            case 'Š' -> PHYSICS_CONTROL_MODIFIER;
            case 'Œ' -> CLANHASH;
            case 'š' -> CUTSCENE;
            case '¡' -> ITEMCODE;
            case '¢' -> PVPKILLS;
            case '£' -> MAPSCENEICON;
            case '§' -> CLANFORUMQFC;
            case '«' -> VORBIS;
            case '®' -> VERIFY_OBJECT;
            case 'µ' -> MAPELEMENT;
            case '¶' -> CATEGORYTYPE;
            case 'Æ' -> SOCIAL_NETWORK;
            case '×' -> HITMARK;
            case 'Þ' -> PACKAGE;
            case 'á' -> PARTICLE_EFFECTOR;
            case 'æ' -> CONTROLLER_UID;
            case 'é' -> PARTICLE_EMITTER;
            case 'í' -> PLOGTYPE;
            case 'î' -> UNSIGNED_INT;
            case 'ó' -> SKYBOX;
            case 'ú' -> SKYDECOR;
            case 'û' -> HASH64;
            case 'Î' -> INPUTTYPE;
            case 'J' -> STRUCT;
            case 'Ð' -> DBROW;
            case '¤' -> STORABLELABEL;
            case '¥' -> STORABLEPROC;
            case 'è' -> GAMELOGEVENT;
            case '¹' -> ANIMATIONCLIP;
            case '°' -> SKELETON;
            case 'ì' -> REGIONVISIBILITY;
            case 'ë' -> FMODHANDLE;
            case 'þ' -> REGION_ALLOWLOGIN;
            case 'ý' -> REGION_INFO;
            case 'ÿ' -> REGION_INFO_FAILURE;
            case 'õ' -> SERVER_ACCOUNT_CREATION_STEP;
            case 'ô' -> CLIENT_ACCOUNT_CREATION_STEP;
            case 'ö' -> LOBBY_ACCOUNT_CREATION_STEP;
            case 'ò' -> GWC_PLATFORM;
            case 'Ü' -> CURRENCY;
            case 'ù' -> KEYBOARD_KEY;
            case 'ï' -> MOUSEEVENT;
            case '¯' -> HEADBAR;
            case 'ê' -> BUG_TEMPLATE;
            case 'ð' -> BILLING_AUTH_FLAG;
            case 'å' -> ACCOUNT_FEATURE_FLAG;
            case 'a' -> INTERFACE;
            case 'F' -> TOPLEVELINTERFACE;
            case 'L' -> OVERLAYINTERFACE;
            case '©' -> CLIENTINTERFACE;
            case 'Ý' -> MOVESPEED;
            case '¬' -> MATERIAL;
            case 'ø' -> SEQGROUP;
            case 'ä' -> TEMP_HISCORE;
            case 'ã' -> TEMP_HISCORE_LENGTH_TYPE;
            case 'â' -> TEMP_HISCORE_DISPLAY_TYPE;
            case 'à' -> TEMP_HISCORE_CONTRIBUTE_RESULT;
            case 'À' -> AUDIOGROUP;
            case 'Ò' -> AUDIOMIXBUSS;
            case 'Ï' -> LONG;
            case 'Ì' -> CRM_CHANNEL;
            case 'É' -> HTTP_IMAGE;
            case 'Ê' -> POP_UP_DISPLAY_BEHAVIOUR;
            case '÷' -> POLL;
            case '¼' -> MTXN_PACKAGE;
            case '½' -> MTXN_PRICE_POINT;
            case '-' -> ENTITYOVERLAY;
            case 'Ø' -> DBTABLE;
            case 'X' -> COMPONENTARRAY;
            case 'W' -> INTARRAY;
            case 'b' -> LABEL;
            case 'B' -> QUEUE;
            case '4' -> TIMER;
            case 'w' -> WEAKQUEUE;
            case 'q' -> SOFTTIMER;
            case '0' -> OBJVAR;
            case '6' -> WALKTRIGGER;
            case '#' -> UNKNOWN1;
            case '(' -> UNKNOWN2;
            case '%' -> UNKNOWN3;
            case '&' -> UNKNOWN4;
            case ')' -> UNKNOWN5;
            case '3' -> UNKNOWN6;
            case '5' -> UNKNOWN7;
            case '7' -> VARP;
            case '8' -> UNKNOWN9;
            case '9' -> UNKNOWN10;
            case 'D' -> UNKNOWN11;
            case 'G' -> UNKNOWN12;
            case 'U' -> UNKNOWN13;
            case 'Á' -> UNKNOWN14;
            case 'Z' -> UNKNOWN15;
            case '~' -> UNKNOWN16;
            case '±' -> UNKNOWN17;
            case '»' -> UNKNOWN18;
            case '¿' -> UNKNOWN19;
            case 'Ç' -> UNKNOWN20;
            case 'Ñ' -> UNKNOWN21;
            case 'ñ' -> UNKNOWN22;
            case 'Ù' -> UNKNOWN23;
            case 'ß' -> UNKNOWN24;
            case 'E' -> UNKNOWN25;
            case 'Y' -> TRANSMIT_LIST;
            case 'Ä' -> UNKNOWN27;
            case 'ü' -> UNKNOWN28;
            case 'Ú' -> UNKNOWN29;
            case 'Û' -> UNKNOWN30;
            case 'Ó' -> UNKNOWN31;
            case 'È' -> UNKNOWN32;
            case 'Ô' -> UNKNOWN33;
            case '¾' -> UNKNOWN34;
            case 'Ö' -> UNKNOWN35;
            case '³' -> UNKNOWN36;
            case '·' -> UNKNOWN37;
            case 'º' -> UNKNOWN41;
            case '!' -> UNKNOWN46;
            case '$' -> UNKNOWN47;
            case '?' -> UNKNOWN48;
            case 'ç' -> UNKNOWN49;
            case '*' -> UNKNOWN50;
            case '¸' -> STRINGVECTOR;
            default -> throw new IllegalArgumentException("unknown type char " + id);
        };
    }
}
