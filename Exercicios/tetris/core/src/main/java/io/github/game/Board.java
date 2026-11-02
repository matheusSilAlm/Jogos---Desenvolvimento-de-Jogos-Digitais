package io.github.game;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    private final int rows = Constants.BOARD_ROWS;
    private final int cols = Constants.BOARD_COLS;
    private final Color[][] grid;

    public Board() {
        grid = new Color[rows][cols];
    }

    public boolean isValidPosition(int[][] shape, int posX, int posY) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] == 0) continue;

                int boardRow = posY + (shape.length - 1 - r);
                int boardCol = posX + c;

                if (boardRow < 0 || boardRow >= rows || boardCol < 0 || boardCol >= cols) return false;
                if (grid[boardRow][boardCol] != null) return false;
            }
        }
        return true;
    }

    public void placePiece(Tetromino piece) {
        int[][] shape = piece.getShape();
        int posX = piece.getX();
        int posY = piece.getY();
        Color color = piece.getColor();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] == 0) continue;
                int boardRow = posY + (shape.length - 1 - r);
                int boardCol = posX + c;
                if (boardRow >= 0 && boardRow < rows && boardCol >= 0 && boardCol < cols) {
                    grid[boardRow][boardCol] = color;
                }
            }
        }
    }

    public List<Integer> getFullRows() {
        List<Integer> full = new ArrayList<Integer>();
        for (int r = 0; r < rows; r++) {
            if (isRowFull(r)) {
                full.add(r);
            }
        }
        return full;
    }

    public void removeRows(List<Integer> fullRows) {
        // Remove de cima para baixo ou em ordem decrescente
        for (int i = fullRows.size() - 1; i >= 0; i--) {
            int row = fullRows.get(i);
            removeRow(row);
        }
    }

    public int clearLines() {
        List<Integer> full = getFullRows();
        removeRows(full);
        return full.size();
    }

    public boolean isRowFull(int row) {
        for (int c = 0; c < cols; c++) {
            if (grid[row][c] == null) return false;
        }
        return true;
    }

    private void removeRow(int row) {
        for (int r = row; r < rows - 1; r++) {
            System.arraycopy(grid[r + 1], 0, grid[r], 0, cols);
        }
        for (int c = 0; c < cols; c++) {
            grid[rows - 1][c] = null;
        }
    }

    public int getHighestOccupiedRow() {
        for (int r = rows - 1; r >= 0; r--) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != null) return r;
            }
        }
        return -1;
    }

    public void generateGarbageRows(int numRows, Color[] palette, Random random) {
        reset();
        for (int r = 0; r < numRows && r < rows; r++) {
            int holes = 1 + random.nextInt(3); // 1 a 3 buracos
            boolean[] isHole = new boolean[cols];
            for (int h = 0; h < holes; h++) {
                isHole[random.nextInt(cols)] = true;
            }
            for (int c = 0; c < cols; c++) {
                if (!isHole[c]) {
                    grid[r][c] = palette[random.nextInt(palette.length)];
                }
            }
        }
    }

    public Color getCell(int row, int col) { return grid[row][col]; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public void reset() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = null;
    }
}
