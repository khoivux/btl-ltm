package constant;

public enum MessageType {
    // Authentication
    LOGIN("login"),
    LOGOUT("logout"),
    LOGIN_SUCCESS("login_success"),
    LOGIN_FAILURE("login_failure"),
    LOGOUT_SUCCESS("logout_success"),
    REGISTER("register"),
    REGISTER_SUCCESS("register_success"),
    REGISTER_FAILURE("register_failure"),

    // Notification
    UPDATE_USER_STATUS("update_user_status"),

    // GET
    ONLINE_LIST("online_list"),

    // LEADERBOARD
    LEADERBOARD("leaderboard"),
    LEADERBOARD_SUCCESS("leaderboard_success"),
    LEADERBOARD_FAILURE("leaderboard_failure"),
    LEADERBOARD_UPDATE("leaderboard_update"),

    // RANK
    RANK("rank"),
    RANK_SUCCESS("rank_success"),
    RANK_FAILURE("rank_failure"),

    TEST("hihi");
    private final String value;

    MessageType(String value) {
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
