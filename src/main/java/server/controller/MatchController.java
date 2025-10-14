//// controller/GameController.java
//package server.controller;
//
//import model.*;
//
//import java.util.*;
//
//public class MatchController {
//    private GameSession session;
//
//    public void startNewGame(User p1, User p2, List<String> targetColors) {
//        this.session = new GameSession(p1,p2,targetColors);
//        System.out.println("Trận mới giữa " + p1.getUsername() + " và " + p2.getUsername());
//        session.getBoard().printBoard();
//    }
//
//    public void handlePick(User player,int row,int col) {
//        session.pickCell(player,row,col);
//        System.out.println("Điểm hiện tại: " + session.getScores());
//    }
//
//    public void endGame() {
//        session.endMatch();
//        System.out.println("Kết quả cuối: " + session.getScores());
//    }
//
//    public GameSession getSession() { return session; }
//}
