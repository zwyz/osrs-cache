package osrs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import osrs.js5.*;
import osrs.unpack.*;
import osrs.unpack.config.*;
import osrs.unpack.map.Environment;
import osrs.unpack.script.Command;
import osrs.unpack.script.ScriptUnpacker;
import osrs.util.Packet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static osrs.unpack.Js5Archive.*;
import static osrs.unpack.Js5ConfigGroup.*;
import static osrs.unpack.Js5DefaultsGroup.GRAPHICS;
import static osrs.unpack.Js5WorldMapGroup.DETAILS;

// todo: clean this up
public class Unpack {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static int VERSION;
    private static Js5ResourceProvider PROVIDER;
    private static Js5MasterIndex MASTER_INDEX;
    public static int CONFIGS_VERSION;
    public static int CLIENTSCRIPTS_VERSION;

    public static void main(String[] args) throws IOException, InterruptedException {
        unpackLive("unpacked/live", 233, "oldschool1.runescape.com", 43594, null);
//        unpackOpenRS2("unpacked/beta", 223, "runescape", 1826);
    }

    public static void unpackOpenRS2(String path, int version, String scope, int id) throws IOException {
        unpack(path, version, new MemoryCacheResourceProvider(new FileSystemCacheResourceProvider(
                Path.of(System.getProperty("user.home") + "/.rscache/osrs"),
                new OpenRS2Js5ResourceProvider(scope, id))
        ));
    }

    public static void unpackLive(String path, int version, String host, int port, int[] key) throws IOException {
        unpack(path, version, new MemoryCacheResourceProvider(new FileSystemCacheResourceProvider(
                Path.of(System.getProperty("user.home") + "/.rscache/osrs"),
                new TcpJs5ResourceProvider(host, port, version, key))
        ));
    }

    public static void unpack(String path, int version, Js5ResourceProvider provider) throws IOException {
        VERSION = version;
        PROVIDER = provider;
        MASTER_INDEX = new Js5MasterIndex(Js5Util.decompress(Unpack.PROVIDER.get(255, 255, false)));
        CONFIGS_VERSION = MASTER_INDEX.getArchiveData(JS5_CONFIG.id).getVersion();
        CLIENTSCRIPTS_VERSION = MASTER_INDEX.getArchiveData(JS5_CLIENTSCRIPTS.id).getVersion();
        Command.reset(); // todo: make non-static
        Unpacker.reset(); // todo: make non-static
        ScriptUnpacker.reset(); // todo: make non-static

        // System.out.println("CONFIGS version: " + CONFIGS_VERSION);
        // System.out.println("CLIENTSCRIPTS version: " + CLIENTSCRIPTS_VERSION);

        Files.createDirectories(Path.of(path));
        Files.createDirectories(Path.of(path + "/config"));
        Files.createDirectories(Path.of(path + "/script"));
        Files.createDirectories(Path.of(path + "/interface"));
//        Files.createDirectories(Path.of(path + "/maps"));

        // load names
        loadDebugNames(Js5DebugNamesGroup.OBJTYPES, Unpacker.OBJ_NAME);
        loadDebugNames(Js5DebugNamesGroup.NPCTYPES, Unpacker.NPC_NAME);
        loadDebugNames(Js5DebugNamesGroup.INVTYPES, Unpacker.INV_NAME);
        loadDebugNames(Js5DebugNamesGroup.VARPTYPES, Unpacker.VARP_NAME);
        loadDebugNames(Js5DebugNamesGroup.VARBITTYPES, Unpacker.VARBIT_NAME);
        loadDebugNames(Js5DebugNamesGroup.LOCTYPES, Unpacker.LOC_NAME);
        loadDebugNames(Js5DebugNamesGroup.SEQTYPES, Unpacker.SEQ_NAME);
        loadDebugNames(Js5DebugNamesGroup.SPOTTYPES, Unpacker.SPOTANIM_NAME);
        loadDebugNames(Js5DebugNamesGroup.ROWTYPES, Unpacker.DBROW_NAME);
        loadDebugNames(Js5DebugNamesGroup.SOUNDTYPES, Unpacker.JINGLE_NAME);
        loadDebugNames(Js5DebugNamesGroup.VARCTYPES, Unpacker.VARC_NAME);
        loadDebugNamesInterface();
        loadDebugNamesDBTable();
        loadGroupNamesScriptTrigger(JS5_CLIENTSCRIPTS, Unpacker.SCRIPT_NAME);
        loadGroupNames(Path.of("data/names/scripts.txt"), JS5_CLIENTSCRIPTS, Unpacker.SCRIPT_NAME::put);
        loadGroupNames(Path.of("data/names/graphics.txt"), JS5_SPRITES, Unpacker.GRAPHIC_NAME::put);
        loadGroupNames(Path.of("data/names/midis.txt"), JS5_SONGS, Unpacker.MIDI_NAME::put);
        loadGroupNames(Path.of("data/names/binaries.txt"), JS5_BINARY, Unpacker.BINARY_NAME::put);

        // world map
        unpackWorldMapGroup(DETAILS, path + "/config/dump.wma");

        // things stuff depends on
        unpackConfigGroup(VARBIT, VarPlayerBitUnpacker::unpack, path + "/config/dump.varbit");
        unpackConfigGroup(VARPLAYER, VarPlayerUnpacker::unpack, path + "/config/dump.varp");
        unpackConfigGroup(VARCLIENT, VarClientUnpacker::unpack, path + "/config/dump.varc");
        unpackConfigGroup(VARCLIENTSTR, VarClientStringUnpacker::unpack, path + "/config/dump.varcstr");
        unpackConfigGroup(VAROBJ, VarObjUnpacker::unpack, path + "/config/dump.varobj"); // increased with treasure trail expansion
        unpackConfigGroup(VARSHARED, VarSharedUnpacker::unpack, path + "/config/dump.vars"); // increased with poh board https://twitter.com/JagexAsh/status/1610606943726456834
        unpackConfigGroup(VARSHAREDSTR, VarSharedStringUnpacker::unpack, path + "/config/dump.varsstr");
        unpackConfigGroup(VARNPC, VarNpcUnpacker::unpack, path + "/config/dump.varn");
        unpackConfigGroup(VARNPCBIT, VarNpcBitUnpacker::unpack, path + "/config/dump.varnbit");
        unpackConfigGroup(VARGLOBAL, VarGlobalUnpacker::unpack, path + "/config/dump.varg"); // matches leaderboards
        unpackConfigGroup(VARCONTROLLER, VarControllerUnpacker::unpack, path + "/config/dump.varcon"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(VARCONTROLLERBIT, VarControllerBitUnpacker::unpack, path + "/config/dump.varconbit"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(VAR_CLAN, VarClanUnpacker::unpack, path + "/config/dump.varclan");
        unpackConfigGroup(VAR_CLAN_SETTING, VarClanSettingUnpacker::unpack, path + "/config/dump.varclansetting");
        unpackConfigGroup(PARAMTYPE, ParamUnpacker::unpack, path + "/config/dump.param");

        // regular configs
        unpackConfigGroup(FLUTYPE, FloorUnderlayUnpacker::unpack, path + "/config/dump.flu");
        unpackConfigGroup(HUNTTYPE, HuntUnpacker::unpack, path + "/config/dump.hunt"); // https://youtu.be/5pvoMQUCla4?si=-BvlpFgRrAo0UrXb&t=4070
        unpackConfigGroup(IDKTYPE, IDKUnpacker::unpack, path + "/config/dump.idk");
        unpackConfigGroup(FLOTYPE, FloorOverlayUnpacker::unpack, path + "/config/dump.flo");
        unpackConfigGroup(INVTYPE, InvUnpacker::unpack, path + "/config/dump.inv");
        unpackConfigGroup(LOCTYPE, LocUnpacker::unpack, path + "/config/dump.loc");
        unpackConfigGroup(MESANIMTYPE, MesAnimUnpacker::unpack, path + "/config/dump.mesanim"); // todo: source?
        unpackConfigGroup(ENUMTYPE, EnumUnpacker::unpack, path + "/config/dump.enum");
        unpackConfigGroup(NPCTYPE, NpcUnpacker::unpack, path + "/config/dump.npc");
        unpackConfigGroup(OBJTYPE, ObjUnpacker::unpack, path + "/config/dump.obj");
        unpackConfigGroup(SEQTYPE, SeqUnpacker::unpack, path + "/config/dump.seq");
        unpackConfigGroup(SPOTTYPE, EffectAnimUnpacker::unpack, path + "/config/dump.spot");
        unpackConfigGroup(AREATYPE, AreaUnpacker::unpack, path + "/config/dump.area");
        unpackConfigGroup(ITEMCODETYPE, ItemCodeUnpacker::unpack, path + "/config/dump.itemcode"); // https://twitter.com/JagexAsh/status/1663851152310452225
        unpackConfigGroup(CONTROLLERTYPE, ControllerUnpacker::unpack, path + "/config/dump.controller"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(BUGTEMPLATETYPE, BugTemplateUnpacker::unpack, path + "/config/dump.bugtemplate");
        unpackConfigGroup(HITMARKTYPE, HitmarkUnpacker::unpack, path + "/config/dump.hitmark");
        unpackConfigGroup(HEADBARTYPE, HeadbarUnpacker::unpack, path + "/config/dump.headbar");
        unpackConfigGroup(STRUCTTYPE, StructUnpacker::unpack, path + "/config/dump.struct");
        unpackConfigGroup(MELTYPE, MapElementUnpacker::unpack, path + "/config/dump.mel");
        unpackConfigGroup(STRINGVECTORTYPE, StringVectorUnpacker::unpack, path + "/config/dump.stringvector"); // https://twitter.com/JagexAsh/status/1656354577057185792
        unpackConfigGroup(DBROWTYPE, DBRowUnpacker::unpack, path + "/config/dump.dbrow");
        unpackConfigGroup(DBTABLETYPE, DBTableUnpacker::unpack, path + "/config/dump.dbtable");
        unpackConfigGroup(GAMELOGEVENT, GameLogEventUnpacker::unpack, path + "/config/dump.gamelogevent"); // tfu
        unpackConfigGroup(WORLDENTITY, WorldEntityUnpacker::unpack, path + "/config/dump.worldentity");
        unpackConfigGroup(CONFIG71, Config71Unpacker::unpack, path + "/config/dump.config71");
        unpackConfigGroup(WATERTYPE, WaterUnpacker::unpack, path + "/config/dump.water");

        // defaults
        unpackDefaultsGroup(GRAPHICS, GraphicsDefaultsUnpacker::unpack, path + "/config/graphics.defaults");

        // scripts
        unpackScripts(Path.of(path + "/script"));

        // interface
        unpackInterfaces(JS5_INTERFACES, InterfaceUnpacker::unpack, Path.of(path + "/interface"));

        // materials
        unpackConfigArchive(JS5_TEXTURES, 0, TextureUnpacker::unpack, Path.of(path + "/config/dump.texture"));

        // other
        unpackBinaries(Path.of(path + "/binary"));

        // maps
//        unpackMaps(Path.of(path + "/maps"), path);
    }

    private static void loadGroupNamesScriptTrigger(Js5Archive archive, Map<Integer, String> names) throws IOException {
        var scriptByHash = new HashMap<Integer, Integer>();
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, JS5_CLIENTSCRIPTS.id, false)));
        var archiveIndexConfig = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, 2, false)));
        var maxCategory = 6000;

        for (var group : archiveIndex.groupId) {
            scriptByHash.put(archiveIndex.groupNameHash[group], group);
        }

        // set of names that result in a collision.
        // only necessary for scripts we don't actually know the name of.
        Set<Integer> collisions = new HashSet<>();
        collisions.add("5926".hashCode());       // [trigger_38,type_23]

        for (var trigger : ScriptTrigger.values()) {
            // trigger
            var key1 = String.valueOf(trigger.id - 512);

            if (scriptByHash.containsKey(key1.hashCode()) && !collisions.contains(key1.hashCode())) {
                names.put(scriptByHash.get(key1.hashCode()), "[" + trigger.name().toLowerCase(Locale.ROOT) + ",_]");
            }

            if (trigger.type != null) {
                // category trigger
                for (var category = 0; category < maxCategory; category++) {
                    var key2 = String.valueOf((-3 - category << 8) + trigger.id);

                    if (scriptByHash.containsKey(key2.hashCode()) && !collisions.contains(key2.hashCode())) {
                        names.put(scriptByHash.get(key2.hashCode()), "[" + trigger.name().toLowerCase(Locale.ROOT) + ",_" + Unpacker.format(Type.CATEGORY, category) + "]");
                    }
                }

                // type trigger
                int maxType;

                if (trigger.type == Type.NPC) {
                    maxType = archiveIndexConfig.groupMaxFileId[NPCTYPE.id];
                } else if (trigger.type == Type.LOC) {
                    maxType = archiveIndexConfig.groupMaxFileId[LOCTYPE.id];
                } else if (trigger.type == Type.OBJ) {
                    maxType = archiveIndexConfig.groupMaxFileId[OBJTYPE.id];
                } else if (trigger.type == Type.MAPELEMENT) {
                    maxType = MELTYPE.id >= archiveIndexConfig.groupMaxFileId.length ? 0 : archiveIndexConfig.groupMaxFileId[MELTYPE.id];
                } else {
                    throw new AssertionError("todo");
                }

                for (var type = 0; type < maxType; type++) {
                    var key3 = String.valueOf((type << 8) + trigger.id);

                    if (scriptByHash.containsKey(key3.hashCode()) && !collisions.contains(key3.hashCode())) {
                        names.put(scriptByHash.get(key3.hashCode()), "[" + trigger.name().toLowerCase(Locale.ROOT) + "," + Unpacker.format(trigger.type, type) + "]");
                    }
                }
            }
        }
    }

    private static void generateNames(Path path, Map<Integer, String> names) throws IOException {
        for (var name : Files.readAllLines(path)) {
            generateNames(name, names);
        }
    }

    private static void generateNames(String unhash, Map<Integer, String> names) {
        if (unhash.indexOf('#') != -1) {
            var index = unhash.indexOf('#');
            var a = unhash.substring(0, index);
            var b = unhash.substring(index + 1);

            for (var i = 0; i < 500; i++) {
                generateNames(a + i + b, names);
            }
        } else {
            names.put(unhash.hashCode(), unhash);
        }
    }

    private static void unpackMaps(Path path, String rootPath) throws IOException {
        var names = new HashSet<String>();

        for (var x = 0; x < 128; x++) {
            for (var z = 0; z < 256; z++) {
                names.add("m" + x + "_" + z);
                names.add("l" + x + "_" + z);
                names.add("e" + x + "_" + z);
                names.add("wm" + x + "_" + z);
                names.add("t" + x + "_" + z);
                names.add("w" + x + "_" + z);
            }
        }

        for (var id = -1; id < 0xffff; id++) {
            names.add("wa" + id);
        }

        var namesByHash = new HashMap<Integer, String>();

        for (var name : names) {
            if (namesByHash.containsKey(name.hashCode())) {
                System.err.println("duplicate: " + name + " " + namesByHash.get(name.hashCode()));
            }

            namesByHash.put(name.hashCode(), name);
        }

        var width = 128 * 8;
        var height = 256 * 8;
        var image = new int[width * height];

        var wa = new HashMap<Integer, List<String>>();

        iterateArchiveNamed(JS5_MAPS, (nameHash, data) -> {
            var name = namesByHash.get(nameHash);

            if (name == null) {
                System.err.println("missing name: " + nameHash);
            } else if (name.startsWith("l")) {
                var parts = name.substring(1).split("_");
                var squareX = Integer.parseInt(parts[0]);
                var squareZ = Integer.parseInt(parts[1]);
//                    System.out.println();
            } else if (name.startsWith("m")) {
                var parts = name.substring(1).split("_");
                var squareX = Integer.parseInt(parts[0]);
                var squareZ = Integer.parseInt(parts[1]);
//                    System.out.println();
            } else if (name.startsWith("e")) {
                var parts = name.substring(1).split("_");
                var squareX = Integer.parseInt(parts[0]);
                var squareZ = Integer.parseInt(parts[1]);
                var packet = new Packet(data);

                if (packet.g1() != 0) {
                    var environment = new Environment(packet);
                    packet.g4s(); // todo: osrs-only

                    try {
                        Files.writeString(path.resolve(name + ".json"), GSON.toJson(environment));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                if (packet.pos < packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

            } else if (name.startsWith("t")) {
                var parts = name.substring(1).split("_");
                var squareX = Integer.parseInt(parts[0]);
                var squareZ = Integer.parseInt(parts[1]);
                var packet = new Packet(data);

                if (packet.g1() != 0) {
                    throw new IllegalStateException("todo");
                }

                if (packet.g1() != 0) {
                    throw new IllegalStateException("todo");
                }

                if (packet.pos < packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }
            } else if (name.startsWith("w") && !name.startsWith("wm") && !name.startsWith("wa")) {
                var parts = name.substring(1).split("_");
                var squareX = Integer.parseInt(parts[0]);
                var squareZ = Integer.parseInt(parts[1]);
                var packet = new Packet(data);

                if (packet.g1() != 0) {
                    throw new IllegalStateException("todo");
                }

                if (packet.pos < packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }
            } else if (name.startsWith("wm")) {
                var parts = name.substring(2).split("_");
                var squareX = Integer.parseInt(parts[0]);
                var squareZ = Integer.parseInt(parts[1]);

                var colors = decodeWorldMapColor(data);

                for (var zoneX = 0; zoneX < 8; zoneX++) {
                    for (var zoneZ = 0; zoneZ < 8; zoneZ++) {
                        var x = 8 * squareX + zoneX;
                        var z = 8 * squareZ + zoneZ;
                        image[width * (height - 1 - z) + x] = colors[8 * zoneX + zoneZ];
                    }
                }
            } else if (name.startsWith("wa")) {
                var id = Integer.parseInt(name.substring(2));
                var lines = WorldAreaUnpacker.unpack(id, data);
                lines.add("");
                wa.put(id, lines);
            } else {
                throw new IllegalStateException("unexpected name: " + name);
            }
        });

        var waLines = wa.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .toList();

        Files.write(Path.of(rootPath + "/config/dump.worldarea"), waLines);

        var rgbData = new DataBufferInt(image, image.length);
        var raster = Raster.createPackedRaster(rgbData, width, height, width, new int[]{0xff0000, 0xff00, 0xff}, null);
        var colorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

        ImageIO.write(new BufferedImage(colorModel, raster, false, null), "png", new File(rootPath + "/areas.png"));
    }

    private static int[] decodeWorldMapColor(byte[] data) {
        var result = new int[64];
        var packet = new Packet(data);

        var index = 0;
        var target = 0;

        while (target < 64) {
            var value = packet.g3();

            if (packet.pos >= packet.arr.length) {
                target = 64; // fill remaining with `value`
            } else {
                target += packet.g1(); // fill n with `value`
            }

            while (index < target) {
                result[index++] = value;
            }
        }

        if (packet.pos < packet.arr.length) {
            throw new IllegalStateException("end of file not reached");
        }

        return result;
    }

    private static void unpackWorldMapGroup(Js5WorldMapGroup group, String result) throws IOException {
        unpackGroup(JS5_WORLDMAPDATA, group.id, MapAreaUnpacker::unpack, result);
    }

    private static void unpackScripts(Path path) throws IOException {
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, JS5_CLIENTSCRIPTS.id, false)));
        var groups = preloadGroups(JS5_CLIENTSCRIPTS.id);

        for (var group : archiveIndex.groupId) {
            var files = Js5Util.unpackGroup(archiveIndex, group, groups[group]);

            if (archiveIndex.groupNameHash[group] == "version.dat".hashCode()) {
                continue;
            }

            for (var file : files.keySet()) {
                ScriptUnpacker.load(group, files.get(file));
            }
        }

        ScriptUnpacker.decompile();

        for (var group : archiveIndex.groupId) {
            if (archiveIndex.groupNameHash[group] == "version.dat".hashCode()) {
                continue;
            }

            var lines = new ArrayList<>(ScriptUnpacker.unpack(group));
            lines.addFirst("// " + group);
            Files.write(path.resolve(Unpacker.getScriptName(group) + ".cs2"), lines);
        }
    }

    private static byte[][] preloadGroups(int id) {
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, id, false)));
        var groups = new byte[archiveIndex.groupArraySize][];

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int group : archiveIndex.groupId) {
                scope.fork(() -> {
                    groups[group] = PROVIDER.get(id, group, false);
                    return null;
                });
            }

            scope.join().throwIfFailed();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return groups;
    }

    private static void iterateArchiveNamed(Js5Archive archive, BiConsumer<Integer, byte[]> unpack) throws IOException {
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, archive.id, false)));
        var groups = preloadGroups(archive.id);

        for (var group : archiveIndex.groupId) {
            try {
                var files = Js5Util.unpackGroup(archiveIndex, group, groups[group]);

                if (files.size() == 1 && files.containsKey(0)) {
                    unpack.accept(archiveIndex.groupNameHash[group], files.get(0));
                } else {
                    throw new IllegalStateException();
                }
            } catch (UncheckedIOException ignored) {

            }
        }
    }

    private static void unpackConfigArchive(Js5Archive archive, int bits, BiFunction<Integer, byte[], List<String>> unpack, Path result) throws IOException {
        var lines = new ArrayList<String>();
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, archive.id, false)));
        var groups = preloadGroups(archive.id);

        for (var group : archiveIndex.groupId) {
            var files = Js5Util.unpackGroup(archiveIndex, group, groups[group]);

            for (var file : files.keySet()) {
                lines.addAll(unpack.apply((group << bits) + file, files.get(file)));
                lines.add("");
            }
        }

        Files.write(result, lines);
    }

    private static void unpackInterfaces(Js5Archive archive, BiFunction<Integer, byte[], List<String>> unpack, Path result) throws IOException {
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, archive.id, false)));
        var groups = preloadGroups(archive.id);

        for (var group : archiveIndex.groupId) {
            var files = Js5Util.unpackGroup(archiveIndex, group, groups[group]);
            var lines = new ArrayList<String>();
            boolean scripted = false;

            for (var file : files.keySet()) {
                byte[] data = files.get(file);
                scripted |= data[0] == -1;
                lines.addAll(unpack.apply((group << 16) | file, data));
                lines.add("");
            }

            String extension = scripted ? "if3" : "if";
            Files.write(result.resolve(Unpacker.format(Type.INTERFACE, group) + "." + extension), lines);
        }
    }

    private static void unpackDefaultsGroup(Js5DefaultsGroup group, BiFunction<Integer, byte[], List<String>> unpack, String result) throws IOException {
        unpackGroup(JS5_DEFAULTS, group.id, unpack, result);
    }

    private static void unpackConfigGroup(Js5ConfigGroup group, BiFunction<Integer, byte[], List<String>> unpack, String result) throws IOException {
        unpackGroup(JS5_CONFIG, group.id, unpack, result);
    }

    private static void unpackGroup(Js5Archive archive, int group, BiFunction<Integer, byte[], List<String>> unpack, String result) throws IOException {
        var lines = new ArrayList<String>();
        var files = loadGroupFiles(archive, group);

        if (files != null) {
            for (var file : files.keySet()) {
                if (Unpacker.DUMP_CONFIG_IDS) {
                    lines.add("// " + file);
                }
                lines.addAll(unpack.apply(file, files.get(file)));
                lines.add("");
            }

            Files.write(Path.of(result), lines);
        }
    }

    private static Map<Integer, byte[]> loadGroupFiles(Js5Archive archive, int group) {
        if (archive.id >= MASTER_INDEX.getArchiveCount() || MASTER_INDEX.getArchiveData(archive.id).getCrc() == 0) {
            return null;
        }

        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, archive.id, false)));

        if (Arrays.binarySearch(archiveIndex.groupId, group) < 0) {
            return null;
        }

        var files = Js5Util.unpackGroup(archiveIndex, group, PROVIDER.get(archive.id, group, false));
        return files;
    }

    private static void loadGroupNames(Path path, Js5Archive archive, BiConsumer<Integer, String> consumer) throws IOException {
        var unhash = new HashMap<Integer, String>();
        generateNames(path, unhash);
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, archive.id, false)));

        for (var group : archiveIndex.groupId) {
            var hash = archiveIndex.groupNameHash[group];

            if (unhash.containsKey(hash)) {
                consumer.accept(group, unhash.get(hash));
            }
        }
    }

    private static void loadDebugNames(Js5DebugNamesGroup group, Map<Integer, String> names) {
        var files = loadGroupFiles(JS5_DEBUGNAMES, group.id);

        if (files != null) {
            for (var file : files.keySet()) {
                names.put(file, new String(files.get(file), StandardCharsets.UTF_8));
            }
        }
    }

    private static void loadDebugNamesInterface() {
        var filesV2 = loadGroupFiles(JS5_DEBUGNAMES, Js5DebugNamesGroup.IFTYPES_V2.id);

        if (filesV2 != null) {
            for (var itf : filesV2.keySet()) {
                var packet = new Packet(filesV2.get(itf));
                Unpacker.INTERFACE_NAME.put(itf, packet.gjstr());

                while (true) {
                    int com = packet.g2();
                    if (com == 0xffff) break;
                    Unpacker.COMPONENT_NAME.put((itf << 16) | com, packet.gjstr());
                }
            }

            return;
        }

        var files = loadGroupFiles(JS5_DEBUGNAMES, Js5DebugNamesGroup.IFTYPES.id);

        if (files != null) {
            for (var itf : files.keySet()) {
                var packet = new Packet(files.get(itf));
                Unpacker.INTERFACE_NAME.put(itf, packet.gjstr());

                // interfaces can have more than 255 components. thankfully the data exists
                // in the buffer still, we just need to detect if there is additional data.
                for (var com = 0; packet.g1() != 0xff || (packet.pos < packet.arr.length && packet.arr[packet.pos] != 0); com++) {
                    Unpacker.COMPONENT_NAME.put((itf << 16) | com, packet.gjstr());
                }
            }
        }
    }

    private static void loadDebugNamesDBTable() {
        var files = loadGroupFiles(JS5_DEBUGNAMES, Js5DebugNamesGroup.TABLETYPES.id);

        if (files != null) {
            for (var table : files.keySet()) {
                var packet = new Packet(files.get(table));

                if (packet.gBoolean()) {
                    Unpacker.DBTABLE_NAME.put(table, packet.gjstr());
                }

                for (var column = 0; packet.g1() != 0; column++) {
                    Unpacker.DBCOLUMN_NAME.put((table << 16) | column, packet.gjstr());
                }
            }
        }
    }

    private static void unpackBinaries(Path path) throws IOException {
        Files.createDirectories(path);
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(PROVIDER.get(255, JS5_BINARY.id, false)));

        for (var group : archiveIndex.groupId) {
            var files = Js5Util.unpackGroup(archiveIndex, group, PROVIDER.get(JS5_BINARY.id, group, false));
            Files.write(path.resolve(Unpacker.BINARY_NAME.get(group)), files.get(0));
        }
    }
}
