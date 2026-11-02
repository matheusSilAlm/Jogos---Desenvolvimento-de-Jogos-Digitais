package io.github.game;

import com.badlogic.gdx.graphics.Color;

public class Tetromino {
    private final TetrominoType type;
    private int[][] shape;
    private int x, y;
    private final Color color;

    public Tetromino(TetrominoType type) {
        this.type = type;
        this.shape = type.copyShape();
        this.color = type.color;
        this.x = Constants.BOARD_COLS / 2 - shape[0].length / 2;
        this.y = Constants.BOARD_ROWS - shape.length;
    }

    public Tetromino(Tetromino other) {
        this.type = other.type;
        this.shape = new int[other.shape.length][];
        for (int i = 0; i < other.shape.length; i++) {
            this.shape[i] = other.shape[i].clone();
        }
        this.x = other.x;
        this.y = other.y;
        this.color = other.color;
    }

    public boolean moveLeft(Board board) {
        if (board.isValidPosition(shape, x - 1, y)) { x--; return true; }
        return false;
    }

    public boolean moveRight(Board board) {
        if (board.isValidPosition(shape, x + 1, y)) { x++; return true; }
        return false;
    }

    public boolean moveDown(Board board) {
        if (board.isValidPosition(shape, x, y - 1)) { y--; return true; }
        return false;
    }

    public boolean rotate(Board board) {
        int[][] rotated = rotateMatrix(shape);
        if (board.isValidPosition(rotated, x, y)) {
            shape = rotated;
            return true;
        }
        return false;
    }

    private int[][] rotateMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] rotated = new int[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = matrix[r][c];
            }
        }
        return rotated;
    }

    public int[][] getShape() { return shape; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Color getColor() { return color; }
    public TetrominoType getType() { return type; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
