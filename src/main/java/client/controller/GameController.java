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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Message;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameController {
    @FXML private Label lblPlayer1;
    @FXML private Label scorePlayer1;
    @FXML private Label lblPlayer2;
    @FXML private Label scorePlayer2;
    @FXML private Label timerLabel;
    @FXML private GridPane boardGrid;
    @FXML private HBox previewBox;


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

    private void showPreview(List<String> colors) {
        Platform.runLater(() -> {
            try {
                // Xóa mọi ô cũ
//                previewBox.getChildren().clear();

                // Thêm Rectangle cho từng màu
                for (String col : colors) {
                    Rectangle r = new Rectangle(50, 50);
                    r.setFill(parseColor(col));
                    r.setStroke(Color.BLACK);   // viền để dễ nhìn
                    r.setArcWidth(10);
                    r.setArcHeight(10);
                    previewBox.getChildren().add(r);
                }
                Label testLabel = new Label("TEST PREVIEW");
                testLabel.setStyle("-fx-text-fill: red; -fx-font-size: 20px;"); // màu chữ dễ nhìn
                previewBox.getChildren().add(testLabel);
                System.out.println(previewBox.getChildren());

                // Ép layout và áp dụng CSS
                previewBox.applyCss();
                previewBox.layout();

                // Hiển thị previewBox, ẩn boardGrid
                previewBox.setVisible(true);
                previewBox.setManaged(true);
                boardGrid.setVisible(false);
                boardGrid.setManaged(false);

                // Sau 3 giây, ẩn previewBox và hiển thị board
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.schedule(() -> {
                    Platform.runLater(() -> {
                        previewBox.setVisible(false);
                        previewBox.setManaged(false);

                        boardGrid.setVisible(true);
                        boardGrid.setManaged(true);

                        // Build board sau khi preview ẩn
                        buildBoard();
                    });
                    scheduler.shutdown();
                }, 3, TimeUnit.SECONDS);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Chuyển màu dạng tên hoặc ARGB sang Color JavaFX
     */
    private Color parseColor(String raw) {
        try {
            // Nếu là tên màu chuẩn như "RED", "CYAN"...
            return Color.valueOf(raw);
        } catch (IllegalArgumentException e) {
            if (raw.startsWith("0x") && raw.length() == 10) {
                // 0xAARRGGBB -> #RRGGBBAA
                String a = raw.substring(2, 4);
                String r = raw.substring(4, 6);
                String g = raw.substring(6, 8);
                String b = raw.substring(8, 10);
                return Color.web("#" + r + g + b + a);
            }
            return Color.GRAY; // fallback
        }
    }



    // Called by Client when server sends SHOW_COLORS or GAME_START etc.
    public void onShowColors(List<String> colors) {
        showPreview(colors);
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
