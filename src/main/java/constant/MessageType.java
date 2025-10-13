package constant;

public enum MessageType {
    // Authentication
    LOGIN("login"),
    LOGOUT("logout"),
    LOGIN_SUCCESS("login_success"),
    LOGIN_FAILURE("login_failure"),
    LOGOUT_SUCCESS("logout_success"),

    // Notification
    UPDATE_USER_STATUS("update_user_status"),

    // GET
    ONLINE_LIST("online_list"),

    // LEADERBOARD
    LEADERBOARD("leaderboard"),
    LEADERBOARD_SUCCESS("leaderboard_success"),
    LEADERBOARD_FAILURE("leaderboard_failure"),

    // CHAT
    CHAT("chat"),
    CHAT_SUCCESS("chat_success"),
    CHAT_FAILURE("chat_failure"),
    ADD_CHAT("add_chat"),
    ADD_CHAT_SUCCESS("add_chat_success"),
    ADD_CHAT_FAILURE("add_chat_failure"),

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
