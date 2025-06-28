package osrs.util;

import java.util.*;

public class Lattice<T> {
    private final Map<T, Set<T>> upper = new HashMap<>();
    private final Map<T, Set<T>> lower = new HashMap<>();

    public void add(T a, T b) {
        if (!test(a, b)) {
            for (var x : lower(a)) {
                for (var y : upper(b)) { // x <= a <= b <= y -> x <= y
                    upper(x).add(y);
                    lower(y).add(x);
                }
            }
        }
    }

    public boolean test(T a, T b) {
        return upper(a).contains(b);
    }

    public Set<T> vertices() {
        return upper.keySet();
    }

    public T meet(T a, T b) {
        if (a == b) return a; // optimization
        if (test(a, b)) return a; // optimization
        if (test(b, a)) return b; // optimization
        return meet(List.of(a, b));
    }

    public T meet(List<T> ts) {
        // intersect all subtypes
        var possible = new LinkedHashSet<>(vertices());

        for (var t : ts) {
            possible.removeIf(x -> !test(x, t));
        }

        // choose max
        var max = possible.getFirst();

        for (var type : possible) {
            if (test(max, type)) {
                max = type;
            }
        }

        return max;
    }

    public T join(T a, T b) {
        if (a == b) return a; // optimization
        if (test(a, b)) return b; // optimization
        if (test(b, a)) return a; // optimization
        return join(List.of(a, b));
    }

    public T join(List<T> ts) {
        // intersect all supertypes
        var possible = new LinkedHashSet<>(vertices());

        for (var t : ts) {
            possible.removeIf(x -> !test(t, x));
        }

        // choose min
        var min = possible.getFirst();

        for (var type : possible) {
            if (test(type, min)) {
                min = type;
            }
        }

        return min;
    }

    public Set<T> upper(T a) {
        var result = upper.get(a);

        if (result == null) {
            result = new HashSet<>();
            result.add(a);
            upper.put(a, result);
        }

        return result;
    }

    public Set<T> lower(T a) {
        var result = lower.get(a);

        if (result == null) {
            result = new HashSet<>();
            result.add(a);
            lower.put(a, result);
        }

        return result;
    }
}
