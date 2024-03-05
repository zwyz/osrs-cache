package osrs.js5;

public final class Js5MasterIndexArchiveData {
    private final int crc;
    private final int version;

    public Js5MasterIndexArchiveData(int crc, int version) {
        this.crc = crc;
        this.version = version;
    }

    public int getCrc() {
        return crc;
    }

    public int getVersion() {
        return version;
    }
}
