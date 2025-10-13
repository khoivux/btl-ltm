package server.controller;

import model.Chat;
import server.dao.ChatDAO;

import java.util.List;

public class ChatController {
    private ChatDAO chatDAO;

    public ChatController() {
        this.chatDAO = new ChatDAO();
    }

    public List<Chat> getAllChat(){
        return chatDAO.getAllChats();
    }

    public boolean saveChat(Chat chat) {
        return chatDAO.saveChat(chat);
    }
}
