package server;

import java.util.*;

public class GameBoardManager {
    private final int ROWS = 8;
    private final int COLS = 8;
    private String[][] board; // mỗi ô là một màu (ví dụ: "RED", "BLUE"...)

    public GameBoardManager(List<String> targetColors) {
        board = new String[ROWS][COLS];
        generateBoard(targetColors);
    }

    private void generateBoard(List<String> targetColors) {
        // targetColors là danh sách 5 màu được server gửi xuống
        Random rand = new Random();
        List<String> allColors = Arrays.asList("RED", "GREEN", "BLUE", "YELLOW", "ORANGE", "PINK", "PURPLE", "CYAN");

        // reset bảng toàn bộ về màu sai trước
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                // chọn màu ngẫu nhiên trong các màu sai
                String wrongColor;
                do {
                    wrongColor = allColors.get(rand.nextInt(allColors.size()));
                } while (targetColors.contains(wrongColor));
                board[i][j] = wrongColor;
            }
        }

        // đặt 2-3 ô đúng cho từng màu trong targetColors
        for (String color : targetColors) {
            int count = 2 + rand.nextInt(2); // 2 hoặc 3 ô
            for (int k = 0; k < count; k++) {
                int r, c;
                do {
                    r = rand.nextInt(ROWS);
                    c = rand.nextInt(COLS);
                } while (targetColors.contains(board[r][c])); // tránh ghi đè
                board[r][c] = color;
            }
        }
    }

    public String getCell(int row, int col) {
        return board[row][col];
    }

    public void setCell(int row, int col, String mark) {
        board[row][col] = mark; // đánh dấu P1/P2
    }

    public void printBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                System.out.print(board[i][j] + "\t");
            }
            System.out.println();
        }
    }
}
