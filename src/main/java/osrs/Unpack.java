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
    private static final Path BASE_PATH = Path.of(System.getProperty("user.home") + "/.rscache/osrs");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("unpacked"));

        // things stuff depends on
        unpackConfigGroup(VARBIT, VarPlayerBitUnpacker::unpack, "unpacked/dump.varbit");
        unpackConfigGroup(VARPLAYER, VarPlayerUnpacker::unpack, "unpacked/dump.varp");
        unpackConfigGroup(VARCLIENT, VarClientUnpacker::unpack, "unpacked/dump.varc");
        unpackConfigGroup(VAROBJ, VarObjUnpacker::unpack, "unpacked/dump.varobj"); // increased with treasure trail expansion
        unpackConfigGroup(VARSHARED, VarSharedUnpacker::unpack, "unpacked/dump.vars"); // increased with poh board https://twitter.com/JagexAsh/status/1610606943726456834
        unpackConfigGroup(VARNPC, VarNpcUnpacker::unpack, "unpacked/dump.varn");
        unpackConfigGroup(VARNPCBIT, VarNpcBitUnpacker::unpack, "unpacked/dump.varnbit");
        unpackConfigGroup(VARGLOBAL, VarGlobalUnpacker::unpack, "unpacked/dump.varg"); // matches leaderboards
        unpackConfigGroup(VARCONTROLLER, VarControllerUnpacker::unpack, "unpacked/dump.varcon"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(VARCONTROLLERBIT, VarControllerBitUnpacker::unpack, "unpacked/dump.varconbit"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(VAR_CLAN, VarClanUnpacker::unpack, "unpacked/dump.varclan");
        unpackConfigGroup(VAR_CLAN_SETTING, VarClanSettingUnpacker::unpack, "unpacked/dump.varclansetting");
        unpackConfigGroup(PARAMTYPE, ParamUnpacker::unpack, "unpacked/dump.param");

        // regular configs
        unpackConfigGroup(FLUTYPE, FloorUnderlayUnpacker::unpack, "unpacked/dump.flu");
        unpackConfigGroup(HUNTTYPE, HuntUnpacker::unpack, "unpacked/dump.hunt"); // https://youtu.be/5pvoMQUCla4?si=-BvlpFgRrAo0UrXb&t=4070
        unpackConfigGroup(IDKTYPE, IDKUnpacker::unpack, "unpacked/dump.idk");
        unpackConfigGroup(FLOTYPE, FloorOverlayUnpacker::unpack, "unpacked/dump.flo");
        unpackConfigGroup(INVTYPE, InvUnpacker::unpack, "unpacked/dump.inv");
        unpackConfigGroup(LOCTYPE, LocUnpacker::unpack, "unpacked/dump.loc");
        unpackConfigGroup(MESANIMTYPE, MesAnimUnpacker::unpack, "unpacked/dump.mesanim"); // todo: source?
        unpackConfigGroup(ENUMTYPE, EnumUnpacker::unpack, "unpacked/dump.enum");
        unpackConfigGroup(NPCTYPE, NpcUnpacker::unpack, "unpacked/dump.npc");
        unpackConfigGroup(OBJTYPE, ObjUnpacker::unpack, "unpacked/dump.obj");
        unpackConfigGroup(SEQTYPE, SeqUnpacker::unpack, "unpacked/dump.seq");
        unpackConfigGroup(SPOTTYPE, EffectAnimUnpacker::unpack, "unpacked/dump.spot");
        unpackConfigGroup(AREATYPE, AreaUnpacker::unpack, "unpacked/dump.area");
        unpackConfigGroup(ITEMCODETYPE, ItemCodeUnpacker::unpack, "unpacked/dump.itemcode"); // https://twitter.com/JagexAsh/status/1663851152310452225
        unpackConfigGroup(CONTROLLERTYPE, ControllerUnpacker::unpack, "unpacked/dump.controller"); // https://twitter.com/JagexAsh/status/1600154097742553088
        unpackConfigGroup(UNKNOWN_31, Config31Unpacker::unpack, "unpacked/dump.unknown31");
        unpackConfigGroup(HITMARKTYPE, HitmarkUnpacker::unpack, "unpacked/dump.hitmark");
        unpackConfigGroup(HEADBARTYPE, HeadbarUnpacker::unpack, "unpacked/dump.headbar");
        unpackConfigGroup(STRUCTTYPE, StructUnpacker::unpack, "unpacked/dump.struct");
        unpackConfigGroup(MELTYPE, MapElementUnpacker::unpack, "unpacked/dump.mel");
        unpackConfigGroup(STRINGVECTORTYPE, StringVectorUnpacker::unpack, "unpacked/dump.stringvector"); // https://twitter.com/JagexAsh/status/1656354577057185792
        unpackConfigGroup(DBROWTYPE, DBRowUnpacker::unpack, "unpacked/dump.dbrow");
        unpackConfigGroup(DBTABLETYPE, DBTableUnpacker::unpack, "unpacked/dump.dbtable");
        unpackConfigGroup(GAMELOGEVENT, GameLogEventUnpacker::unpack, "unpacked/dump.gamelogevent"); // tfu

        // world map
        unpackWorldMapGroup(DETAILS, "unpacked/dump.wma");

        // defaults
        unpackDefaultsGroup(GRAPHICS, GraphicsDefaultsUnpacker::unpack, "unpacked/graphics.defaults");

        // scripts
        unpackScripts(Path.of("unpacked/dump.cs2"));

        // interface
        unpackConfigArchive(JS5_INTERFACES, 16, InterfaceUnpacker::unpack, Path.of("unpacked/dump.if3"));

        // materials
        unpackConfigArchive(JS5_MATERIALS, 0, MaterialUnpacker::unpack, Path.of("unpacked/dump.material"));

        // maps
        unpackMaps();
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

        Files.write(Path.of("unpacked/dump.worldarea"), waLines);

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

        var lines = new ArrayList<String>();

        for (var group : archiveIndex.groupId) {
            lines.addAll(ScriptUnpacker.unpack(group));
            lines.add("");
        }

        Files.write(path, lines);
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

    private static void unpackDefaultsGroup(Js5DefaultsGroup group, BiFunction<Integer, byte[], List<String>> unpack, String result) throws IOException {
        unpackGroup(JS5_DEFAULTS, group.id, unpack, result);
    }

    private static void unpackConfigGroup(Js5ConfigGroup group, BiFunction<Integer, byte[], List<String>> unpack, String result) throws IOException {
        unpackGroup(JS5_CONFIG, group.id, unpack, result);
    }

    private static void unpackGroup(Js5Archive archive, int group, BiFunction<Integer, byte[], List<String>> unpack, String result) throws IOException {
        var lines = new ArrayList<String>();
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Files.readAllBytes(BASE_PATH.resolve("255/" + archive.id + ".dat"))));

        var files = Js5Util.unpackGroup(archiveIndex, group, Files.readAllBytes(BASE_PATH.resolve(archive.id + "/" + group + ".dat")));

        for (var file : files.keySet()) {
            lines.addAll(unpack.apply(file, files.get(file)));
            lines.add("");
        }

        Files.write(Path.of(result), lines);
    }
}
