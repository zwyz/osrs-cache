package osrs.unpack;

public enum BaseVarType {
    INTEGER,
    LONG,
    STRING,
    ARRAY;

    public static BaseVarType get(int id) {
        return switch (id) {
            case 0 -> INTEGER;
            case 1 -> LONG;
            case 2 -> STRING;
            default -> throw new IllegalStateException();
        };
    }
}
