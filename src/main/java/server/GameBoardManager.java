package server;

import java.util.*;

/**
 * GameBoardManager lưu trữ một bảng màu 8x8, trong đó mỗi ô là một màu trong hệ RGB (ví dụ: "#A1B2C3").
 * Các ô trên bàn cờ được tạo ra dưới dạng các màu ngẫu nhiên gần với màu mục tiêu được cung cấp,
 * có 2-3 ô mục tiêu chính xác được đặt cho mỗi màu mục tiêu.
 */

public class GameBoardManager {
    private final int ROWS = 8;
    private final int COLS = 8;
    private String[][] board; // each cell is a color hex (e.g. "#A1B2C3")
    int minDelta = 50; // minimum perturbation per channel
    int maxDelta = 200; // maximum perturbation per channel

    public GameBoardManager(List<String> targetColors) {
        board = new String[ROWS][COLS];
        generateBoard(targetColors);
    }

    private void generateBoard(List<String> targetColors) {
        if (targetColors == null || targetColors.isEmpty()) {
            throw new IllegalArgumentException("targetColors must contain at least one color");
        }

        Random rand = new Random();

        // Fill board with perturbed variants of random target colors
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                String variant;
                // pick a base target and perturb until variant is not exactly a target
                int attempts = 0;
                do {
                    String base = targetColors.get(rand.nextInt(targetColors.size()));
                    variant = perturbColor(base, minDelta, maxDelta, rand);
                    attempts++;
                    if (attempts > 100) break; // fallback to base if something odd happens
                } while (targetColors.contains(variant));
                board[i][j] = variant;
            }
        }

        // Place 2-3 exact target cells for each target color
        for (String color : targetColors) {
            int count = 3 + rand.nextInt(3); // 2 or 3
            for (int k = 0; k < count; k++) {
                int r, c, tries = 0;
                do {
                    r = rand.nextInt(ROWS);
                    c = rand.nextInt(COLS);
                    tries++;
                    // stop if too many tries (board may be mostly exacts)
                    if (tries > 200) break;
                } while (targetColors.contains(board[r][c])); // avoid overwriting an existing exact target
                board[r][c] = color;
            }
        }
    }

    private String perturbColor(String hex, int minDelta, int maxDelta, Random rand) {
        int[] rgb = hexToRgb(hex);

        // Tạo delta ngẫu nhiên cho từng kênh bằng hàm helper mới
        int r_delta = generateDelta(minDelta, maxDelta, rand);
        int g_delta = generateDelta(minDelta, maxDelta, rand);
        int b_delta = generateDelta(minDelta, maxDelta, rand);

        System.out.println(String.format("Deltas: r=%d, g=%d, b=%d", r_delta, g_delta, b_delta));

        // Áp dụng delta và "kẹp" giá trị trong khoảng [0, 255]
        int r = clamp(rgb[0] + r_delta, 0, 255);
        int g = clamp(rgb[1] + g_delta, 0, 255);
        int b = clamp(rgb[2] + b_delta, 0, 255);

        return rgbToHex(r, g, b);
    }

    private int generateDelta(int minDelta, int maxDelta, Random rand) {
        if (minDelta > maxDelta) {
            minDelta = maxDelta;
        }

        // Nếu minDelta và maxDelta đều là 0, delta = 0
        if (maxDelta == 0) {
            return 0;
        }

        int rangeSize = maxDelta - minDelta + 1;
        int magnitude = rand.nextInt(rangeSize) + minDelta;

        if (rand.nextBoolean()) {
            return magnitude;
        } else {
            return -magnitude;
        }
    }

    private int[] hexToRgb(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() != 6) return new int[] {0,0,0};
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return new int[] { r, g, b };
    }

    private String rgbToHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public String getCell(int row, int col) {
        return board[row][col];
    }

    public void setCell(int row, int col, String mark) {
        board[row][col] = mark; // mark player P1/P2 or other
    }

    public String[][] getBoardData() {
        String[][] data = new String[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            System.arraycopy(board[i], 0, data[i], 0, COLS);
        }
        return data;
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
