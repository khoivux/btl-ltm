package model;

public class Chat {
    private static final long serialVersionUID = 1L;
    private int id;
    private String content;
    private User user;

    public Chat(String content, User user) {
        this.content = content;
        this.user = user;
    }

    public Chat(){}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
