package server.manager;

import java.util.*;
import java.util.concurrent.*;

import constant.MessageType;
import model.Message;
import server.ClientHandler;
import server.controller.GameController;

public class GameManager {
    private final Map<String, SessionInfo> userSessionMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private static final List<String> COLOR_POOL = Arrays.asList(
            "#FF0000", // RED
            "#00FF00", // GREEN
            "#0000FF", // BLUE
            "#FFFF00", // YELLOW
            "#FFA500", // ORANGE
            "#FFC0CB", // PINK
            "#800080", // PURPLE
            "#00FFFF",  // CYAN
            "#FF00FF", // MAGENTA / FUCHSIA (Hồng đậm)
            "#A52A2A", // BROWN (Nâu)
            "#008080", // TEAL (Xanh mòng két)
            "#32CD32", // LIME GREEN (Xanh lá mạ)
            "#000080",
            "#FFFFFF", // WHITE (Trắng)
            "#808080" // GRAY (Xám)
    );

    public GameManager() {
    }

    private static class SessionInfo {
        GameController session;
        ClientHandler ch1;
        ClientHandler ch2;
        ScheduledFuture<?> tickTask;
        int secondsLeft;
    }

    public synchronized void createSession(ClientHandler ch1, ClientHandler ch2) {
        if (ch1 == null || ch2 == null) return;
        if (ch1.getUser() == null || ch2.getUser() == null) return;
        String u1 = ch1.getUser().getUsername();
        String u2 = ch2.getUser().getUsername();
        if (u1 == null || u2 == null) return;
        if (userSessionMap.containsKey(u1) || userSessionMap.containsKey(u2)) {
            System.out.println("GameManager: one of players already in session: " + u1 + "," + u2);
            return;
        }

        // pick 5 random colors
        Set<String> colorSet = new LinkedHashSet<>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<String> pool = COLOR_POOL;
        while (colorSet.size() < 5) {
            String hex = pool.get(rnd.nextInt(pool.size()));
            colorSet.add(hex);
        }
        List<String> targetColors = new ArrayList<>(colorSet);

        GameController session = new GameController(ch1.getUser(), ch2.getUser(), targetColors);

        SessionInfo info = new SessionInfo();
        info.session = session;
        info.ch1 = ch1;
        info.ch2 = ch2;
        info.secondsLeft = 15;

        userSessionMap.put(u1, info);
        userSessionMap.put(u2, info);

        System.out.println("GameManager: creating session for " + u1 + " vs " + u2 + ", colors=" + targetColors);

        Object[] payload1 = new Object[]{ new ArrayList<>(targetColors), u1, u2 , session.getBoard().getBoardData()};
        Object[] payload2 = new Object[]{ new ArrayList<>(targetColors), u1, u2 , session.getBoard().getBoardData()};
        try { if (info.ch1 != null) info.ch1.sendResponse(new Message(MessageType.START_GAME, payload1)); } catch (Exception ignored) {}
        try { if (info.ch2 != null) info.ch2.sendResponse(new Message(MessageType.START_GAME, payload2)); } catch (Exception ignored) {}

        Message show = new Message(MessageType.SHOW_COLORS, new ArrayList<>(targetColors));
        try { if (info.ch1 != null) info.ch1.sendResponse(show); } catch (Exception ignored) {}
        try { if (info.ch2 != null) info.ch2.sendResponse(show); } catch (Exception ignored) {}

        System.out.println("GameManager: scheduled game start in 3s for " + u1 + " vs " + u2);
        scheduler.schedule(() -> startGame(info), 3, TimeUnit.SECONDS);
    }


    private void startGame(SessionInfo info) {
        if (info == null || info.ch1 == null || info.ch2 == null) return;
        info.secondsLeft = 15;
        System.out.println("GameManager: starting game, ticking for session between " + info.ch1.getUser().getUsername() + " and " + info.ch2.getUser().getUsername());
        broadcast(info, new Message(MessageType.GAME_TICK, info.secondsLeft));

        info.tickTask = scheduler.scheduleAtFixedRate(() -> {
            info.secondsLeft -= 1;
            if (info.secondsLeft >= 0) {
                broadcast(info, new Message(MessageType.GAME_TICK, info.secondsLeft));
            }
            if (info.secondsLeft <= 0) {
                System.out.println("GameManager: time up for session " + info.ch1.getUser().getUsername() + " vs " + info.ch2.getUser().getUsername());
                endSession(info,null);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void handlePick(ClientHandler from, int row, int col) {
        if (from == null || from.getUser() == null) return;
        SessionInfo info = userSessionMap.get(from.getUser().getUsername());
        if (info == null) return;

        System.out.println("GameManager: handlePick from " + from.getUser().getUsername() + " at (" + row + "," + col + ")");
        GameController.PickResult res = info.session.pickCell(from.getUser(), row, col);

        Object[] payload = new Object[]{row, col, res.hit, res.marker, res.scoreP1, res.scoreP2, from.getUser().getUsername()};
        broadcast(info, new Message(MessageType.PICK_RESULT, payload));
    }

    private synchronized void endSession(SessionInfo info, String usernameQuit) {
        if (info == null || info.session == null) return;
        if (info.tickTask != null) info.tickTask.cancel(false);

        System.out.println("GameManager: ending session between " + info.ch1.getUser().getUsername() + " and " + info.ch2.getUser().getUsername());

        GameController.MatchResult mr = info.session.endMatch(usernameQuit);
        boolean isQuit = (usernameQuit != null && !usernameQuit.isEmpty());
        Object[] payload = new Object[]{mr.score1, mr.score2, mr.winner, mr.awardP1, mr.awardP2, isQuit};
        broadcast(info, new Message(MessageType.MATCH_RESULT, payload));

        if (info.ch1 != null && info.ch1.getUser() != null) {
            try {
                info.ch1.getClientManager().updateStatus(info.ch1.getUser().getUsername(), constant.Status.AVAILABLE);
                System.out.println("Updated status for " + info.ch1.getUser().getUsername() + " to AVAILABLE");
            } catch (Exception e) {
                System.err.println("Failed to update status for player 1: " + e.getMessage());
            }
        }
        
        if (info.ch2 != null && info.ch2.getUser() != null) {
            try {
                info.ch2.getClientManager().updateStatus(info.ch2.getUser().getUsername(), constant.Status.AVAILABLE);
                System.out.println("Updated status for " + info.ch2.getUser().getUsername() + " to AVAILABLE");
            } catch (Exception e) {
                System.err.println("Failed to update status for player 2: " + e.getMessage());
            }
        }

        try { userSessionMap.remove(info.ch1.getUser().getUsername()); } catch (Exception ignored) {}
        try { userSessionMap.remove(info.ch2.getUser().getUsername()); } catch (Exception ignored) {}
        
        System.out.println("GameManager: Session ended. Players returned to lobby.");
    }

    public void handleExit(ClientHandler from) {
        if (from == null || from.getUser() == null) return;
        SessionInfo info = userSessionMap.get(from.getUser().getUsername());
        if (info == null) return;
        
        System.out.println("GameManager: " + from.getUser().getUsername() + " is exiting the game");

        endSession(info, from.getUser().getUsername());
    }

    private void broadcast(SessionInfo info, Message msg) {
        try {
            if (info.ch1 != null) info.ch1.sendResponse(msg);
        } catch (Exception e) {
        }
        try {
            if (info.ch2 != null) info.ch2.sendResponse(msg);
        } catch (Exception e) {
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
