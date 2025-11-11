package client.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MatchController {

    // BƯỚC 3: Dùng @FXML và fx:id để liên kết biến này với GridPane trong FXML
    @FXML
    private GridPane colorGrid;

    // Danh sách 64 màu của bạn (hãy thay thế bằng màu của bạn nếu muốn)
    private final List<String> colorList = new ArrayList<>(Arrays.asList(
            "#e74c3c", "#f1c40f", "#2ecc71", "#3498db", "#9b59b6", "#e67e22", "#1abc9c", "#34495e",
            "#c0392b", "#f39c12", "#27ae60", "#2980b9", "#8e44ad", "#d35400", "#16a085", "#2c3e50",
            "#ff7675", "#fdcb6e", "#55efc4", "#74b9ff", "#a29bfe", "#fab1a0", "#00cec9", "#636e72",
            "#d63031", "#ffeaa7", "#00b894", "#0984e3", "#6c5ce7", "#e17055", "#81ecec", "#b2bec3",
            "#ff4757", "#feca57", "#1dd1a1", "#54a0ff", "#5f27cd", "#ff6b6b", "#48dbfb", "#3d3d3d",
            "#B53471", "#F97F51", "#2C3A47", "#4a69bd", "#6D214F", "#CAD3C8", "#1B1464", "#57606f",
            "#ff6348", "#f0932b", "#badc58", "#574b90", "#079992", "#38ada9", "#e55039", "#4a4a4a",
            "#fa983a", "#eb4d4b", "#6ab04c", "#30336b", "#130f40", "#f8a5c2", "#f7d794", "#778beb"
    ));

    // Phương thức này tự động chạy sau khi FXML được tải
    @FXML
    public void initialize() {
        populateColorGrid();
    }

    private void populateColorGrid() {
        // Xáo trộn màu sắc để mỗi lần chơi là khác nhau
        Collections.shuffle(colorList);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // Tính toán chỉ số màu trong danh sách
                int colorIndex = row * 8 + col;
                String hexColor = colorList.get(colorIndex);

                // Tạo một ô màu mới
                Pane colorPane = new Pane();
                colorPane.setStyle("-fx-background-color: " + hexColor + "; -fx-background-radius: 5;");


                int finalRow = row;
                int finalCol = col;

                // Thêm hiệu ứng khi di chuột vào (tùy chọn)
                colorPane.setOnMouseEntered(e -> colorPane.setStyle("-fx-background-color: " + hexColor + "; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);"));
                colorPane.setOnMouseExited(e -> colorPane.setStyle("-fx-background-color: " + hexColor + "; -fx-background-radius: 5;"));

                // Thêm sự kiện khi click chuột vào ô màu
                colorPane.setOnMouseClicked(event -> {

                    System.out.println("Đã chọn màu: " + hexColor + " tại ô [" + finalRow + "," + finalCol + "]");
                    // TODO: Thêm logic xử lý game của bạn ở đây
                });

                // Thêm ô màu vào lưới tại đúng vị trí cột và hàng
                colorGrid.add(colorPane, col, row);
            }
        }
    }
}