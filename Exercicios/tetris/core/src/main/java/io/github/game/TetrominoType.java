package io.github.game;

import com.badlogic.gdx.graphics.Color;

public enum TetrominoType {
    I(new int[][]{{1, 1, 1, 1}}, Color.CYAN),
    O(new int[][]{{1, 1}, {1, 1}}, Color.YELLOW),
    T(new int[][]{{0, 1, 0}, {1, 1, 1}}, Color.PURPLE),
    S(new int[][]{{0, 1, 1}, {1, 1, 0}}, Color.GREEN),
    Z(new int[][]{{1, 1, 0}, {0, 1, 1}}, Color.RED),
    J(new int[][]{{1, 0, 0}, {1, 1, 1}}, Color.BLUE),
    L(new int[][]{{0, 0, 1}, {1, 1, 1}}, Color.ORANGE);

    public final int[][] shape;
    public final Color color;

    TetrominoType(int[][] shape, Color color) {
        this.shape = shape;
        this.color = color;
    }

    public int[][] copyShape() {
        int[][] copy = new int[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            copy[i] = shape[i].clone();
        }
        return copy;
    }
}
