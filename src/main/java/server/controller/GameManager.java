package server.controller;

import model.Message;
import constant.MessageType;
import server.ClientHandler;

import java.util.*;
import java.util.concurrent.*;

/**
 * GameManager orchestrates game sessions between two connected clients.
 * It sends SHOW_COLORS, schedules the short preview, runs the game ticks,
 * handles picks and ends sessions with MATCH_RESULT.
 */
public class GameManager {
    private final Map<String, SessionInfo> userSessionMap = new ConcurrentHashMap<>(); // username -> session
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private static final List<String> COLOR_POOL = Arrays.asList("RED","GREEN","BLUE","YELLOW","ORANGE","PINK","PURPLE","CYAN");

    public GameManager() {
    }

    private static class SessionInfo {
        GameSession session;
        ClientHandler ch1;
        ClientHandler ch2;
        ScheduledFuture<?> tickTask;
        int secondsLeft;
    }

    /**
     * Create a new session between two client handlers and start the preview -> game flow.
     */
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
        List<String> pool = new ArrayList<>(COLOR_POOL);
        Collections.shuffle(pool);
        List<String> targetColors = pool.subList(0, Math.min(5, pool.size()));

        GameSession session = new GameSession(ch1.getUser(), ch2.getUser(), targetColors);

        SessionInfo info = new SessionInfo();
        info.session = session;
        info.ch1 = ch1;
        info.ch2 = ch2;
        info.secondsLeft = 15;

        userSessionMap.put(u1, info);
        userSessionMap.put(u2, info);

        System.out.println("GameManager: creating session for " + u1 + " vs " + u2 + ", colors=" + targetColors);

        // send START_GAME (personalized payload) to both so clients can initialize UI
        Object[] payload1 = new Object[]{ new ArrayList<>(targetColors), u1, u2 , session.getBoard().getBoardData()};
        Object[] payload2 = new Object[]{ new ArrayList<>(targetColors), u1, u2 , session.getBoard().getBoardData()};
        try { if (info.ch1 != null) info.ch1.sendResponse(new Message(MessageType.START_GAME, payload1)); } catch (Exception ignored) {}
        try { if (info.ch2 != null) info.ch2.sendResponse(new Message(MessageType.START_GAME, payload2)); } catch (Exception ignored) {}

        // also send SHOW_COLORS (backwards-compatible) and schedule start after 3 seconds (preview period)
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

        // schedule tick every second
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

    /**
     * Handle a pick from a client: call session.pickCell and broadcast PICK_RESULT.
     */
    public void handlePick(ClientHandler from, int row, int col) {
        if (from == null || from.getUser() == null) return;
        SessionInfo info = userSessionMap.get(from.getUser().getUsername());
        if (info == null) return;

        System.out.println("GameManager: handlePick from " + from.getUser().getUsername() + " at (" + row + "," + col + ")");
        GameSession.PickResult res = info.session.pickCell(from.getUser(), row, col);

        // build payload: {row,col,hit,marker,scoreP1,scoreP2}
        Object[] payload = new Object[]{row, col, res.hit, res.marker, res.scoreP1, res.scoreP2};
        broadcast(info, new Message(MessageType.PICK_RESULT, payload));
    }

    /**
     * End session: compute results via GameSession.endMatch(), broadcast MATCH_RESULT and cleanup.
     */
    private synchronized void endSession(SessionInfo info, String usernameQuit) {
        if (info == null || info.session == null) return;
        if (info.tickTask != null) info.tickTask.cancel(false);

        System.out.println("GameManager: ending session between " + info.ch1.getUser().getUsername() + " and " + info.ch2.getUser().getUsername());
        GameSession.MatchResult mr = info.session.endMatch(usernameQuit);
        Object[] payload = new Object[]{mr.score1, mr.score2, mr.winner, mr.awardP1, mr.awardP2};
        broadcast(info, new Message(MessageType.MATCH_RESULT, payload));

        // cleanup map entries for both users
        try { userSessionMap.remove(info.ch1.getUser().getUsername()); } catch (Exception ignored) {}
        try { userSessionMap.remove(info.ch2.getUser().getUsername()); } catch (Exception ignored) {}
    }

    /**
     * If a client exits mid-game or disconnects, end the session (award handled by GameSession.endMatch).
     */
    public void handleExit(ClientHandler from) {
        if (from == null || from.getUser() == null) return;
        SessionInfo info = userSessionMap.get(from.getUser().getUsername());
        if (info == null) return;
        endSession(info, from.getUser().getUsername());
    }

    private void broadcast(SessionInfo info, Message msg) {
        try {
            if (info.ch1 != null) info.ch1.sendResponse(msg);
        } catch (Exception e) {
            // ignore send failures
        }
        try {
            if (info.ch2 != null) info.ch2.sendResponse(msg);
        } catch (Exception e) {
            // ignore
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
