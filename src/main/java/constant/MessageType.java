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
    TEST("hihi"),

    // Game-related messages
    INVITE_REQUEST("invite_request"),      // Người chơi A gửi lời mời tới B
    INVITE_RECEIVED("invite_received"),    // Server gửi thông báo đến B
    INVITE_ACCEPT("invite_accept"),        // B chấp nhận
    INVITE_REJECT("invite_reject"),        // B từ chối
    START_GAME("start_game"),              // Server thông báo bắt đầu trận
    GAME_START("game_start"),
    SHOW_COLORS("show_colors"),
    GAME_TICK("game_tick"),
    PICK_CELL("pick_cell"),
    PICK_RESULT("pick_result"),
    CELL_LOCKED("cell_locked"),
    GAME_END("game_end"),
    MATCH_RESULT("match_result"),
    REMATCH_OFFER("rematch_offer"),
    REMATCH_RESPONSE("rematch_response"),
    EXIT_GAME("exit_game"),
    UPDATE_SCORE("update_score");
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
