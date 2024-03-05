package osrs.js5;

import osrs.util.Packet;

public class Js5MasterIndex {
    public static final int ARCHIVE_COUNT = 22;
    private final Js5MasterIndexArchiveData[] archiveInfo;

    public Js5MasterIndex(byte[] data) {
        var packet = new Packet(data);

        archiveInfo = new Js5MasterIndexArchiveData[ARCHIVE_COUNT];

        for (var i = 0; i < ARCHIVE_COUNT; i++) {
            var crc = packet.g4s();
            var version = packet.g4s();
            archiveInfo[i] = new Js5MasterIndexArchiveData(crc, version);
        }

        if (packet.pos != packet.arr.length) {
            throw new IllegalStateException("end of file not reached");
        }
    }

    public int getArchiveCount() {
        return archiveInfo.length;
    }

    public Js5MasterIndexArchiveData getArchiveData(int archive) {
        return archiveInfo[archive];
    }
}