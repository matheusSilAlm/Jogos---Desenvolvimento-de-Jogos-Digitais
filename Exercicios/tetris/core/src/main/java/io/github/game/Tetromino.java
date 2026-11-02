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
        if (type == TetrominoType.O) {
            return true; // Peca O eh simetrica, nao precisa rotacionar
        }

        int[][] rotated = rotateMatrix(shape);

        // Wall Kicks e Floor Kicks para nunca travar nas paredes ou chão
        int[][] kicks = {
            {0, 0},   // Posição original
            {-1, 0},  // Empurrão para esquerda (se encostado na parede direita)
            {1, 0},   // Empurrão para direita (se encostado na parede esquerda)
            {-2, 0},  // Empurrão 2 células esquerda (peça I na parede)
            {2, 0},   // Empurrão 2 células direita (peça I na parede)
            {-3, 0},  // Empurrão 3 células esquerda (peça I no canto extremo)
            {0, 1},   // Floor kick (empurrão para cima se encostado no fundo)
            {0, -1},  // Teto (se rotacionar no topo da tela)
            {0, 2},   // 2 células para cima
            {0, -2},  // 2 células para baixo
            {-1, 1},  // Diagonal esquerda-cima
            {1, 1},   // Diagonal direita-cima
            {-1, -1}, // Diagonal esquerda-baixo
            {1, -1},  // Diagonal direita-baixo
            {-2, 1},
            {2, 1}
        };

        for (int[] kick : kicks) {
            int testX = x + kick[0];
            int testY = y + kick[1];
            if (board.isValidPosition(rotated, testX, testY)) {
                shape = rotated;
                x = testX;
                y = testY;
                return true;
            }
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
