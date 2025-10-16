package constant;

public enum Status {
    AVAILABLE("Rảnh"),
    NOT_AVAILABLE("Trong trận"),
    OFFLINE("Offline");
    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}

