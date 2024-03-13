package osrs;

import osrs.js5.Js5ArchiveIndex;
import osrs.js5.Js5Util;
import osrs.unpack.*;
import osrs.unpack.config.*;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static osrs.unpack.Js5Archive.*;
import static osrs.unpack.Js5ConfigGroup.*;
import static osrs.unpack.Js5DefaultsGroup.GRAPHICS;
import static osrs.unpack.Js5WorldMapGroup.DETAILS;

// todo: clean this up
public class Unpack {
    public static final int VERSION = 220;
    private static final Path BASE_PATH = Path.of(System.getProperty("user.home") + "/.rscache/osrs");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("unpacked"));
        Files.createDirectories(Path.of("unpacked/config"));
        Files.createDirectories(Path.of("unpacked/script"));
        Files.createDirectories(Path.of("unpacked/interface"));

        // load names
        loadGroupNames(Path.of("data/names/scripts.txt"), JS5_CLIENTSCRIPTS, Unpacker.SCRIPT_NAMES::put);
        loadGroupNames(Path.of("data/names/graphics.txt"), JS5_SPRITES, Unpacker.GRAPHIC_NAMES::put);

        // things stuff depends on
        unpackConfigGroup(VARBIT, VarPlayerBitUnpacker::unpack, "unpacked/config/dump.varbit");
        unpackConfigGroup(VARPLAYER, VarPlayerUnpacker::unpack, "unpacked/config/dump.varp");
        unpackConfigGroup(VARCLIENT, VarClientUnpacker::unpack, "unpacked/config/dump.varc");
        unpackConfigGroup(VARCLIENTSTR, VarClientStringUnpacker::unpack, "unpacked/config/dump.varcstr");
        unpackConfigGroup(VAROBJ, VarObjUnpacker::unpack, "unpacked/config/dump.varobj"); // increased with treasure trail expansion
        unpackConfigGroup(VARSHARED, VarSharedUnpacker::unpack, "unpacked/config/dump.vars"); // increased with poh board https://twitter.com/JagexAsh/status/1610606943726456834
        unpackConfigGroup(VARSHAREDSTR, VarSharedStringUnpacker::unpack, "unpacked/config/dump.varsstr");
        unpackConfigGroup(VARNPC, VarNpcUnpacker::unpack, "unpacked/config/dump.varn");
        unpackConfigGroup(VARNPCBIT, VarNpcBitUnpacker::unpack, "unpacked/config/dump.varnbit");
        unpackConfigGroup(VARGLOBAL, VarGlobalUnpacker::unpack, "unpacked/config/dump.varg"); // matches leaderboards
        unpackConfigGroup(VARCONTROLLER, VarControllerUnpacker::unpack, "unpacked/config/dump.varcon"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(VARCONTROLLERBIT, VarControllerBitUnpacker::unpack, "unpacked/config/dump.varconbit"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(VAR_CLAN, VarClanUnpacker::unpack, "unpacked/config/dump.varclan");
        unpackConfigGroup(VAR_CLAN_SETTING, VarClanSettingUnpacker::unpack, "unpacked/config/dump.varclansetting");
        unpackConfigGroup(PARAMTYPE, ParamUnpacker::unpack, "unpacked/config/dump.param");

        // regular configs
        unpackConfigGroup(FLUTYPE, FloorUnderlayUnpacker::unpack, "unpacked/config/dump.flu");
        unpackConfigGroup(HUNTTYPE, HuntUnpacker::unpack, "unpacked/config/dump.hunt"); // https://youtu.be/5pvoMQUCla4?si=-BvlpFgRrAo0UrXb&t=4070
        unpackConfigGroup(IDKTYPE, IDKUnpacker::unpack, "unpacked/config/dump.idk");
        unpackConfigGroup(FLOTYPE, FloorOverlayUnpacker::unpack, "unpacked/config/dump.flo");
        unpackConfigGroup(INVTYPE, InvUnpacker::unpack, "unpacked/config/dump.inv");
        unpackConfigGroup(LOCTYPE, LocUnpacker::unpack, "unpacked/config/dump.loc");
        unpackConfigGroup(MESANIMTYPE, MesAnimUnpacker::unpack, "unpacked/config/dump.mesanim"); // todo: source?
        unpackConfigGroup(ENUMTYPE, EnumUnpacker::unpack, "unpacked/config/dump.enum");
        unpackConfigGroup(NPCTYPE, NpcUnpacker::unpack, "unpacked/config/dump.npc");
        unpackConfigGroup(OBJTYPE, ObjUnpacker::unpack, "unpacked/config/dump.obj");
        unpackConfigGroup(SEQTYPE, SeqUnpacker::unpack, "unpacked/config/dump.seq");
        unpackConfigGroup(SPOTTYPE, EffectAnimUnpacker::unpack, "unpacked/config/dump.spot");
        unpackConfigGroup(AREATYPE, AreaUnpacker::unpack, "unpacked/config/dump.area");
        unpackConfigGroup(ITEMCODETYPE, ItemCodeUnpacker::unpack, "unpacked/config/dump.itemcode"); // https://twitter.com/JagexAsh/status/1663851152310452225
        unpackConfigGroup(CONTROLLERTYPE, ControllerUnpacker::unpack, "unpacked/config/dump.controller"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(UNKNOWN_31, Config31Unpacker::unpack, "unpacked/config/dump.unknown31");
        unpackConfigGroup(HITMARKTYPE, HitmarkUnpacker::unpack, "unpacked/config/dump.hitmark");
        unpackConfigGroup(HEADBARTYPE, HeadbarUnpacker::unpack, "unpacked/config/dump.headbar");
        unpackConfigGroup(STRUCTTYPE, StructUnpacker::unpack, "unpacked/config/dump.struct");
        unpackConfigGroup(MELTYPE, MapElementUnpacker::unpack, "unpacked/config/dump.mel");
        unpackConfigGroup(STRINGVECTORTYPE, StringVectorUnpacker::unpack, "unpacked/config/dump.stringvector"); // https://twitter.com/JagexAsh/status/1656354577057185792
        unpackConfigGroup(DBROWTYPE, DBRowUnpacker::unpack, "unpacked/config/dump.dbrow");
        unpackConfigGroup(DBTABLETYPE, DBTableUnpacker::unpack, "unpacked/config/dump.dbtable");
        unpackConfigGroup(GAMELOGEVENT, GameLogEventUnpacker::unpack, "unpacked/config/dump.gamelogevent"); // tfu

        // world map
        unpackWorldMapGroup(DETAILS, "unpacked/config/dump.wma");

        // defaults
        unpackDefaultsGroup(GRAPHICS, GraphicsDefaultsUnpacker::unpack, "unpacked/config/graphics.defaults");

        // scripts
        unpackScripts(Path.of("unpacked/script"));

        // interface
        unpackInterfaces(JS5_INTERFACES, InterfaceUnpacker::unpack, Path.of("unpacked/interface"));

        // materials
        unpackConfigArchive(JS5_TEXTURES, 0, TextureUnpacker::unpack, Path.of("unpacked/config/dump.texture"));

        // other
        unpackArchive(10, Path.of("unpacked/binary"), ".dat");

        // maps
        unpackMaps();
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

    private static void unpackMaps() throws IOException {
        var names = new HashSet<String>();

        for (var x = 0; x < 128; x++) {
            for (var z = 0; z < 256; z++) {
                names.add("m" + x + "_" + z);
                names.add("l" + x + "_" + z);
                names.add("e" + x + "_" + z);
                names.add("wm" + x + "_" + z);
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
                var lines = new ArrayList<String>();
                lines.add("coloura=" + packet.g1() + "," + packet.g1() + "," + packet.g1());
                lines.add("unknown4a=" + packet.g1());
                lines.add("unknown5=" + packet.g1());

                if (packet.g1() == 0) {
                    lines.add("colourb=" + packet.g1() + "," + packet.g1() + "," + packet.g1());
                    lines.add("unknown4b=" + packet.g1());
                }

                var count = packet.gSmart2or4s();

                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

//                    System.out.println(lines);
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

        Files.write(Path.of("unpacked/config/dump.worldarea"), waLines);

        var rgbData = new DataBufferInt(image, image.length);
        var raster = Raster.createPackedRaster(rgbData, width, height, width, new int[]{0xff0000, 0xff00, 0xff}, null);
        var colorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

        ImageIO.write(new BufferedImage(colorModel, raster, false, null), "png", new File("unpacked/areas.png"));
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

        return result;
    }

    private static void unpackWorldMapGroup(Js5WorldMapGroup group, String result) throws IOException {
        unpackGroup(JS5_WORLDMAPDATA, group.id, MapAreaUnpacker::unpack, result);
    }

    private static void unpackScripts(Path path) throws IOException {
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Files.readAllBytes(BASE_PATH.resolve("255/" + JS5_CLIENTSCRIPTS.id + ".dat"))));

        for (var group : archiveIndex.groupId) {
            var files = Js5Util.unpackGroup(archiveIndex, group, Files.readAllBytes(BASE_PATH.resolve(JS5_CLIENTSCRIPTS.id + "/" + group + ".dat")));

            if (archiveIndex.groupNameHash[group] == "version.dat".hashCode()) {
                continue;
            }

            for (var file : files.keySet()) {
                ScriptUnpacker.load(group, files.get(file));
            }
        }

        ScriptUnpacker.decompile();

        for (var group : archiveIndex.groupId) {
            var lines = new ArrayList<>(ScriptUnpacker.unpack(group));
            lines.addFirst("// " + group);
            Files.write(path.resolve(Unpacker.getScriptName(group) + ".cs2"), lines);
        }
    }

    private static void iterateArchiveNamed(Js5Archive archive, BiConsumer<Integer, byte[]> unpack) throws IOException {
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Files.readAllBytes(BASE_PATH.resolve("255/" + archive.id + ".dat"))));

        var groupId = archiveIndex.groupId;

        for (var i = 0; i < groupId.length; i++) {
            var group = groupId[i];
            try {
                var files = Js5Util.unpackGroup(archiveIndex, group, Files.readAllBytes(BASE_PATH.resolve(archive.id + "/" + group + ".dat")));

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
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Files.readAllBytes(BASE_PATH.resolve("255/" + archive.id + ".dat"))));

        for (var group : archiveIndex.groupId) {
            var files = Js5Util.unpackGroup(archiveIndex, group, Files.readAllBytes(BASE_PATH.resolve(archive.id + "/" + group + ".dat")));

            for (var file : files.keySet()) {
                lines.addAll(unpack.apply((group << bits) + file, files.get(file)));
                lines.add("");
            }
        }

        Files.write(result, lines);
    }

    private static void unpackInterfaces(Js5Archive archive, BiFunction<Integer, byte[], List<String>> unpack, Path result) throws IOException {
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Files.readAllBytes(BASE_PATH.resolve("255/" + archive.id + ".dat"))));

        for (var group : archiveIndex.groupId) {
            var files = Js5Util.unpackGroup(archiveIndex, group, Files.readAllBytes(BASE_PATH.resolve(archive.id + "/" + group + ".dat")));
            var lines = new ArrayList<String>();

            for (var file : files.keySet()) {
                lines.addAll(unpack.apply(file, files.get(file)));
                lines.add("");
            }

            Files.write(result.resolve(Unpacker.format(Type.INTERFACE, group) + ".if3"), lines);
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

        if (!Files.exists(BASE_PATH.resolve(archive.id + "/" + archive.id + ".dat"))) {
            return; // empty groups don't get packed
        }

        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Files.readAllBytes(BASE_PATH.resolve("255/" + archive.id + ".dat"))));

        if (!Files.exists(BASE_PATH.resolve(archive.id + "/" + group + ".dat"))) {
            return; // empty groups don't get packed
        }

        var files = Js5Util.unpackGroup(archiveIndex, group, Files.readAllBytes(BASE_PATH.resolve(archive.id + "/" + group + ".dat")));

        for (var file : files.keySet()) {
            lines.addAll(unpack.apply(file, files.get(file)));
            lines.add("");
        }

        Files.write(Path.of(result), lines);
    }

    private static void loadGroupNames(Path path, Js5Archive archive, BiConsumer<Integer, String> consumer) throws IOException {
        var unhash = new HashMap<Integer, String>();
        generateNames(path, unhash);
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Files.readAllBytes(BASE_PATH.resolve("255/" + archive.id + ".dat"))));

        for (var group : archiveIndex.groupId) {
            var hash = archiveIndex.groupNameHash[group];

            if (unhash.containsKey(hash)) {
                consumer.accept(group, unhash.get(hash));
            }
        }
    }

    private static void unpackArchive(int archive, Path path, String extension) throws IOException {
        if (!Files.exists(BASE_PATH.resolve("255/" + archive + ".dat"))) {
            return;
        }

        Files.createDirectories(path);
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Files.readAllBytes(BASE_PATH.resolve("255/" + archive + ".dat"))));

        for (var group : archiveIndex.groupId) {
            var files = Js5Util.unpackGroup(archiveIndex, group, Files.readAllBytes(BASE_PATH.resolve(archive + "/" + group + ".dat")));

            if (files.size() == 1 && files.containsKey(0)) {
                Files.write(path.resolve(group + extension), files.get(0));
            } else {
                var groupDirectory = path.resolve(String.valueOf(group));
                Files.createDirectories(groupDirectory);

                for (var file : files.keySet()) {
                    Files.write(groupDirectory.resolve(file + extension), files.get(file));
                }
            }
        }
    }

}
