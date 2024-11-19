package records;

public enum Type {
    REAL (0),
    CATEGORY (1),
    TIME (2),
    REAL_LABEL (3),
    CLASS_LABEL (4),
    INFO (5);

    private final int levelCode;

    Type(int levelCode) {
        this.levelCode = levelCode;
    }

    public int getLevelCode() {
        return this.levelCode;
    }

}