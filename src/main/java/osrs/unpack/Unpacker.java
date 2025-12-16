package osrs.unpack;

import osrs.Unpack;
import osrs.unpack.script.ScriptUnpacker;

import java.util.*;

public class Unpacker {
    public static final Map<Type, Map<Integer, String>> NAME = new HashMap<>();
    public static final Map<Integer, String> SCRIPT_NAME = new HashMap<>();
    public static final Map<Integer, String> BINARY_NAME = new HashMap<>();
    public static final Map<Integer, Map<Integer, List<Type>>> DBCOLUMN_TYPE = new HashMap<>();
    public static final Map<Integer, Type> PARAM_TYPE = new HashMap<>();
    public static final Map<Integer, Type> ENUM_INPUT_TYPE = new HashMap<>();
    public static final Map<Integer, Type> ENUM_OUTPUT_TYPE = new HashMap<>();
    public static final Map<Integer, Type> VAR_CLAN_SETTING_TYPE = new HashMap<>();
    public static final Map<Integer, Type> VAR_CLAN_TYPE = new HashMap<>();
    public static final Map<Integer, Type> VAR_PLAYER_TYPE = new HashMap<>();
    public static final Map<Integer, Type> VAR_CLIENT_TYPE = new HashMap<>();
    public static final Map<Integer, List<Type>> IF_SCRIPT_TYPE = new HashMap<>();
    public static final Set<Integer> OPTIONAL_COLUMNS = new HashSet<>();
    public static final Set<Integer> LIST_COLUMNS = new HashSet<>();
    public static final Set<Integer> INDEXED_COLUMNS = new HashSet<>();
    public static final Map<Integer, Integer> COLUMN_COUNTS = new HashMap<>();
    public static final Map<Integer, Map<Integer, IfType>> IF_TYPES = new LinkedHashMap<>();

    public static void reset() {
        NAME.clear();
        SCRIPT_NAME.clear();
        BINARY_NAME.clear();
        DBCOLUMN_TYPE.clear();
        PARAM_TYPE.clear();
        ENUM_INPUT_TYPE.clear();
        ENUM_OUTPUT_TYPE.clear();
        VAR_CLAN_SETTING_TYPE.clear();
        VAR_CLAN_TYPE.clear();
        IF_SCRIPT_TYPE.clear();
        OPTIONAL_COLUMNS.clear();
        LIST_COLUMNS.clear();
        INDEXED_COLUMNS.clear();
        COLUMN_COUNTS.clear();
        IF_TYPES.clear();

        setSymbolName(Type.BOOLEAN, 0, "false");
        setSymbolName(Type.BOOLEAN, 1, "true");

        setSymbolName(Type.MOVESPEED, 0, "stationary");
        setSymbolName(Type.MOVESPEED, 1, "crawl");
        setSymbolName(Type.MOVESPEED, 2, "walk");
        setSymbolName(Type.MOVESPEED, 3, "run");
        setSymbolName(Type.MOVESPEED, 4, "instant");

        setSymbolName(Type.LOC_SHAPE, 0, "1");
        setSymbolName(Type.LOC_SHAPE, 1, "2");
        setSymbolName(Type.LOC_SHAPE, 2, "3");
        setSymbolName(Type.LOC_SHAPE, 3, "4");
        setSymbolName(Type.LOC_SHAPE, 4, "Q");
        setSymbolName(Type.LOC_SHAPE, 5, "W");
        setSymbolName(Type.LOC_SHAPE, 6, "E");
        setSymbolName(Type.LOC_SHAPE, 7, "R");
        setSymbolName(Type.LOC_SHAPE, 8, "T");
        setSymbolName(Type.LOC_SHAPE, 9, "5");
        setSymbolName(Type.LOC_SHAPE, 10, "8");
        setSymbolName(Type.LOC_SHAPE, 11, "9");
        setSymbolName(Type.LOC_SHAPE, 12, "A");
        setSymbolName(Type.LOC_SHAPE, 13, "S");
        setSymbolName(Type.LOC_SHAPE, 14, "D");
        setSymbolName(Type.LOC_SHAPE, 15, "F");
        setSymbolName(Type.LOC_SHAPE, 16, "G");
        setSymbolName(Type.LOC_SHAPE, 17, "H");
        setSymbolName(Type.LOC_SHAPE, 18, "Z");
        setSymbolName(Type.LOC_SHAPE, 19, "X");
        setSymbolName(Type.LOC_SHAPE, 20, "C");
        setSymbolName(Type.LOC_SHAPE, 21, "V");
        setSymbolName(Type.LOC_SHAPE, 22, "0");

        setSymbolName(Type.STAT, 0, "attack");
        setSymbolName(Type.STAT, 1, "defence");
        setSymbolName(Type.STAT, 2, "strength");
        setSymbolName(Type.STAT, 3, "hitpoints");
        setSymbolName(Type.STAT, 4, "ranged");
        setSymbolName(Type.STAT, 5, "prayer");
        setSymbolName(Type.STAT, 6, "magic");
        setSymbolName(Type.STAT, 7, "cooking");
        setSymbolName(Type.STAT, 8, "woodcutting");
        setSymbolName(Type.STAT, 9, "fletching");
        setSymbolName(Type.STAT, 10, "fishing");
        setSymbolName(Type.STAT, 11, "firemaking");
        setSymbolName(Type.STAT, 12, "crafting");
        setSymbolName(Type.STAT, 13, "smithing");
        setSymbolName(Type.STAT, 14, "mining");
        setSymbolName(Type.STAT, 15, "herblore");
        setSymbolName(Type.STAT, 16, "agility");
        setSymbolName(Type.STAT, 17, "thieving");
        setSymbolName(Type.STAT, 18, "slayer");
        setSymbolName(Type.STAT, 19, "farming");
        setSymbolName(Type.STAT, 20, "runecraft");
        setSymbolName(Type.STAT, 21, "hunter");
        setSymbolName(Type.STAT, 22, "construction");
        setSymbolName(Type.STAT, 23, "sailing");

        setSymbolName(Type.INT_INT, Integer.MIN_VALUE, "^min_32bit_int");
        setSymbolName(Type.INT_INT, Integer.MAX_VALUE, "^max_32bit_int");

        setSymbolName(Type.INT_BOOLEAN, 0, "^false");
        setSymbolName(Type.INT_BOOLEAN, 1, "^true");

        setSymbolName(Type.INT_RGB, 0xff0000, "^red");
        setSymbolName(Type.INT_RGB, 0x00ff00, "^green");
        setSymbolName(Type.INT_RGB, 0x0000ff, "^blue");
        setSymbolName(Type.INT_RGB, 0xffff00, "^yellow");
        setSymbolName(Type.INT_RGB, 0xff00ff, "^magenta");
        setSymbolName(Type.INT_RGB, 0x00ffff, "^cyan");
        setSymbolName(Type.INT_RGB, 0xffffff, "^white");
        setSymbolName(Type.INT_RGB, 0x000000, "^black");

        setSymbolName(Type.INT_CHATFILTER, 0, "^chatfilter_on");
        setSymbolName(Type.INT_CHATFILTER, 1, "^chatfilter_friends");
        setSymbolName(Type.INT_CHATFILTER, 2, "^chatfilter_off");
        setSymbolName(Type.INT_CHATFILTER, 3, "^chatfilter_hide");
        setSymbolName(Type.INT_CHATFILTER, 4, "^chatfilter_autochat");

        // https://twitter.com/TheCrazy0neTv/status/1100567742602756096
        setSymbolName(Type.INT_CHATTYPE, 0, "^chattype_gamemessage");
        setSymbolName(Type.INT_CHATTYPE, 1, "^chattype_modchat");
        setSymbolName(Type.INT_CHATTYPE, 2, "^chattype_publicchat");
        setSymbolName(Type.INT_CHATTYPE, 3, "^chattype_privatechat");
        setSymbolName(Type.INT_CHATTYPE, 4, "^chattype_engine");
        setSymbolName(Type.INT_CHATTYPE, 5, "^chattype_loginlogoutnotification");
        setSymbolName(Type.INT_CHATTYPE, 6, "^chattype_privatechatout");
        setSymbolName(Type.INT_CHATTYPE, 7, "^chattype_modprivatechat");
        setSymbolName(Type.INT_CHATTYPE, 9, "^chattype_friendschat");
        setSymbolName(Type.INT_CHATTYPE, 11, "^chattype_friendschatnotification");
        setSymbolName(Type.INT_CHATTYPE, 14, "^chattype_broadcast");
        setSymbolName(Type.INT_CHATTYPE, 26, "^chattype_snapshotfeedback");
        setSymbolName(Type.INT_CHATTYPE, 27, "^chattype_obj_examine");
        setSymbolName(Type.INT_CHATTYPE, 28, "^chattype_npc_examine");
        setSymbolName(Type.INT_CHATTYPE, 29, "^chattype_loc_examine");
        setSymbolName(Type.INT_CHATTYPE, 30, "^chattype_friendnotification");
        setSymbolName(Type.INT_CHATTYPE, 31, "^chattype_ignorenotification");
        setSymbolName(Type.INT_CHATTYPE, 41, "^chattype_clanchat");
        setSymbolName(Type.INT_CHATTYPE, 43, "^chattype_clanmessage");
        setSymbolName(Type.INT_CHATTYPE, 44, "^chattype_clanguestchat");
        setSymbolName(Type.INT_CHATTYPE, 46, "^chattype_clanguestmessage");
        setSymbolName(Type.INT_CHATTYPE, 90, "^chattype_autotyper");
        setSymbolName(Type.INT_CHATTYPE, 91, "^chattype_modautotyper");
        setSymbolName(Type.INT_CHATTYPE, 99, "^chattype_console");
        setSymbolName(Type.INT_CHATTYPE, 101, "^chattype_tradereq");
        setSymbolName(Type.INT_CHATTYPE, 102, "^chattype_trade");
        setSymbolName(Type.INT_CHATTYPE, 103, "^chattype_chalreq_trade");
        setSymbolName(Type.INT_CHATTYPE, 104, "^chattype_chalreq_friendschat");
        setSymbolName(Type.INT_CHATTYPE, 105, "^chattype_spam");
        setSymbolName(Type.INT_CHATTYPE, 106, "^chattype_playerrelated");
        setSymbolName(Type.INT_CHATTYPE, 107, "^chattype_10sectimeout");
        setSymbolName(Type.INT_CHATTYPE, 109, "^chattype_clancreationinvitation");
        setSymbolName(Type.INT_CHATTYPE, 110, "^chattype_chalreq_clanchat");
        setSymbolName(Type.INT_CHATTYPE, 114, "^chattype_dialogue");
        setSymbolName(Type.INT_CHATTYPE, 115, "^chattype_mesbox");

        setSymbolName(Type.INT_CLIENTTYPE, 1, "^clienttype_desktop");
        setSymbolName(Type.INT_CLIENTTYPE, 2, "^clienttype_android");
        setSymbolName(Type.INT_CLIENTTYPE, 3, "^clienttype_ios");
        setSymbolName(Type.INT_CLIENTTYPE, 4, "^clienttype_enhanced_windows");
        setSymbolName(Type.INT_CLIENTTYPE, 5, "^clienttype_enhanced_mac");
        setSymbolName(Type.INT_CLIENTTYPE, 7, "^clienttype_enhanced_android");
        setSymbolName(Type.INT_CLIENTTYPE, 8, "^clienttype_enhanced_ios");
        setSymbolName(Type.INT_CLIENTTYPE, 10, "^clienttype_enhanced_linux");

        setSymbolName(Type.INT_PLATFORMTYPE, 0, "^platformtype_default");
        setSymbolName(Type.INT_PLATFORMTYPE, 1, "^platformtype_steam");
        setSymbolName(Type.INT_PLATFORMTYPE, 2, "^platformtype_android");
        setSymbolName(Type.INT_PLATFORMTYPE, 3, "^platformtype_apple");
        setSymbolName(Type.INT_PLATFORMTYPE, 5, "^platformtype_jagex");

        setSymbolName(Type.INT_IFTYPE, 0, "^iftype_layer");
        setSymbolName(Type.INT_IFTYPE, 3, "^iftype_rectangle");
        setSymbolName(Type.INT_IFTYPE, 4, "^iftype_text");
        setSymbolName(Type.INT_IFTYPE, 5, "^iftype_graphic");
        setSymbolName(Type.INT_IFTYPE, 6, "^iftype_model");
        setSymbolName(Type.INT_IFTYPE, 9, "^iftype_line");
        setSymbolName(Type.INT_IFTYPE, 10, "^iftype_circle");
        setSymbolName(Type.INT_IFTYPE, 11, "^iftype_crmview");
        setSymbolName(Type.INT_IFTYPE, 12, "^iftype_inputfield");

        setSymbolName(Type.INT_KEY, 0, "0");
        setSymbolName(Type.INT_KEY, 1, "^key_f1");
        setSymbolName(Type.INT_KEY, 2, "^key_f2");
        setSymbolName(Type.INT_KEY, 3, "^key_f3");
        setSymbolName(Type.INT_KEY, 4, "^key_f4");
        setSymbolName(Type.INT_KEY, 5, "^key_f5");
        setSymbolName(Type.INT_KEY, 6, "^key_f6");
        setSymbolName(Type.INT_KEY, 7, "^key_f7");
        setSymbolName(Type.INT_KEY, 8, "^key_f8");
        setSymbolName(Type.INT_KEY, 9, "^key_f9");
        setSymbolName(Type.INT_KEY, 10, "^key_f10");
        setSymbolName(Type.INT_KEY, 11, "^key_f11");
        setSymbolName(Type.INT_KEY, 12, "^key_f12");
        setSymbolName(Type.INT_KEY, 13, "^key_escape");
        setSymbolName(Type.INT_KEY, 16, "^key_1");
        setSymbolName(Type.INT_KEY, 17, "^key_2");
        setSymbolName(Type.INT_KEY, 18, "^key_3");
        setSymbolName(Type.INT_KEY, 19, "^key_4");
        setSymbolName(Type.INT_KEY, 20, "^key_5");
        setSymbolName(Type.INT_KEY, 21, "^key_6");
        setSymbolName(Type.INT_KEY, 22, "^key_7");
        setSymbolName(Type.INT_KEY, 23, "^key_8");
        setSymbolName(Type.INT_KEY, 24, "^key_9");
        setSymbolName(Type.INT_KEY, 25, "^key_0");
        setSymbolName(Type.INT_KEY, 26, "^key_minus");
        setSymbolName(Type.INT_KEY, 27, "^key_equals");
        setSymbolName(Type.INT_KEY, 28, "^key_console");
        setSymbolName(Type.INT_KEY, 32, "^key_q");
        setSymbolName(Type.INT_KEY, 33, "^key_w");
        setSymbolName(Type.INT_KEY, 34, "^key_e");
        setSymbolName(Type.INT_KEY, 35, "^key_r");
        setSymbolName(Type.INT_KEY, 36, "^key_t");
        setSymbolName(Type.INT_KEY, 37, "^key_y");
        setSymbolName(Type.INT_KEY, 38, "^key_u");
        setSymbolName(Type.INT_KEY, 39, "^key_i");
        setSymbolName(Type.INT_KEY, 40, "^key_o");
        setSymbolName(Type.INT_KEY, 41, "^key_p");
        setSymbolName(Type.INT_KEY, 42, "^key_left_bracket");
        setSymbolName(Type.INT_KEY, 43, "^key_right_bracket");
        setSymbolName(Type.INT_KEY, 48, "^key_a");
        setSymbolName(Type.INT_KEY, 49, "^key_s");
        setSymbolName(Type.INT_KEY, 50, "^key_d");
        setSymbolName(Type.INT_KEY, 51, "^key_f");
        setSymbolName(Type.INT_KEY, 52, "^key_g");
        setSymbolName(Type.INT_KEY, 53, "^key_h");
        setSymbolName(Type.INT_KEY, 54, "^key_j");
        setSymbolName(Type.INT_KEY, 55, "^key_k");
        setSymbolName(Type.INT_KEY, 56, "^key_l");
        setSymbolName(Type.INT_KEY, 57, "^key_semicolon");
        setSymbolName(Type.INT_KEY, 58, "^key_apostrophe");
        setSymbolName(Type.INT_KEY, 59, "^key_win_left");
        setSymbolName(Type.INT_KEY, 64, "^key_z");
        setSymbolName(Type.INT_KEY, 65, "^key_x");
        setSymbolName(Type.INT_KEY, 66, "^key_c");
        setSymbolName(Type.INT_KEY, 67, "^key_v");
        setSymbolName(Type.INT_KEY, 68, "^key_b");
        setSymbolName(Type.INT_KEY, 69, "^key_n");
        setSymbolName(Type.INT_KEY, 70, "^key_m");
        setSymbolName(Type.INT_KEY, 71, "^key_comma");
        setSymbolName(Type.INT_KEY, 72, "^key_period");
        setSymbolName(Type.INT_KEY, 73, "^key_slash");
        setSymbolName(Type.INT_KEY, 74, "^key_backslash");
        setSymbolName(Type.INT_KEY, 80, "^key_tab");
        setSymbolName(Type.INT_KEY, 81, "^key_shift_left");
        setSymbolName(Type.INT_KEY, 82, "^key_control_left");
        setSymbolName(Type.INT_KEY, 83, "^key_space");
        setSymbolName(Type.INT_KEY, 84, "^key_return");
        setSymbolName(Type.INT_KEY, 85, "^key_backspace");
        setSymbolName(Type.INT_KEY, 86, "^key_alt_left");
        setSymbolName(Type.INT_KEY, 87, "^key_numpad_add");
        setSymbolName(Type.INT_KEY, 88, "^key_numpad_subtract");
        setSymbolName(Type.INT_KEY, 89, "^key_numpad_multiply");
        setSymbolName(Type.INT_KEY, 90, "^key_numpad_divide");
        setSymbolName(Type.INT_KEY, 91, "^key_clear");
        setSymbolName(Type.INT_KEY, 96, "^key_left");
        setSymbolName(Type.INT_KEY, 97, "^key_right");
        setSymbolName(Type.INT_KEY, 98, "^key_up");
        setSymbolName(Type.INT_KEY, 99, "^key_down");
        setSymbolName(Type.INT_KEY, 100, "^key_insert");
        setSymbolName(Type.INT_KEY, 101, "^key_del");
        setSymbolName(Type.INT_KEY, 102, "^key_home");
        setSymbolName(Type.INT_KEY, 103, "^key_end");
        setSymbolName(Type.INT_KEY, 104, "^key_page_up");
        setSymbolName(Type.INT_KEY, 105, "^key_page_down");

        setSymbolName(Type.INT_SETPOSH, 0, "^setpos_abs_left");
        setSymbolName(Type.INT_SETPOSH, 1, "^setpos_abs_centre");
        setSymbolName(Type.INT_SETPOSH, 2, "^setpos_abs_right");
        setSymbolName(Type.INT_SETPOSH, 3, "^setpos_rel_left");
        setSymbolName(Type.INT_SETPOSH, 4, "^setpos_rel_centre");
        setSymbolName(Type.INT_SETPOSH, 5, "^setpos_rel_right");

        setSymbolName(Type.INT_SETPOSV, 0, "^setpos_abs_top");
        setSymbolName(Type.INT_SETPOSV, 1, "^setpos_abs_centre");
        setSymbolName(Type.INT_SETPOSV, 2, "^setpos_abs_bottom");
        setSymbolName(Type.INT_SETPOSV, 3, "^setpos_rel_top");
        setSymbolName(Type.INT_SETPOSV, 4, "^setpos_rel_centre");
        setSymbolName(Type.INT_SETPOSV, 5, "^setpos_rel_bottom");

        setSymbolName(Type.INT_SETSIZE, 0, "^setsize_abs");
        setSymbolName(Type.INT_SETSIZE, 1, "^setsize_minus");
        setSymbolName(Type.INT_SETSIZE, 2, "^setsize_rel");

        setSymbolName(Type.INT_SETTEXTALIGNH, 0, "^settextalign_left");
        setSymbolName(Type.INT_SETTEXTALIGNH, 1, "^settextalign_centre");
        setSymbolName(Type.INT_SETTEXTALIGNH, 2, "^settextalign_right");

        setSymbolName(Type.INT_SETTEXTALIGNV, 0, "^settextalign_top");
        setSymbolName(Type.INT_SETTEXTALIGNV, 1, "^settextalign_centre");
        setSymbolName(Type.INT_SETTEXTALIGNV, 2, "^settextalign_bottom");

        // from tfu
        setSymbolName(Type.INT_WINDOWMODE, 0, "0");
        setSymbolName(Type.INT_WINDOWMODE, 1, "^windowmode_small");
        setSymbolName(Type.INT_WINDOWMODE, 2, "^windowmode_resizable");

        // from c++ client
        setSymbolName(Type.INT_DEVICEOPTION, 2, "^deviceoption_hide_user_name");
        setSymbolName(Type.INT_DEVICEOPTION, 3, "^deviceoption_mute_title_screen");
        setSymbolName(Type.INT_DEVICEOPTION, 4, "^deviceoption_display_fps");
        setSymbolName(Type.INT_DEVICEOPTION, 5, "^deviceoption_fps_limit");
        setSymbolName(Type.INT_DEVICEOPTION, 6, "^deviceoption_brightness");
        setSymbolName(Type.INT_DEVICEOPTION, 10, "^deviceoption_window_width");
        setSymbolName(Type.INT_DEVICEOPTION, 11, "^deviceoption_window_height");
        setSymbolName(Type.INT_DEVICEOPTION, 12, "^deviceoption_window_topmost");
        setSymbolName(Type.INT_DEVICEOPTION, 14, "^deviceoption_draw_distance");
        setSymbolName(Type.INT_DEVICEOPTION, 15, "^deviceoption_ui_quality");
        setSymbolName(Type.INT_DEVICEOPTION, 16, "^deviceoption_display_build_info");
        setSymbolName(Type.INT_DEVICEOPTION, 17, "^deviceoption_full_screen");
        setSymbolName(Type.INT_DEVICEOPTION, 19, "^deviceoption_master_volume");
        setSymbolName(Type.INT_DEVICEOPTION, 20, "^deviceoption_anti_aliasing_sample_level");
        setSymbolName(Type.INT_DEVICEOPTION, 21, "^deviceoption_plugin_safe_mode");
        setSymbolName(Type.INT_DEVICEOPTION, 22, "^deviceoption_is_sfx_8_bit");
        setSymbolName(Type.INT_DEVICEOPTION, 23, "^deviceoption_afk_timeout");
        setSymbolName(Type.INT_DEVICEOPTION, 24, "^deviceoption_anisotropy_exponent");
        setSymbolName(Type.INT_DEVICEOPTION, 25, "^deviceoption_force_disable_rseven");
        setSymbolName(Type.INT_DEVICEOPTION, 26, "^deviceoption_background_fps_limit");
        setSymbolName(Type.INT_DEVICEOPTION, 27, "^deviceoption_ui_scaling");
        setSymbolName(Type.INT_DEVICEOPTION, 28, "^deviceoption_vsync_mode");

        // from c++ client
        setSymbolName(Type.INT_GAMEOPTION, 1, "^gameoption_remove_roof");
        setSymbolName(Type.INT_GAMEOPTION, 2, "^gameoption_haptic_on_op");
        setSymbolName(Type.INT_GAMEOPTION, 3, "^gameoption_haptic_on_drag");
        setSymbolName(Type.INT_GAMEOPTION, 4, "^gameoption_haptic_on_minimenu_open");
        setSymbolName(Type.INT_GAMEOPTION, 5, "^gameoption_haptic_on_minimenu_entry_hover");
        setSymbolName(Type.INT_GAMEOPTION, 6, "^gameoption_minimenu_long_press_time");
        setSymbolName(Type.INT_GAMEOPTION, 7, "^gameoption_midi_volume");
        setSymbolName(Type.INT_GAMEOPTION, 8, "^gameoption_wave_volume");
        setSymbolName(Type.INT_GAMEOPTION, 9, "^gameoption_ambient_volume");
        setSymbolName(Type.INT_GAMEOPTION, 10, "^gameoption_chat_timestamp_mode");
        setSymbolName(Type.INT_GAMEOPTION, 11, "^gameoption_camera_sensitivity");
        setSymbolName(Type.INT_GAMEOPTION, 12, "^gameoption_draw_minimenu_header");
        setSymbolName(Type.INT_GAMEOPTION, 13, "^gameoption_minimenu_mouse_start_index");
        setSymbolName(Type.INT_GAMEOPTION, 14, "^gameoption_minimenu_spacing");
        setSymbolName(Type.INT_GAMEOPTION, 15, "^gameoption_afk_timeout");

        setSymbolName(Type.INT_MENUENTRYTYPE, 0, "^menuentrytype_none");
        setSymbolName(Type.INT_MENUENTRYTYPE, 1, "^menuentrytype_tile");
        setSymbolName(Type.INT_MENUENTRYTYPE, 2, "^menuentrytype_npc");
        setSymbolName(Type.INT_MENUENTRYTYPE, 3, "^menuentrytype_loc");
        setSymbolName(Type.INT_MENUENTRYTYPE, 4, "^menuentrytype_obj");
        setSymbolName(Type.INT_MENUENTRYTYPE, 6, "^menuentrytype_player");
        setSymbolName(Type.INT_MENUENTRYTYPE, 7, "^menuentrytype_component");
        setSymbolName(Type.INT_MENUENTRYTYPE, 8, "^menuentrytype_worldmapelement");

        setSymbolName(Type.INT_BLENDMODE, 0, "^blendmode_replace");
        setSymbolName(Type.INT_BLENDMODE, 1, "^blendmode_vgrad");
        setSymbolName(Type.INT_BLENDMODE, 2, "^blendmode_vgrad_trans");

        setSymbolName(Type.INT_OBJOWNER, 0, "^objowner_none");
        setSymbolName(Type.INT_OBJOWNER, 1, "^objowner_self");
        setSymbolName(Type.INT_OBJOWNER, 2, "^objowner_other");
        setSymbolName(Type.INT_OBJOWNER, 3, "^objowner_group");

        setSymbolName(Type.INT_OPKIND, 0, "^opkind_entityserver");
        setSymbolName(Type.INT_OPKIND, 1, "^opkind_target");
        setSymbolName(Type.INT_OPKIND, 2, "^opkind_entity");
        setSymbolName(Type.INT_OPKIND, 3, "^opkind_component");
        setSymbolName(Type.INT_OPKIND, 4, "^opkind_walk");
        setSymbolName(Type.INT_OPKIND, 5, "^opkind_shiftop");
        setSymbolName(Type.INT_OPKIND, 6, "^opkind_player");
        setSymbolName(Type.INT_OPKIND, 7, "^opkind_use");
        setSymbolName(Type.INT_OPKIND, 8, "^opkind_cancel");
        setSymbolName(Type.INT_OPKIND, 9, "^opkind_examine");
        setSymbolName(Type.INT_OPKIND, 10, "^opkind_unknown");
        setSymbolName(Type.INT_OPKIND, 11, "^opkind_obj");
        setSymbolName(Type.INT_OPKIND, 12, "^opking_loc");
        setSymbolName(Type.INT_OPKIND, 13, "^opkind_npc");
        setSymbolName(Type.INT_OPKIND, 14, "^opkind_worldentity");

        setSymbolName(Type.INT_OPMODE, 0, "^opmode_always");
        setSymbolName(Type.INT_OPMODE, 1, "^opmode_never");
        setSymbolName(Type.INT_OPMODE, 2, "^opmode_shift");
        setSymbolName(Type.INT_OPMODE, 3, "^opmode_noshift");

        setSymbolName(Type.INT_CLAN, 0, "^clantype_clan");
        setSymbolName(Type.INT_CLAN, 1, "^clantype_gim");
        setSymbolName(Type.INT_CLAN, 2, "^clantype_pvpa_group");

        if (Unpack.CONFIGS_VERSION >= 1765282238) {
            setDBColumnType(194, 5, List.of(Type.DBROW));
        }

        if (Unpack.CONFIGS_VERSION >= 4867) {
            // TODO note down names
            // TODO don't set column 2 after version that transmits it
            setDBColumnType(115, 2, List.of(Type.STRING));
            setDBColumnType(115, 4, List.of(Type.STRING));
        }
    }

    public static String format(Type type, int value) {
        return format(type, value, true);
    }

    public static String format(Type type, int value, boolean safe) {
        if (type == Type.INT) return format(Type.INT_INT, value, safe);
        if (type == Type.FONTMETRICS) return format(Type.GRAPHIC, value, safe);
        if (type == Type.VARP) return format(Type.VAR_PLAYER, value, safe);
        if (type == Type.NAMEDOBJ) return format(Type.OBJ, value, safe);
        if (type == Type.GCLIENTCLICKNPC) return format(Type.CLIENTSCRIPT, value, safe);
        if (type == Type.GCLIENTCLICKLOC) return format(Type.CLIENTSCRIPT, value, safe);
        if (type == Type.GCLIENTCLICKOBJ) return format(Type.CLIENTSCRIPT, value, safe);
        if (type == Type.GCLIENTCLICKPLAYER) return format(Type.CLIENTSCRIPT, value, safe);
        if (type == Type.GCLIENTCLICKTILE) return format(Type.CLIENTSCRIPT, value, safe);

        var name = NAME.getOrDefault(type, Map.of()).get(value);

        if (name != null) {
            return quote(name, safe);
        } else if (type == Type.TYPE) {
            return Type.byChar(value).name;
        } else if (type == Type.COORDGRID) {
            if (value == -1) {
                return "null";
            } else {
                var level = value >>> 28;
                var x = value >>> 14 & 16383;
                var z = value & 16383;
                return level + "_" + (x / 64) + "_" + (z / 64) + "_" + (x % 64) + "_" + (z % 64);
            }
        } else if (type == Type.COMPONENT) {
            if (value == -1) {
                return "null";
            } else {
                var itf = value >> 16;
                var com = value & 0xffff;
                return quote(format(Type.INTERFACE, itf, false) + ":com_" + com, safe);
            }
        } else if (type == Type.DBCOLUMN) {
            var table = value >>> 12;
            var column = (value >>> 4) & 255;
            var tuple = (value & 15) - 1;

            if (tuple != -1) {
                return quote(format(Type.DBCOLUMN, (table << 12) | (column << 4), false) + ":" + tuple, safe);
            } else {
                return quote(format(Type.DBTABLE, table, false) + ":col" + column, safe);
            }
        } else if (type == Type.CLIENTSCRIPT) {
            if (value == -1) {
                return "null";
            } else {
                return formatScriptShort(value);
            }
        } else if (type == Type.INT_INT) {
            return Integer.toString(value);
        } else if (type == Type.INT_RGB) {
            if (value == -1) {
                return "null";
            } else {
                return formatColour(value);
            }
        } else if (Type.LATTICE.test(type, Type.INT)) {
            if (value == -1) {
                return "null";
            } else {
                return Integer.toString(value);
            }
        } else {
            if (value == -1) {
                return "null";
            } else if (type == Type.TOPLEVELINTERFACE || type == Type.OVERLAYINTERFACE || type == Type.CLIENTINTERFACE) {
                name = NAME.getOrDefault(Type.INTERFACE, Map.of()).get(value);
            } else  {
                name = type.name.replace("_", "") + "_" + Integer.toUnsignedString(value);
            }

            Unpacker.setSymbolName(type, value, name);
            return name;
        }
    }

    private static String quote(String name, boolean safe) {
        if (safe && !name.matches("\\^?[a-zA-Z0-9_.:]+")) {
            return "\"" + name + "\"";
        } else {
            return name;
        }
    }

    public static String formatScriptShort(int value) {
        var result = getScriptName(value);
        result = result.substring(1, result.length() - 1);
        result = result.split(",")[1];
        return result;
    }

    public static String formatComponentShort(int value) {
        var result = format(Type.COMPONENT, value, false);
        result = result.substring(result.indexOf(':') + 1);
        return result;
    }

    public static String formatDBColumnShort(int value) {
        var result = format(Type.DBCOLUMN, value, false);
        result = result.substring(result.indexOf(':') + 1);
        return result;
    }

    public static String formatColour(int colour) {
        var hex = Integer.toHexString(colour);

        if (hex.length() > 6) {
            return "0x" + "0".repeat(8 - hex.length()) + hex;
        } else {
            return "0x" + "0".repeat(6 - hex.length()) + hex;
        }
    }

    public static String formatYesNo(int id) {
        return switch (id) {
            case 0 -> "no";
            case 1 -> "yes";
            default -> throw new IllegalArgumentException("invalid value: " + id);
        };
    }

    public static void setSymbolName(Type type, int id, String name) {
        NAME.computeIfAbsent(type, _ -> new HashMap<>()).put(id, name);
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
        var tableTypes = DBCOLUMN_TYPE.computeIfAbsent(table, _ -> new HashMap<>());
        tableTypes.put(column, types);
    }

    private static List<Type> getDBColumnType(int table, int column) {
        var typeTypes = Objects.requireNonNull(DBCOLUMN_TYPE.get(table));
        return Objects.requireNonNull(typeTypes.get(column), "no types for " + table + ", " + column);
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

    public static boolean isColumnOptional(int columnID) {
        return OPTIONAL_COLUMNS.contains(columnID);
    }

    public static void setColumnOptional(int columnID) {
        OPTIONAL_COLUMNS.add(columnID);
    }

    public static boolean isColumnList(int columnID) {
        return LIST_COLUMNS.contains(columnID);
    }

    public static void setColumnList(int columnID) {
        LIST_COLUMNS.add(columnID);
    }

    public static boolean isColumnIndexed(int columnID) {
        return INDEXED_COLUMNS.contains(columnID);
    }

    public static void setColumnIndexed(int columnID) {
        INDEXED_COLUMNS.add(columnID);
    }

    public static int getColumnCount(int table) {
        return COLUMN_COUNTS.getOrDefault(table, 0);
    }

    public static void setColumnCount(int table, int column) {
        COLUMN_COUNTS.put(table, column);
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
