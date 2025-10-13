package server.dao;

import model.Chat;
import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

public class ChatDAO extends DAO{

    private UserDAO userDAO;

    public ChatDAO(){
        super();
        this.userDAO = new UserDAO();
    }

    public boolean saveChat(Chat newChat){
        String SQL_QUERY = "INSERT INTO chats (content, user_id) VALUES (?,?)";

        try{
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ps.setString(1, newChat.getContent());
            ps.setInt(2, newChat.getUser().getId());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Chat> getAllChats(){
        String SQL_QUERY = "SELECT id, content, user_id FROM chats ORDER BY id ASC";
        List<Chat> result = new ArrayList<>();

        try{
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                User user = userDAO.getUserById(rs.getInt("user_id"));
                result.add(new Chat(
                        rs.getString("content"),
                        user,
                        rs.getInt("id")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
