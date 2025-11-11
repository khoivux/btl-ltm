package model;

import java.io.Serializable;

public class Chat implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String content;
    private User user;

    public Chat(String content, User user) {
        this.content = content;
        this.user = user;
    }

    public Chat(String content, User user, int id) {
        this.content = content;
        this.user = user;
        this.id = id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString(){
        return (this.user.getUsername() + ": " + this.content);
    }
}
