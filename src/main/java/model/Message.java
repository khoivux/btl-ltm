package model;

import constant.MessageType;

import java.io.Serializable;

public class Message implements Serializable {
    private MessageType type;
    private Object content;

    public Message(MessageType type, Object content) {
        this.type = type;
        this.content = content;
    }

    // Getters
    public MessageType getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }
}
