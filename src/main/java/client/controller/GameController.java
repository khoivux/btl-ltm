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
import javafx.scene.text.Font;
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
    private String[][] currentBoardData; // <-- BIẾN MỚI: Lưu trữ dữ liệu màu của bảng

    public void setClient(Client client) {
        this.client = client;
    }

    // Cần gọi phương thức này khi nhận được dữ liệu bảng từ server (ví dụ: trong onSessionStart)
    public void setBoardData(String[][] boardData) {
        this.currentBoardData = boardData;
    }

    // Build an 8x8 grid of buttons
    public void buildBoard() {
        Platform.runLater(() -> {
            boardGrid.getChildren().clear();
            if (currentBoardData == null) {
                System.err.println("Board data not set. Cannot build board.");
                return;
            }

            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Button btn = new Button(" ");
                    btn.setMinSize(40, 40);
                    btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                    // --- THAY ĐỔI: Gán màu nền cho nút dựa trên currentBoardData ---
                    String colorName = currentBoardData[r][c];
                    Color buttonColor = parseColor(colorName);

                    // Đặt màu nền bằng CSS cho Button
                    String style = String.format("-fx-background-color: %s; -fx-text-fill: black;",
                            toWebColor(buttonColor));
                    btn.setStyle(style);

                    // Đặt màu viền để dễ phân biệt ô
                    btn.setStyle(btn.getStyle() + " -fx-border-color: #333333; -fx-border-width: 1; -fx-font-weight: bold;");

                    // -----------------------------------------------------------------

                    final int rr = r, cc = c;
                    btn.setOnAction(ev -> { ev.consume(); onCellClicked(rr, cc); });

                    boardGrid.add(btn, c, r);
                    GridPane.setHalignment(btn, HPos.CENTER);
                    GridPane.setValignment(btn, VPos.CENTER);
                }
            }
            // Đảm bảo GridPane giãn ra để chứa các nút
            boardGrid.requestLayout();
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
                // Xóa mọi ô cũ và reset lại chỉ có label "Màu mục tiêu (3s)"
                previewBox.getChildren().clear();
                Label title = new Label("Màu mục tiêu (3s)");
                title.setFont(new Font("System Bold", 24.0));
                previewBox.getChildren().add(title);


                // Thêm Rectangle cho từng màu (đặt ở giữa)
                for (String col : colors) {
                    Rectangle r = new Rectangle(50, 50);
                    r.setFill(parseColor(col));
                    r.setStroke(Color.BLACK);   // viền để dễ nhìn
                    r.setArcWidth(10);
                    r.setArcHeight(10);
                    previewBox.getChildren().add(r);
                }

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
            return Color.valueOf(raw);
        } catch (IllegalArgumentException e) {
            // ... (Giữ nguyên logic chuyển đổi màu của bạn nếu cần)
            return Color.GRAY; // fallback
        }
    }

    /**
     * Chuyển JavaFX Color sang mã hex cho CSS
     */
    private String toWebColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    // Called by Client when server sends SHOW_COLORS or GAME_START etc.
    public void onShowColors(List<String> colors) {
        showPreview(colors);
    }


    /**
     * Called when a session starts and server provides the target colors, player names VÀ DỮ LIỆU BẢNG.
     * @param colors list of target color names
     * @param myName current player's username
     * @param opponentName opponent's username
     * @param boardData Mảng 2D chứa màu của từng ô (Nếu server gửi) <-- THAM SỐ GIẢ ĐỊNH MỚI
     */
    public void onSessionStart(List<String> colors, String myName, String opponentName, String[][] boardData) {
        System.out.println("Session start. Me=" + myName + " vs " + opponentName + " colors=" + colors);
        setBoardData(boardData); // <-- LƯU DỮ LIỆU BẢNG

        Platform.runLater(() -> {
            if (lblPlayer1 != null) lblPlayer1.setText(myName != null ? myName : "Player1");
            if (lblPlayer2 != null) lblPlayer2.setText(opponentName != null ? opponentName : "Player2");
            onShowColors(colors);
        });
    }

    public void onGameTick(int secondsLeft) {
        Platform.runLater(() -> timerLabel.setText(String.valueOf(secondsLeft)));
    }

    public void onPickResult(int row, int col, boolean hit, String marker, int score1, int score2) {
        Platform.runLater(() -> {
            // update button text and style at row,col
            boardGrid.getChildren().stream().filter(n -> GridPane.getRowIndex(n) == row && GridPane.getColumnIndex(n) == col).findFirst().ifPresent(node -> {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    if(hit) {

                        // --- THAY ĐỔI: Thay đổi style của nút sau khi chọn ---
                        btn.setText(marker != null ? marker : (hit ? "OK" : "X"));
                        btn.setDisable(true);

                        // Đổi màu nền để biểu thị ô đã được chọn
                        Color pickedColor = parseColor(currentBoardData[row][col]); // Lấy màu ban đầu
                        String newStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14pt;",
                                toWebColor(pickedColor.darker().darker())); // Làm tối màu để đánh dấu
                        btn.setStyle(newStyle + " -fx-border-color: white; -fx-border-width: 2; -fx-font-weight: bold;");
                    }
                }
            });
            scorePlayer1.setText(String.valueOf(score1));
            scorePlayer2.setText(String.valueOf(score2));
        });
    }

    // ... (Các phương thức khác giữ nguyên)

    public void onGameEnd(int score1, int score2, String winner, int award1, int award2) {
        Platform.runLater(() -> {
            try {
                String title = "Kết quả trận đấu";
                String header = null;
                String body;
                if (winner == null) {
                    body = String.format("Hòa! Điểm: %d - %d\nMỗi người được +%d điểm.", score1, score2, award1);
                } else {
                    body = String.format("%s thắng! Điểm: %d - %d\n%s được +%d điểm.", winner, score1, score2,
                            winner.equals(lblPlayer1.getText()) ? lblPlayer1.getText() : lblPlayer2.getText(),
                            winner.equals(lblPlayer1.getText()) ? award1 : award2);
                }

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(body);
                alert.showAndWait();

                // After acknowledging, return to main UI
                if (client != null) {
                    client.showMainUI();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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