package osrs;

import osrs.js5.Js5ArchiveIndex;
import osrs.js5.Js5Util;
import osrs.unpack.*;
import osrs.unpack.script.ScriptUnpacker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Symbols {
    private static final StandardOpenOption[] OPEN_OPTIONS = new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
    private static final Set<String> constantNames = new HashSet<>();

    public static void dumpSymbols(Path path) throws IOException {
        constantNames.clear();

        dumpType(Type.STAT, path.resolve("stat.sym"));
        dumpType(Type.LOC_SHAPE, path.resolve("locshape.sym"));
        dumpType(Type.CATEGORY, path.resolve("category.sym"));
        dumpType(Type.GRAPHIC, path.resolve("graphic.sym"));
        dumpType(Type.MAPAREA, path.resolve("wma.sym"));
        dumpType(Type.BUG_TEMPLATE, path.resolve("bugtemplate.sym"));
        dumpType(Type.MAPELEMENT, path.resolve("mapelement.sym"));
        dumpType(Type.LOC, path.resolve("loc.sym"));
        dumpType(Type.NPC, path.resolve("npc.sym"));
        dumpType(Type.OBJ, path.resolve("obj.sym"));
        dumpType(Type.INV, path.resolve("inv.sym"));
        dumpType(Type.ENUM, path.resolve("enum.sym"));
        dumpType(Type.STRUCT, path.resolve("struct.sym"));
        dumpType(Type.SEQ, path.resolve("seq.sym"));
        dumpType(Type.DBTABLE, path.resolve("dbtable.sym"));
        dumpType(Type.DBROW, path.resolve("dbrow.sym"));
        dumpType(Type.STRINGVECTOR, path.resolve("stringvector.sym"));
        dumpType(Type.VAR_PLAYER_BIT, path.resolve("varbit.sym"));
        dumpType(Type.MODEL, path.resolve("model.sym"));
        dumpType(Type.FONTMETRICS, path.resolve("fontmetrics.sym"));
        dumpType(Type.SYNTH, path.resolve("synth.sym"));
        dumpType(Type.MIDI, path.resolve("midi.sym"));
        dumpType(Type.JINGLE, path.resolve("jingle.sym"));
        dumpType(Type.WORLDENTITY, path.resolve("worldentity.sym"));

        dumpConfigWithValue(Js5ConfigGroup.PARAMTYPE, Type.PARAM, Unpacker.PARAM_TYPE, path.resolve("param.sym"));
        dumpConfigWithValue(Js5ConfigGroup.VARPLAYER, Type.VAR_PLAYER, Unpacker.VAR_PLAYER_TYPE, path.resolve("varp.sym"));
        dumpConfigWithValue(Js5ConfigGroup.VARCLIENT, Type.VAR_CLIENT, Unpacker.VAR_CLIENT_TYPE, path.resolve("varc.sym"));
        dumpConfigWithValue(Js5ConfigGroup.VAR_CLAN, Type.VAR_CLAN, Unpacker.VAR_CLAN_TYPE, path.resolve("varclan.sym"));
        dumpConfigWithValue(Js5ConfigGroup.VAR_CLAN_SETTING, Type.VAR_CLAN_SETTING, Unpacker.VAR_CLAN_SETTING_TYPE, path.resolve("varclansetting.sym"));

        dumpInterface(path);
        dumpDBColumn(path.resolve("dbcolumn.sym"));
        dumpClientScript(path.resolve("clientscript.sym"));
        dumpIfScript(path.resolve("if_script.sym"));
        dumpArchive(Js5Archive.JS5_FONTMETRICS, Type.FONTMETRICS, path.resolve("fontmetrics.sym"));

        dumpConstant(Type.INT, path.resolve("constant/int.sym"));
        dumpConstant(Type.INT_IFTYPE, path.resolve("constant/iftype.sym"));
        dumpConstant(Type.INT_BOOLEAN, path.resolve("constant/boolean.sym"));
        dumpConstant(Type.INT_CLAN, path.resolve("constant/clantype.sym"));
        dumpConstant(Type.INT_CHATFILTER, path.resolve("constant/chatfilter.sym"));
        dumpConstant(Type.INT_CHATTYPE, path.resolve("constant/chattype.sym"));
        dumpConstant(Type.INT_CLIENTTYPE, path.resolve("constant/clienttype.sym"));
        dumpConstant(Type.INT_PLATFORMTYPE, path.resolve("constant/platformtype.sym"));
        dumpConstant(Type.INT_KEY, path.resolve("constant/key.sym"));
        dumpConstant(Type.INT_SETPOSH, path.resolve("constant/setposh.sym"));
        dumpConstant(Type.INT_SETPOSV, path.resolve("constant/setposv.sym"));
        dumpConstant(Type.INT_SETSIZE, path.resolve("constant/setsize.sym"));
        dumpConstant(Type.INT_SETTEXTALIGNH, path.resolve("constant/settextalignh.sym"));
        dumpConstant(Type.INT_SETTEXTALIGNV, path.resolve("constant/settextalignv.sym"));
        dumpConstant(Type.INT_WINDOWMODE, path.resolve("constant/windowmode.sym"));
        dumpConstant(Type.INT_GAMEOPTION, path.resolve("constant/gameoption.sym"));
        dumpConstant(Type.INT_DEVICEOPTION, path.resolve("constant/deviceoption.sym"));
        dumpConstant(Type.INT_MENUENTRYTYPE, path.resolve("constant/menuentrytype.sym"));
        dumpConstant(Type.INT_BLENDMODE, path.resolve("constant/blendmode.sym"));
        dumpConstant(Type.INT_OBJOWNER, path.resolve("constant/objowner.sym"));
        dumpConstant(Type.INT_RGB, path.resolve("constant/rgb.sym"));
        dumpConstant(Type.INT_OPKIND, path.resolve("constant/opkind.sym"));
        dumpConstant(Type.INT_OPMODE, path.resolve("constant/opmode.sym"));
    }

    private static void dumpType(Type type, Path output) throws IOException {
        var unsortedSymbols = Unpacker.NAME.get(type);
        if (unsortedSymbols == null) {
            return;
        }

        var builder = new StringBuilder();
        var symbols = new TreeMap<>(unsortedSymbols);
        for (var entry : symbols.entrySet()) {
            if (entry.getKey() < 0) {
                continue;
            }

            var name = unquote(entry.getValue());
            builder.append(entry.getKey()).append('\t').append(name).append('\n');
        }

        Files.writeString(output, builder.toString(), OPEN_OPTIONS);
    }

    private static void dumpArchive(Js5Archive archive, Type type, Path output) throws IOException {
        var ids = getIds(archive);
        dump(ids, type, output);
    }

    private static void dumpConfig(Js5ConfigGroup group, Type type, Path output) throws IOException {
        dumpGroup(Js5Archive.JS5_CONFIG, group.id, type, output);
    }

    private static void dumpGroup(Js5Archive archive, int group, Type type, Path output) throws IOException {
        var ids = getIds(archive, group);
        dump(ids, type, output);
    }

    private static void dump(int[] ids, Type type, Path output) throws IOException {
        var builder = new StringBuilder();
        for (var id : ids) {
            var name = unquote(Unpacker.format(type, id, false));
            builder.append(id).append('\t').append(name).append('\n');
        }

        Files.writeString(output, builder.toString(), OPEN_OPTIONS);
    }

    private static void dumpConfigWithValue(Js5ConfigGroup group, Type primaryType, Map<Integer, Type> secondaryType, Path output) throws IOException {
        var names = Unpacker.NAME.get(primaryType);
        var ids = getIds(Js5Archive.JS5_CONFIG, group.id);

        var builder = new StringBuilder();
        for (var id : ids) {
            var name = unquote(names.get(id));
            var type = secondaryType.get(id);
            if (type == null) {
                type = Type.INT;
            } else if (type.alias != null) {
                type = type.alias;
            }

            builder.append(id).append('\t').append(name).append('\t').append(type.name).append('\n');
        }
        Files.writeString(output, builder.toString(), OPEN_OPTIONS);
    }

    private static void dumpInterface(Path outputRoot) throws IOException {
        // 'interface' is last since it is the fallback
        var interfaceTypes = List.of(Type.TOPLEVELINTERFACE, Type.OVERLAYINTERFACE, Type.CLIENTINTERFACE, Type.INTERFACE);
        var interfaceIds = getIds(Js5Archive.JS5_INTERFACES);

        var componentBuilder = new StringBuilder();
        var interfaceBuilders = new HashMap<Type, StringBuilder>();
        for (var interfaceType : interfaceTypes) {
            interfaceBuilders.put(interfaceType, new StringBuilder());
        }

        for (var interfaceId : interfaceIds) {
            Type interfaceType = Type.INTERFACE;
            for (var possibleType : interfaceTypes) {
                Map<Integer, String> symbols = Unpacker.NAME.get(possibleType);
                if (symbols != null && symbols.containsKey(interfaceId)) {
                    interfaceType = possibleType;
                    break;
                }
            }

            var name = unquote(Unpacker.format(interfaceType, interfaceId, false));
            var interfaceBuilder = interfaceBuilders.get(interfaceType);
            interfaceBuilder.append(interfaceId).append('\t').append(name).append('\n');

            var componentIds = getIds(Js5Archive.JS5_INTERFACES, interfaceId);
            for (var componentId : componentIds) {
                var combinedId = (interfaceId << 16) | componentId;
                var componentName = Unpacker.format(Type.COMPONENT, combinedId, false);
                componentBuilder.append(combinedId).append('\t').append(componentName).append('\n');
            }
        }

        Files.writeString(outputRoot.resolve("component.sym"), componentBuilder.toString(), OPEN_OPTIONS);
        for (var interfaceType : interfaceTypes) {
            var builder = interfaceBuilders.get(interfaceType);
            if (builder.isEmpty()) {
                continue;
            }
            Files.writeString(outputRoot.resolve(interfaceType.name + ".sym"), builder.toString(), OPEN_OPTIONS);
        }
    }

    private static void dumpDBColumn(Path output) throws IOException {
        var tables = new TreeMap<>(Unpacker.DBCOLUMN_TYPE);

        var builder = new StringBuilder();
        for (var entry : tables.entrySet()) {
            var tableId = entry.getKey();
            var columns = new TreeMap<>(entry.getValue());

            for (var columnEntry : columns.entrySet()) {
                var columnId = columnEntry.getKey();
                var types = columnEntry.getValue();
                var basePackedId = tableId << 12 | columnId << 4;

                var name = Unpacker.format(Type.DBCOLUMN, basePackedId, false);

                var typesJoined = types.stream().map(t -> t.name).collect(Collectors.joining(","));
                builder.append(basePackedId).append('\t').append(name).append('\t').append(typesJoined).append('\n');

                for (int i = 0; i < Math.min(types.size(), 14); i++) {
                    var typeAtIndex = types.get(i);
                    var columnTupleId = basePackedId | (i + 1);
                    builder.append(columnTupleId).append('\t').append(name).append(":").append(i).append('\t').append(typeAtIndex.name).append('\n');
                }
            }
        }
        Files.writeString(output, builder.toString(), OPEN_OPTIONS);
    }

    private static void dumpClientScript(Path output) throws IOException {
        var builder = new StringBuilder();
        var sorted = new TreeMap<>(ScriptUnpacker.SCRIPTS);
        for (var entry : sorted.entrySet()) {
            var name = Unpacker.getScriptName(entry.getKey());
            builder.append(entry.getKey()).append('\t').append(name).append('\n');
        }
        Files.writeString(output, builder.toString(), OPEN_OPTIONS);
    }

    private static void dumpIfScript(Path output) throws IOException {
        var builder = new StringBuilder();
        var sorted = new TreeMap<>(Unpacker.IF_SCRIPT_TYPE);
        for (var entry : sorted.entrySet()) {
            var name = Unpacker.format(Type.IF_SCRIPT, entry.getKey());
            var types = entry.getValue();
            var typesJoined = types.stream().map(t -> ScriptUnpacker.chooseDisplayType(t).name).collect(Collectors.joining(","));
            builder.append(entry.getKey()).append('\t').append(name).append('\t').append(typesJoined).append('\n');
        }
        Files.writeString(output, builder.toString(), OPEN_OPTIONS);
    }

    private static void dumpConstant(Type type, Path output) throws IOException {
        var unsortedSymbols = Unpacker.NAME.get(type);
        if (unsortedSymbols == null) {
            return;
        }

        var builder = new StringBuilder();
        var symbols = new TreeMap<>(unsortedSymbols);
        for (var entry : symbols.entrySet()) {
            var name = entry.getValue();
            if (!name.startsWith("^")) {
                continue;
            }

            name = name.substring(1);
            if (constantNames.contains(name)) {
                // some types have the same name across them, hopefully the same values...
                continue;
            }

            builder.append(name).append('\t').append(entry.getKey()).append('\n');
            constantNames.add(name);
        }

        Files.createDirectories(output.getParent());
        Files.writeString(output, builder.toString(), OPEN_OPTIONS);
    }

    private static int[] getIds(Js5Archive archive) {
        if (archive.id >= Unpack.MASTER_INDEX.getArchiveCount()) {
            // archive doesn't exist
            return new int[0];
        }

        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Unpack.PROVIDER.get(255, archive.id, false)));
        return archiveIndex.groupId;
    }

    private static int[] getIds(Js5Archive archive, int group) {
        if (archive.id >= Unpack.MASTER_INDEX.getArchiveCount()) {
            // archive doesn't exist
            return new int[0];
        }

        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(Unpack.PROVIDER.get(255, archive.id, false)));
        if (group >= archiveIndex.groupFileIds.length) {
            // group doesn't exist
            return new int[0];
        }

        if (archiveIndex.groupFileIds[group] == null) {
            // this is true when there are no gaps in the file ids, so generate an array of all ids
            int[] ids = new int[archiveIndex.groupMaxFileId[group]];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = i;
            }
            return ids;
        } else {
            return archiveIndex.groupFileIds[group];
        }
    }

    private static String unquote(String name) {
        if (name.startsWith("\"") && name.endsWith("\"")) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }
}
