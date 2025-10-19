package server.controller;

import model.Match;
import server.dao.*;

import java.util.*;

public class MatchHitstoryController
{
    private MatchDAO matchDAO;
    private DetailMatchDAO detailMatchDAO;
    public  MatchHitstoryController()
    {
       this.matchDAO =new MatchDAO();
       this.detailMatchDAO = new DetailMatchDAO();
    }
    public List<Match> getMatchesByUsername(String username){
        return matchDAO.getMatchesByUser(username);
    }
}
