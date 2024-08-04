package osrs.js5;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.tukaani.xz.LZMAInputStream;
import osrs.util.Packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;

public class Js5Util {
    public static HashMap<Integer, byte[]> unpackGroup(Js5ArchiveIndex index, int group, byte[] data) {
        return unpackGroup(index, group, data, null);
    }

    public static HashMap<Integer, byte[]> unpackGroup(Js5ArchiveIndex index, int group, byte[] data, int[] key) {
        if (key != null && (key[0] != 0 || key[1] != 0 || key[2] != 0 || key[3] != 0)) {
            var packet = new Packet(data);
            packet.tinyKeyDecrypt(key, 5, packet.arr.length);
        }

        var groupData = decompress(data);
        var fileCount = index.groupSize[group];
        var fileIds = index.groupFileIds[group];

        if (fileCount == 1) {
            var result = new HashMap<Integer, byte[]>();
            result.put(0, groupData);
            return result;
        }

        var somethingCount = groupData[groupData.length - 1] & 255;
        var fileSizesPosition = groupData.length - 1 - somethingCount * fileCount * 4;
        var buffer = ByteBuffer.wrap(groupData);
        var filePositions = new int[fileCount];

        buffer.position(fileSizesPosition);

        for (var i = 0; i < somethingCount; ++i) {
            var fileStart = 0;

            for (var j = 0; j < fileCount; ++j) {
                fileStart += buffer.getInt();
                filePositions[j] += fileStart;
            }
        }

        var files = new byte[fileCount][];

        for (var i = 0; i < fileCount; ++i) {
            files[i] = new byte[filePositions[i]];
            filePositions[i] = 0;
        }

        buffer.position(fileSizesPosition);
        var fileStart = 0;

        for (var i = 0; i < somethingCount; i++) {
            var length = 0;

            for (var j = 0; j < fileCount; j++) {
                length += buffer.getInt();
                System.arraycopy(groupData, fileStart, files[j], filePositions[j], length);
                filePositions[j] += length;
                fileStart += length;
            }
        }

        var fileMap = new LinkedHashMap<Integer, byte[]>();

        for (var i = 0; i < fileCount; ++i) {
            fileMap.put(fileIds == null ? i : fileIds[i], files[i]);
        }

        return fileMap;
    }

    public static byte[] decompress(byte[] compressed) {
        try {
            var buffer = ByteBuffer.wrap(compressed);
            var compressionType = buffer.get();
            var compressedSize = buffer.getInt();

            if (compressionType == 0) {
                return Arrays.copyOfRange(compressed, 5, 5 + compressedSize);
            }

            if (compressionType == 1) {
                var uncompressedSize = buffer.getInt();
                var data = new byte[4 + buffer.limit() - buffer.position()];
                data[0] = 'B';
                data[1] = 'Z';
                data[2] = 'h';
                data[3] = '1';
                System.arraycopy(buffer.array(), buffer.position(), data, 4, buffer.limit() - buffer.position());
                return new BZip2CompressorInputStream(new ByteArrayInputStream(data)).readNBytes(uncompressedSize);
            }

            if (compressionType == 2) {
                var uncompressedSize = buffer.getInt();
                return new GZIPInputStream(new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit() - buffer.position())).readNBytes(uncompressedSize);
            }

            if (compressionType == 3) {
                var uncompressedSize = buffer.getInt();
                var properties = buffer.get();
                var dictionarySize = Integer.reverseBytes(buffer.getInt());
                return new LZMAInputStream(new ByteArrayInputStream(compressed, buffer.position(), buffer.remaining()), uncompressedSize, properties, dictionarySize).readAllBytes();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        throw new IllegalStateException("unsupported compression format");
    }
}
