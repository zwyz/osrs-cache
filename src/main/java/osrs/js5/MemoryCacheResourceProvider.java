package osrs.js5;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryCacheResourceProvider implements Js5ResourceProvider {
    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();
    private final Js5ResourceProvider underlying;

    public MemoryCacheResourceProvider(Js5ResourceProvider underlying) {
        this.underlying = underlying;
    }

    @Override
    public byte[] get(int archive, int group, boolean priority) {
        return cache.computeIfAbsent(archive + "." + group, _ -> underlying.get(archive, group, priority));
    }
}
