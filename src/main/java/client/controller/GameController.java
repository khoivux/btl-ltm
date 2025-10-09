package client.controller;

import client.Client;
import constant.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import model.Message;
import java.io.IOException;
import java.util.List;

public class GameController {
    @FXML private Label lblPlayer1;
    @FXML private Label scorePlayer1;
    @FXML private Label lblPlayer2;
    @FXML private Label scorePlayer2;
    @FXML private Label timerLabel;
    @FXML private GridPane boardGrid;

    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }

    // Build an 8x8 grid of buttons
    public void buildBoard() {
        Platform.runLater(() -> {
            boardGrid.getChildren().clear();
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Button btn = new Button(" ");
                    btn.setMinSize(40, 40);
                    final int rr = r, cc = c;
                    btn.setOnAction(ev -> { ev.consume(); onCellClicked(rr, cc); });
                    boardGrid.add(btn, c, r);
                    GridPane.setHalignment(btn, HPos.CENTER);
                    GridPane.setValignment(btn, VPos.CENTER);
                }
            }
            System.out.println("******" + boardGrid + "******");
        });
    }

    private void onCellClicked(int row, int col) {
        try {
            Message m = new Message(MessageType.PICK_CELL, new int[]{row, col});
            client.sendMessage(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Called by Client when server sends SHOW_COLORS or GAME_START etc.
    public void onShowColors(List<String> colors) {
        // display colors temporarily on UI (could be a popup). For now print and build board.
        System.out.println("Show colors: " + colors);
        buildBoard();
    }

    /**
     * Called when a session starts and server provides the target colors and player names.
     * @param colors list of target color names
     * @param myName current player's username (may be null in tests)
     * @param opponentName opponent's username (may be null)
     */
    public void onSessionStart(List<String> colors, String myName, String opponentName) {
        System.out.println("Session start. Me=" + myName + " vs " + opponentName + " colors=" + colors);
        Platform.runLater(() -> {
            if (lblPlayer1 != null) lblPlayer1.setText(myName != null ? myName : "Player1");
            if (lblPlayer2 != null) lblPlayer2.setText(opponentName != null ? opponentName : "Player2");
            // For now, display colors on console and build the board; a better UI can show colored panes.
            onShowColors(colors);
        });
    }

    public void onGameTick(int secondsLeft) {
        Platform.runLater(() -> timerLabel.setText(String.valueOf(secondsLeft)));
    }

    public void onPickResult(int row, int col, boolean hit, String marker, int score1, int score2) {
        Platform.runLater(() -> {
            // update button text at row,col
            boardGrid.getChildren().stream().filter(n -> GridPane.getRowIndex(n) == row && GridPane.getColumnIndex(n) == col).findFirst().ifPresent(node -> {
                if (node instanceof Button) {
                    ((Button) node).setText(marker != null ? marker : (hit ? "OK" : "X"));
                    ((Button) node).setDisable(true);
                }
            });
            scorePlayer1.setText(String.valueOf(score1));
            scorePlayer2.setText(String.valueOf(score2));
        });
    }

    public void onGameEnd(int score1, int score2, String winner, int award1, int award2) {
        Platform.runLater(() -> {
            // simple alert via console for now
            System.out.println("Game ended. " + score1 + " - " + score2 + ", winner=" + winner);
        });
    }

    @FXML
    public void endGame() {
        try {
            client.sendMessage(new Message(MessageType.EXIT_GAME, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
