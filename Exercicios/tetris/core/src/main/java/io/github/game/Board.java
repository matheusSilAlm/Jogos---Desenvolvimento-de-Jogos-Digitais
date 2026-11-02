package io.github.game;

import com.badlogic.gdx.graphics.Color;

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

    public int clearLines() {
        int linesCleared = 0;
        for (int r = 0; r < rows; r++) {
            if (isRowFull(r)) {
                removeRow(r);
                linesCleared++;
                r--;
            }
        }
        return linesCleared;
    }

    private boolean isRowFull(int row) {
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

    public Color getCell(int row, int col) { return grid[row][col]; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public void reset() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = null;
    }
}

