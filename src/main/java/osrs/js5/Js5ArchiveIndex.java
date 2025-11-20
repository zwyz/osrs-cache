package osrs.js5;

import osrs.util.Packet;

public class Js5ArchiveIndex {
    public int version;
    public int groupCount;
    public int[] groupId;
    public int[] groupNameHash;
    public int groupArraySize;
    public int[] groupChecksum;
    public int[] groupUncompressedChecksum;
    public byte[][] groupDigest;
    public int[] groupVersion;
    public int[] groupLength;
    public int[] groupUncompressedLength;
    public int[] groupSize;
    public int[][] groupFileIds;
    public int[][] groupFileNames;
    public int[] groupMaxFileId;

    public Js5ArchiveIndex(byte[] data) {
        var packet = new Packet(data);
        var protocol = packet.g1();

        if (protocol < 5 || protocol > 7) {
            throw new RuntimeException();
        }

        if (protocol >= 6) {
            version = packet.g4s();
        } else {
            version = 0;
        }

        var flags = packet.g1();
        var hasNames = (flags & 1) != 0;
        var hasDigests = (flags & 2) != 0;
        var hasLengths = (flags & 4) != 0;
        var hasUncompressedChecksums = (flags & 8) != 0;

        if (protocol >= 7) {
            groupCount = packet.gSmart2or4null();
        } else {
            groupCount = packet.g2();
        }

        var var9 = 0;
        var maxGroupId = -1;
        groupId = new int[groupCount];

        if (protocol >= 7) {
            for (var i = 0; i < groupCount; ++i) {
                groupId[i] = var9 += packet.gSmart2or4null();
                if (groupId[i] > maxGroupId) {
                    maxGroupId = groupId[i];
                }
            }
        } else {
            for (var i = 0; i < groupCount; ++i) {
                groupId[i] = var9 += packet.g2();
                if (groupId[i] > maxGroupId) {
                    maxGroupId = groupId[i];
                }
            }
        }

        groupArraySize = maxGroupId + 1;

        groupChecksum = new int[groupArraySize];

        if (hasUncompressedChecksums) {
            groupUncompressedChecksum = new int[groupArraySize];
        }

        if (hasDigests) {
            groupDigest = new byte[groupArraySize][];
        }

        groupVersion = new int[groupArraySize];
        groupSize = new int[groupArraySize];
        groupFileIds = new int[groupArraySize][];
        groupMaxFileId = new int[groupArraySize];

        if (hasNames) {
            groupNameHash = new int[groupArraySize];

            for (var i = 0; i < groupArraySize; ++i) {
                groupNameHash[i] = -1;
            }

            for (var i = 0; i < groupCount; ++i) {
                groupNameHash[groupId[i]] = packet.g4s();
            }
        }

        for (var i = 0; i < groupCount; ++i) {
            groupChecksum[groupId[i]] = packet.g4s();
        }

        if (hasUncompressedChecksums) {
            for (var i = 0; i < groupCount; ++i) {
                groupUncompressedChecksum[i] = packet.g4s();
            }
        }

        if (hasDigests) {
            for (var i = 0; i < groupCount; ++i) {
                groupDigest[groupId[i]] = packet.gdata(64);
            }
        }

        if (hasLengths) {
            groupLength = new int[groupArraySize];
            groupUncompressedLength = new int[groupArraySize];

            for (var i = 0; i < groupCount; ++i) {
                groupLength[groupId[i]] = packet.g4s();
                groupUncompressedLength[groupId[i]] = packet.g4s();
            }
        }

        for (var i = 0; i < groupCount; ++i) {
            groupVersion[groupId[i]] = packet.g4s();
        }

        if (protocol >= 7) {
            for (var i = 0; i < groupCount; ++i) {
                groupSize[groupId[i]] = packet.gSmart2or4null();
            }

            for (var i = 0; i < groupCount; ++i) {
                var group = groupId[i];
                var size = groupSize[group];
                var var25 = 0;
                var var26 = -1;
                groupFileIds[group] = new int[size];

                for (var j = 0; j < size; ++j) {
                    var var28 = groupFileIds[group][j] = var25 += packet.gSmart2or4null();

                    if (var28 > var26) {
                        var26 = var28;
                    }
                }

                groupMaxFileId[group] = var26 + 1;
                if (var26 + 1 == size) {
                    groupFileIds[group] = null;
                }
            }
        } else {
            for (var var29 = 0; var29 < groupCount; ++var29) {
                groupSize[groupId[var29]] = packet.g2();
            }

            for (var var30 = 0; var30 < groupCount; ++var30) {
                var var31 = groupId[var30];
                var var32 = groupSize[var31];
                var var33 = 0;
                var var34 = -1;
                groupFileIds[var31] = new int[var32];

                for (var var35 = 0; var35 < var32; ++var35) {
                    var var36 = groupFileIds[var31][var35] = var33 += packet.g2();
                    if (var36 > var34) {
                        var34 = var36;
                    }
                }

                groupMaxFileId[var31] = var34 + 1;
                if (var34 + 1 == var32) {
                    groupFileIds[var31] = null;
                }
            }
        }

        if (hasNames) {
            groupFileNames = new int[maxGroupId + 1][];

            for (var var37 = 0; var37 < groupCount; ++var37) {
                var group = groupId[var37];
                var groupSize = this.groupSize[group];
                groupFileNames[group] = new int[groupMaxFileId[group]];

                for (var i = 0; i < groupMaxFileId[group]; ++i) {
                    groupFileNames[group][i] = -1;
                }

                for (var i = 0; i < groupSize; ++i) {
                    int fileId;

                    if (groupFileIds[group] != null) {
                        fileId = groupFileIds[group][i];
                    } else {
                        fileId = i;
                    }

                    groupFileNames[group][fileId] = packet.g4s();
                }
            }
        }
    }

    public int[] getGroupFileIDs(int groupID) {
        var fileIds = groupFileIds[groupID];
        if (fileIds == null) {
            fileIds = new int[groupMaxFileId[groupID]];
            for (var fileId = 0; fileId < fileIds.length; ++fileId) {
                fileIds[fileId] = fileId;
            }
        }
        return fileIds;
    }
}
