package constant;

public enum Status {
    PLAYING("Playing"),
    WIN("Win"),
    LOSE("Lose"),
    DRAW("Draw");
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

