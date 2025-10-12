package server.dao;

import model.Chat;
import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO extends DAO{

    private UserDAO userDAO;

    public ChatDAO(){
        super();
    }

    public boolean saveChat(Chat newChat){
        String SQL_QUERY = "INSERT INTO chats (content, user_id) VALUES (?,?)";

        try{
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ps.setString(1, newChat.getContent());
            ps.setInt(2, newChat.getUser().getId());
            ps.executeQuery();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Chat> getAllChats(){
        String SQL_QUERY = "SELECT (content, user_id) FROM chats";
        List<Chat> result = new ArrayList<>();

        try{
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                User user = userDAO.getUserById(rs.getInt("user_id"));
                result.add(new Chat(
                        rs.getString("content"),
                        user
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
