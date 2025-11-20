package osrs.unpack;

import osrs.util.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBTableIndex {

    public final BaseVarType[] types;
    public final List<Map<Object, List<Integer>>> tuplesLookup;

    public DBTableIndex(Packet data) {
        int tupleCount = data.gvarint2();
        types = new BaseVarType[tupleCount];
        tuplesLookup = new ArrayList<>(tupleCount);

        for (int i = 0; i < tupleCount; ++i) {
            types[i] = BaseVarType.get(data.g1());

            var count = data.gvarint2();
            var keyRefs = new HashMap<Object, List<Integer>>(count);

            while (count-- > 0) {
                var key = switch (types[i]) {
                    case INTEGER -> data.g4s();
                    case LONG -> data.g8s();
                    case STRING -> data.gjstr();
                    default -> throw new IllegalStateException();
                };

                int refCount = data.gvarint2();
                var refs = new ArrayList<Integer>(refCount);

                while (refCount-- > 0) {
                    var rowId = data.gvarint2();
                    refs.add(rowId);
                }

                keyRefs.put(key, refs);
            }

            tuplesLookup.add(i, keyRefs);
        }

    }

    public List<Integer> find(Object key, int tupleIndex) {
        if (tupleIndex < 0) {
            tupleIndex = 0;
        }
        var keyRefs = tuplesLookup.get(tupleIndex);
        return keyRefs.get(key);
    }
}
