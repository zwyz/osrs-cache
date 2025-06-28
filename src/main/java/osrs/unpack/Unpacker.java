package osrs.unpack;

import osrs.unpack.script.ScriptUnpacker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Unpacker {
    public static final boolean DUMP_CONFIG_IDS = false;

    public static final Map<Integer, String> SCRIPT_NAME = new HashMap<>();
    public static final Map<Integer, String> GRAPHIC_NAME = new HashMap<>();
    public static final Map<Integer, String> MIDI_NAME = new HashMap<>();
    public static final Map<Integer, String> BINARY_NAME = new HashMap<>();
    public static final Map<Integer, String> MAPAREA_NAME = new HashMap<>();
    public static final Map<Integer, String> OBJ_NAME = new HashMap<>();
    public static final Map<Integer, String> NPC_NAME = new HashMap<>();
    public static final Map<Integer, String> INV_NAME = new HashMap<>();
    public static final Map<Integer, String> VARP_NAME = new HashMap<>();
    public static final Map<Integer, String> VARBIT_NAME = new HashMap<>();
    public static final Map<Integer, String> LOC_NAME = new HashMap<>();
    public static final Map<Integer, String> SEQ_NAME = new HashMap<>();
    public static final Map<Integer, String> SPOTANIM_NAME = new HashMap<>();
    public static final Map<Integer, String> DBROW_NAME = new HashMap<>();
    public static final Map<Integer, String> DBTABLE_NAME = new HashMap<>();
    public static final Map<Integer, String> DBCOLUMN_NAME = new HashMap<>();
    public static final Map<Integer, String> JINGLE_NAME = new HashMap<>();
    public static final Map<Integer, String> INTERFACE_NAME = new HashMap<>();
    public static final Map<Integer, String> COMPONENT_NAME = new HashMap<>();
    public static final Map<Integer, List<Type>> DBCOLUMN_TYPE = new HashMap<>();
    public static final Map<Integer, Type> PARAM_TYPE = new HashMap<>();
    public static final Map<Integer, Type> ENUM_INPUT_TYPE = new HashMap<>();
    public static final Map<Integer, Type> ENUM_OUTPUT_TYPE = new HashMap<>();
    public static final Map<Integer, Type> VAR_CLAN_SETTING_TYPE = new HashMap<>();
    public static final Map<Integer, Type> VAR_CLAN_TYPE = new HashMap<>();
    public static final Map<Integer, Type> VAR_PLAYER_TYPE = new HashMap<>();
    public static final Map<Integer, Type> VAR_CLIENT_TYPE = new HashMap<>();

    public static void reset() {
        SCRIPT_NAME.clear();
        GRAPHIC_NAME.clear();
        MIDI_NAME.clear();
        BINARY_NAME.clear();
        MAPAREA_NAME.clear();
        DBCOLUMN_TYPE.clear();
        PARAM_TYPE.clear();
        ENUM_INPUT_TYPE.clear();
        ENUM_OUTPUT_TYPE.clear();
        VAR_CLAN_SETTING_TYPE.clear();
        VAR_CLAN_TYPE.clear();
    }

    public static String format(Type type, int value) {
        return format(type, value, true);
    }

    public static String format(Type type, int value, boolean safe) {
        if (type == Type.INT || type == Type.INT_INT) return switch (value) {
            case Integer.MIN_VALUE -> "^min_32bit_int";
            case Integer.MAX_VALUE -> "^max_32bit_int";
            default -> String.valueOf(value);
        };

        if (type == Type.BOOLEAN) return switch (value) {
            case -1 -> "null";
            case 0 -> "false";
            case 1 -> "true";
            default -> throw new IllegalArgumentException("invalid boolean");
        };

        if (type == Type.COORDGRID) {
            if (value == -1) {
                return "null";
            }

            var level = value >>> 28;
            var x = value >>> 14 & 16383;
            var z = value & 16383;
            return level + "_" + (x / 64) + "_" + (z / 64) + "_" + (x % 64) + "_" + (z % 64);
        }

        if (type == Type.TYPE) return Type.byChar(value).name;
        if (type == Type.GRAPHIC || type == Type.FONTMETRICS) return formatName(value, GRAPHIC_NAME, "graphic", safe);
        if (type == Type.MIDI) return formatName(value, MIDI_NAME, "midi", safe);
        if (type == Type.MAPAREA) return formatName(value, MAPAREA_NAME, "maparea", safe);
        if (type == Type.OBJ) return formatName(value, OBJ_NAME, "obj", safe);
        if (type == Type.NPC) return formatName(value, NPC_NAME, "npc", safe);
        if (type == Type.INV) return formatName(value, INV_NAME, "inv", safe);
        if (type == Type.LOC) return formatName(value, LOC_NAME, "loc", safe);
        if (type == Type.SEQ) return formatName(value, SEQ_NAME, "seq", safe);
        if (type == Type.SPOTANIM) return formatName(value, SPOTANIM_NAME, "spotanim", safe);
        if (type == Type.DBROW) return formatName(value, DBROW_NAME, "dbrow", safe);
        if (type == Type.DBTABLE) return formatName(value, DBTABLE_NAME, "dbtable", safe);
        if (type == Type.JINGLE) return formatName(value, JINGLE_NAME, "jingle", safe);
        if (type == Type.INTERFACE) return formatName(value, INTERFACE_NAME, "interface", safe);
        if (type == Type.TOPLEVELINTERFACE) return formatName(value, INTERFACE_NAME, "toplevelinterface", safe);
        if (type == Type.OVERLAYINTERFACE) return formatName(value, INTERFACE_NAME, "overlayinterface", safe);
        if (type == Type.CLIENTINTERFACE) return formatName(value, INTERFACE_NAME, "clientinterface", safe);
        if (type == Type.VAR_PLAYER || type == Type.VARP) return formatName(value, VARP_NAME, "varplayer", safe);
        if (type == Type.VAR_PLAYER_BIT) return formatName(value, VARBIT_NAME, "varplayerbit", safe);
        if (type == Type.VAR_CLIENT) return "varclient_" + value;
        if (type == Type.VAR_CLIENT_STRING) return "varclientstring_" + value;
        if (type == Type.VAR_CLAN_SETTING) return "varclansetting" + getVarClanSettingType(value).name + "_" + value;
        if (type == Type.VAR_CLAN) return "varclan" + getVarClanType(value).name + "_" + value;
        if (type == Type.VAR_CONTROLLER) return "varcontroller_" + value;
        if (type == Type.VAR_CONTROLLER_BIT) return "varcontrollerbit_" + value;
        if (type == Type.VAR_GLOBAL) return "varglobal_" + value;
        if (type == Type.VAR_NPC) return "varnpc_" + value;
        if (type == Type.VAR_NPC_BIT) return "varnpcbit_" + value;
        if (type == Type.VAR_OBJ) return "varobject_" + value;
        if (type == Type.VAR_SHARED) return "varworld_" + value;
        if (type == Type.VAR_SHARED_STRING) return "varworldstring_" + value;

        if (type == Type.COMPONENT) {
            if (value == -1) {
                return "null";
            }

            var name = format(Type.INTERFACE, value >> 16) + ":" + COMPONENT_NAME.getOrDefault(value, "com_" + (value & 0xffff));
            if (safe) name = quote(name);
            return name;
        }

        if (type == Type.DBCOLUMN) {
            var table = value >>> 12;
            var column = (value >>> 4) & 255;
            var tuple = (value & 15) - 1;
            var name = format(Type.DBTABLE, table) + ":" + DBCOLUMN_NAME.getOrDefault((table << 16) | column, "col" + column);
            if (tuple != -1) name = name + ":" + tuple;
            if (safe) name = quote(name);
            return name;
        }

        if (type == Type.MOVESPEED) return switch (value) {
            case 0 -> "stationary";
            case 1 -> "crawl";
            case 2 -> "walk";
            case 3 -> "run";
            case 4 -> "instant";
            default -> throw new IllegalArgumentException("invalid movespeed");
        };

        if (type == Type.LOC_SHAPE) return switch (value) {
            case 0 -> "1";
            case 1 -> "2";
            case 2 -> "3";
            case 3 -> "4";
            case 4 -> "Q";
            case 5 -> "W";
            case 6 -> "E";
            case 7 -> "R";
            case 8 -> "T";
            case 9 -> "5";
            case 10 -> "8";
            case 11 -> "9";
            case 12 -> "A";
            case 13 -> "S";
            case 14 -> "D";
            case 15 -> "F";
            case 16 -> "G";
            case 17 -> "H";
            case 18 -> "Z";
            case 19 -> "X";
            case 20 -> "C";
            case 21 -> "V";
            case 22 -> "0";
            default -> throw new IllegalArgumentException("" + value);
        };

        if (type == Type.STAT) return switch (value) {
            case -1 -> "null";
            case 0 -> "attack";
            case 1 -> "defence";
            case 2 -> "strength";
            case 3 -> "hitpoints";
            case 4 -> "ranged";
            case 5 -> "prayer";
            case 6 -> "magic";
            case 7 -> "cooking";
            case 8 -> "woodcutting";
            case 9 -> "fletching";
            case 10 -> "fishing";
            case 11 -> "firemaking";
            case 12 -> "crafting";
            case 13 -> "smithing";
            case 14 -> "mining";
            case 15 -> "herblore";
            case 16 -> "agility";
            case 17 -> "thieving";
            case 18 -> "slayer";
            case 19 -> "farming";
            case 20 -> "runecraft";
            case 21 -> "hunter";
            case 22 -> "construction";
            case 23 -> "sailing";
            default -> "stat_" + value;
        };

        if (type == Type.CLIENTSCRIPT || type == Type.CLIENTOPNPC || type == Type.CLIENTOPLOC || type == Type.CLIENTOPOBJ || type == Type.CLIENTOPPLAYER || type == Type.CLIENTOPTILE) {
            if (value == -1) {
                return "null";
            }

            var name = getScriptName(value);
            name = name.substring(1, name.length() - 1);
            name = name.split(",")[1];
            return name;
        }

        if (type == Type.NAMEDOBJ) return format(Type.OBJ, value);

        if (type == Type.INT_BOOLEAN) return switch (value) {
            case -1 -> "null";
            case 0 -> "^false";
            case 1 -> "^true";
            default -> "" + value;
        };

        if (type == Type.INT_CHATFILTER) return switch (value) {
            case -1 -> "null";
            case 0 -> "^chatfilter_on";
            case 1 -> "^chatfilter_friends";
            case 2 -> "^chatfilter_off";
            case 3 -> "^chatfilter_hide";
            case 4 -> "^chatfilter_autochat";
            default -> "^chatfilter_" + value;
        };

        if (type == Type.INT_CHATTYPE) return switch (value) { // https://twitter.com/TheCrazy0neTv/status/1100567742602756096
            case -1 -> "null";
            case 0 -> "^chattype_gamemessage";
            case 1 -> "^chattype_modchat";
            case 2 -> "^chattype_publicchat";
            case 3 -> "^chattype_privatechat";
            case 4 -> "^chattype_engine";
            case 5 -> "^chattype_loginlogoutnotification";
            case 6 -> "^chattype_privatechatout";
            case 7 -> "^chattype_modprivatechat";
            case 9 -> "^chattype_friendschat";
            case 11 -> "^chattype_friendschatnotification";
            case 14 -> "^chattype_broadcast";
            case 26 -> "^chattype_snapshotfeedback";
            case 27 -> "^chattype_obj_examine";
            case 28 -> "^chattype_npc_examine";
            case 29 -> "^chattype_loc_examine";
            case 30 -> "^chattype_friendnotification";
            case 31 -> "^chattype_ignorenotification";
            case 41 -> "^chattype_clanchat";
            case 43 -> "^chattype_clanmessage";
            case 44 -> "^chattype_clanguestchat";
            case 46 -> "^chattype_clanguestmessage";
            case 90 -> "^chattype_autotyper";
            case 91 -> "^chattype_modautotyper";
            case 99 -> "^chattype_console";
            case 101 -> "^chattype_tradereq";
            case 102 -> "^chattype_trade";
            case 103 -> "^chattype_chalreq_trade";
            case 104 -> "^chattype_chalreq_friendschat";
            case 105 -> "^chattype_spam";
            case 106 -> "^chattype_playerrelated";
            case 107 -> "^chattype_10sectimeout";
            case 109 -> "^chattype_clancreationinvitation";
            case 110 -> "^chattype_chalreq_clanchat";
            case 114 -> "^chattype_dialogue";
            case 115 -> "^chattype_mesbox";
            default -> "^chattype_" + value;
        };

        if (type == Type.INT_CLIENTTYPE) return switch (value) {
            case -1 -> "null";
            case 1 -> "^clienttype_desktop";
            case 2 -> "^clienttype_android";
            case 3 -> "^clienttype_ios";
            case 4 -> "^clienttype_enhanced_windows";
            case 5 -> "^clienttype_enhanced_mac";
            case 7 -> "^clienttype_enhanced_android";
            case 8 -> "^clienttype_enhanced_ios";
            case 10 -> "^clienttype_enhanced_linux";
            default -> "^clienttype_" + value;
        };

        if (type == Type.INT_PLATFORMTYPE) return switch (value) {
            case -1 -> "null";
            case 0 -> "^platformtype_default";
            case 1 -> "^platformtype_steam";
            case 2 -> "^platformtype_android";
            case 3 -> "^platformtype_apple";
            case 5 -> "^platformtype_jagex";
            default -> "^platformtype_" + value;
        };

        if (type == Type.INT_IFTYPE) return switch (value) {
            case -1 -> "null";
            case 0 -> "^iftype_layer";
            case 3 -> "^iftype_rectangle";
            case 4 -> "^iftype_text";
            case 5 -> "^iftype_graphic";
            case 6 -> "^iftype_model";
            case 9 -> "^iftype_line";
            case 10 -> "^iftype_arc";
            case 11 -> "^iftype_crmview";
            case 12 -> "^iftype_inputfield";
            default -> "^iftype_" + value;
        };

        if (type == Type.INT_KEY) return switch (value) {
            case -1 -> "null";
            case 0 -> "0";
            case 1 -> "^key_f1";
            case 2 -> "^key_f2";
            case 3 -> "^key_f3";
            case 4 -> "^key_f4";
            case 5 -> "^key_f5";
            case 6 -> "^key_f6";
            case 7 -> "^key_f7";
            case 8 -> "^key_f8";
            case 9 -> "^key_f9";
            case 10 -> "^key_f10";
            case 11 -> "^key_f11";
            case 12 -> "^key_f12";
            case 13 -> "^key_escape";
            case 16 -> "^key_1";
            case 17 -> "^key_2";
            case 18 -> "^key_3";
            case 19 -> "^key_4";
            case 20 -> "^key_5";
            case 21 -> "^key_6";
            case 22 -> "^key_7";
            case 23 -> "^key_8";
            case 24 -> "^key_9";
            case 25 -> "^key_0";
            case 26 -> "^key_minus";
            case 27 -> "^key_equals";
            case 28 -> "^key_console";
            case 32 -> "^key_q";
            case 33 -> "^key_w";
            case 34 -> "^key_e";
            case 35 -> "^key_r";
            case 36 -> "^key_t";
            case 37 -> "^key_y";
            case 38 -> "^key_u";
            case 39 -> "^key_i";
            case 40 -> "^key_o";
            case 41 -> "^key_p";
            case 42 -> "^key_left_bracket";
            case 43 -> "^key_right_bracket";
            case 48 -> "^key_a";
            case 49 -> "^key_s";
            case 50 -> "^key_d";
            case 51 -> "^key_f";
            case 52 -> "^key_g";
            case 53 -> "^key_h";
            case 54 -> "^key_j";
            case 55 -> "^key_k";
            case 56 -> "^key_l";
            case 57 -> "^key_semicolon";
            case 58 -> "^key_apostrophe";
            case 59 -> "^key_win_left";
            case 64 -> "^key_z";
            case 65 -> "^key_x";
            case 66 -> "^key_c";
            case 67 -> "^key_v";
            case 68 -> "^key_b";
            case 69 -> "^key_n";
            case 70 -> "^key_m";
            case 71 -> "^key_comma";
            case 72 -> "^key_period";
            case 73 -> "^key_slash";
            case 74 -> "^key_backslash";
            case 80 -> "^key_tab";
            case 81 -> "^key_shift_left";
            case 82 -> "^key_control_left";
            case 83 -> "^key_space";
            case 84 -> "^key_return";
            case 85 -> "^key_backspace";
            case 86 -> "^key_alt_left";
            case 87 -> "^key_numpad_add";
            case 88 -> "^key_numpad_subtract";
            case 89 -> "^key_numpad_multiply";
            case 90 -> "^key_numpad_divide";
            case 91 -> "^key_clear";
            case 96 -> "^key_left";
            case 97 -> "^key_right";
            case 98 -> "^key_up";
            case 99 -> "^key_down";
            case 100 -> "^key_insert";
            case 101 -> "^key_del";
            case 102 -> "^key_home";
            case 103 -> "^key_end";
            case 104 -> "^key_page_up";
            case 105 -> "^key_page_down";
            default -> "^key_" + value;
        };

        if (type == Type.INT_SETPOSH) return switch (value) {
            case -1 -> "null";
            case 0 -> "^setpos_abs_left";
            case 1 -> "^setpos_abs_centre";
            case 2 -> "^setpos_abs_right";
            case 3 -> "^setpos_proportion_left";
            case 4 -> "^setpos_proportion_centre";
            case 5 -> "^setpos_proportion_right";
            default -> "^setposh_" + value;
        };

        if (type == Type.INT_SETPOSV) return switch (value) {
            case -1 -> "null";
            case 0 -> "^setpos_abs_top";
            case 1 -> "^setpos_abs_centre";
            case 2 -> "^setpos_abs_bottom";
            case 3 -> "^setpos_proportion_top";
            case 4 -> "^setpos_proportion_centre";
            case 5 -> "^setpos_proportion_bottom";
            default -> "^setposv_" + value;
        };

        if (type == Type.INT_SETSIZE) return switch (value) {
            case -1 -> "null";
            case 0 -> "^setsize_abs";
            case 1 -> "^setsize_minus";
            case 2 -> "^setsize_proportion";
            default -> "^setsize_" + value;
        };

        if (type == Type.INT_SETTEXTALIGNH) return switch (value) {
            case -1 -> "null";
            case 0 -> "^settextalign_left";
            case 1 -> "^settextalign_centre";
            case 2 -> "^settextalign_right";
            default -> "^settextalignh_" + value;
        };

        if (type == Type.INT_SETTEXTALIGNV) return switch (value) {
            case -1 -> "null";
            case 0 -> "^settextalign_top";
            case 1 -> "^settextalign_centre";
            case 2 -> "^settextalign_bottom";
            default -> "^settextalignv_" + value;
        };

        if (type == Type.INT_WINDOWMODE) return switch (value) { // tfu
            case -1 -> "null";
            case 0 -> "0";
            case 1 -> "^windowmode_small";
            case 2 -> "^windowmode_resizable";
            default -> "^windowmode_" + value;
        };

        if (type == Type.INT_DEVICEOPTION) return switch (value) { // from c++ client
            case -1 -> "null";
            case 2 -> "^deviceoption_hide_user_name";
            case 3 -> "^deviceoption_mute_title_screen";
            case 4 -> "^deviceoption_display_fps";
            case 5 -> "^deviceoption_fps_limit";
            case 6 -> "^deviceoption_brightness";
            case 10 -> "^deviceoption_window_width";
            case 11 -> "^deviceoption_window_height";
            case 12 -> "^deviceoption_window_topmost";
            case 14 -> "^deviceoption_draw_distance";
            case 15 -> "^deviceoption_ui_quality";
            case 16 -> "^deviceoption_display_build_info";
            case 17 -> "^deviceoption_full_screen";
            case 19 -> "^deviceoption_master_volume";
            case 20 -> "^deviceoption_anti_aliasing_sample_level";
            case 21 -> "^deviceoption_plugin_safe_mode";
            case 22 -> "^deviceoption_is_sfx_8_bit";
            case 23 -> "^deviceoption_afk_timeout";
            case 24 -> "^deviceoption_anisotropy_exponent";
            default -> "^deviceoption_" + value;
        };

        if (type == Type.INT_GAMEOPTION) return switch (value) { // from c++ client
            case -1 -> "null";
            case 1 -> "^gameoption_remove_roof";
            case 2 -> "^gameoption_haptic_on_op";
            case 3 -> "^gameoption_haptic_on_drag";
            case 4 -> "^gameoption_haptic_on_minimenu_open";
            case 5 -> "^gameoption_haptic_on_minimenu_entry_hover";
            case 6 -> "^gameoption_minimenu_long_press_time";
            case 7 -> "^gameoption_midi_volume";
            case 8 -> "^gameoption_wave_volume";
            case 9 -> "^gameoption_ambient_volume";
            case 10 -> "^gameoption_chat_timestamp_mode";
            case 11 -> "^gameoption_camera_sensitivity";
            case 12 -> "^gameoption_draw_minimenu_header";
            case 13 -> "^gameoption_minimenu_mouse_start_index";
            default -> "^gameoption_" + value;
        };

        if (type == Type.INT_MENUENTRYTYPE) return switch (value) {
            case -1 -> "null";
            case 0 -> "^menuentrytype_none";
            case 1 -> "^menuentrytype_tile";
            case 2 -> "^menuentrytype_npc";
            case 3 -> "^menuentrytype_loc";
            case 4 -> "^menuentrytype_obj";
            case 6 -> "^menuentrytype_player";
            case 7 -> "^menuentrytype_component";
            case 8 -> "^menuentrytype_worldmapelement";
            default -> "^menuentrytype_" + value;
        };

        if (type == Type.INT_RGB) return switch (value) {
            case -1 -> "null";
            case 0xff0000 -> "^red";
            case 0x00ff00 -> "^green";
            case 0x0000ff -> "^blue";
            case 0xffff00 -> "^yellow";
            case 0xff00ff -> "^magenta";
            case 0x00ffff -> "^cyan";
            case 0xffffff -> "^white";
            case 0x000000 -> "^black";
            default -> "0x" + Integer.toHexString(value);
        };

        if (type == Type.INT_GRADIENTMODE) return switch (value) {
            case -1 -> "null";
            case 0 -> "^gradientmode_none";
            case 1 -> "^gradientmode_colour";
            case 2 -> "^gradientmode_colourtrans";
            default -> "^gradientmode_" + value;
        };

        if (type == Type.INT_OBJOWNER) return switch (value) {
            case -1 -> "null";
            case 0 -> "^objowner_none";
            case 1 -> "^objowner_self";
            case 2 -> "^objowner_other";
            case 3 -> "^objowner_group";
            default -> "^objowner_" + value;
        };

        if (type == Type.INT_OPKIND) return switch (value) {
            case -1 -> "null";
            case 0 -> "^opkind_entityserver";
            case 1 -> "^opkind_target";
            case 2 -> "^opkind_entity";
            case 3 -> "^opkind_component";
            case 4 -> "^opkind_walk";
            case 5 -> "^opkind_shiftop";
            case 6 -> "^opkind_player";
            case 7 -> "^opkind_use";
            case 8 -> "^opkind_cancel";
            case 9 -> "^opkind_examine";
            case 10 -> "^opkind_unknown";
            case 11 -> "^opkind_obj";
            default -> "^opkind_" + value;
        };

        if (type == Type.INT_OPMODE) return switch (value) {
            case -1 -> "null";
            case 0 -> "^opmode_always";
            case 1 -> "^opmode_never";
            case 2 -> "^opmode_shift";
            case 3 -> "^opmode_noshift";
            default -> "^opmode_" + value;
        };

        if (type == Type.INT_CLAN) return switch (value) {
            case 0 -> "^clantype_clan";
            case 1 -> "^clantype_gim";
            case 2 -> "^clantype_pvpa_group";
            default -> "^clantype_" + value;
        };

        if (type == Type.INTARRAY) return "intarray" + value;
        if (type == Type.COMPONENTARRAY) return "componentarray" + value;

        if (value == -1) {
            return "null";
        }

        return type.name + "_" + value;
    }

    private static String formatName(int value, Map<Integer, String> names, String prefix, boolean safe) {
        var name = value == -1 ? "null" : names.getOrDefault(value, prefix + "_" + value);
        if (safe) name = quote(name);
        return name;
    }

    private static String quote(String name) {
        if (!name.matches("[a-zA-Z0-9_.:]+")) {
            name = "\"" + name + "\"";
        }

        return name;
    }

    public static String format(Type type, Long value) {
        return value + "L";
    }

    public static String format(Type type, String value) {
        return value;
    }

    public static String getBooleanName(int id) {
        return switch (id) {
            case 0 -> "no";
            case 1 -> "yes";
            default -> throw new IllegalArgumentException("" + id);
        };
    }

    public static String getReplaceModeName(int id) {
        return switch (id) {
            case 0 -> "ignore"; // continues the current animation
            case 1 -> "reset"; // resets frame and loop counter
            case 2 -> "extend"; // resets loop counter only
            default -> throw new IllegalArgumentException("id " + id);
        };
    }

    public static String getPreanimMoveName(int i) {
        return switch (i) {
            case 0 -> "delaymove";
            case 1 -> "delayanim";
            case 2 -> "merge";
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    public static String getPostanimMoveName(int i) {
        return switch (i) {
            case 0 -> "delaymove";
            case 1 -> "abortanim";
            case 2 -> "merge";
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    public static void setEnumInputType(int id, Type type) {
        ENUM_INPUT_TYPE.put(id, type);
    }

    public static Type getEnumInputType(int id) {
        return Objects.requireNonNull(ENUM_INPUT_TYPE.get(id));
    }

    public static void setEnumOutputType(int id, Type type) {
        ENUM_OUTPUT_TYPE.put(id, type);
    }

    public static Type getEnumOutputType(int id) {
        return ENUM_OUTPUT_TYPE.get(id);
    }

    public static void setParamType(int id, Type type) {
        PARAM_TYPE.put(id, type);
    }

    public static Type getParamType(int operand) {
        return Objects.requireNonNull(PARAM_TYPE.get(operand));
    }

    public static void setDBColumnType(int table, int column, List<Type> types) {
        DBCOLUMN_TYPE.put((table << 16) | column, types);
    }

    private static List<Type> getDBColumnType(int table, int column) {
        return Objects.requireNonNull(DBCOLUMN_TYPE.get((table << 16) | column));
    }

    public static List<Type> getDBColumnTypeTuple(int table, int column, int tuple) {
        var types = getDBColumnType(table, column);

        if (tuple == -1) {
            return types;
        } else {
            return List.of(types.get(tuple));
        }
    }

    public static Type getDBColumnTypeTupleAssertSingle(int table, int column, int tuple) {
        var types = getDBColumnTypeTuple(table, column, tuple);

        if (types.size() != 1) {
            throw new IllegalStateException("required single type, got " + types.size());
        }

        return types.get(0);
    }

    public static void setVarClanSettingType(int var, Type type) {
        VAR_CLAN_SETTING_TYPE.put(var, type);
    }

    public static Type getVarClanSettingType(int var) {
        return Objects.requireNonNull(VAR_CLAN_SETTING_TYPE.get(var));
    }

    public static void setVarClanType(int var, Type type) {
        VAR_CLAN_TYPE.put(var, type);
    }

    public static Type getVarClanType(int var) {
        return Objects.requireNonNull(VAR_CLAN_TYPE.get(var));
    }

    public static void setVarPlayerType(int var, Type type) {
        VAR_PLAYER_TYPE.put(var, type);
    }

    public static Type getVarPlayerType(int var) {
        return VAR_PLAYER_TYPE.get(var);
    }

    public static void setVarClientType(int var, Type type) {
        VAR_CLIENT_TYPE.put(var, type);
    }

    public static Type getVarClientType(int var) {
        return VAR_CLIENT_TYPE.get(var);
    }

    public static String formatColour(int colour) {
        var hex = Integer.toHexString(colour);

        if (hex.length() > 6) {
            return "0x" + "0".repeat(8 - hex.length()) + hex;
        } else {
            return "0x" + "0".repeat(6 - hex.length()) + hex;
        }
    }

    public static void setWorldMapAreaName(int id, String name) {
        MAPAREA_NAME.put(id, name);
    }

    public static String formatWearPos(int slot) {
        return switch (slot) {
            case 0 -> "hat";
            case 1 -> "back";
            case 2 -> "front";
            case 3 -> "righthand";
            case 4 -> "torso";
            case 5 -> "lefthand";
            case 6 -> "arms";
            case 7 -> "legs";
            case 8 -> "head";
            case 9 -> "hands";
            case 10 -> "feet";
            case 11 -> "jaw";
            case 12 -> "ring";
            case 13 -> "quiver";
            default -> throw new IllegalArgumentException("wearpos " + slot);
        };
    }

    public static String getScriptName(int id) {
        if (SCRIPT_NAME.containsKey(id)) {
            return SCRIPT_NAME.get(id);
        }

        var trigger = ScriptUnpacker.SCRIPT_TRIGGERS.get(id);
        if (trigger == null) {
            trigger = !ScriptUnpacker.CALLED.contains(id) && ScriptUnpacker.getReturnTypes(id).isEmpty() ? ScriptTrigger.CLIENTSCRIPT : ScriptTrigger.PROC;
        }
        return "[" + trigger.name().toLowerCase() + ",script" + id + "]";
    }
}
