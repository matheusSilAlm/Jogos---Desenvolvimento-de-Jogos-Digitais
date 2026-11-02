package io.github.game;

import com.badlogic.gdx.graphics.Color;

public enum TetrominoType {
    I(new int[][]{{1, 1, 1, 1}}, new Color(0.15f, 0.90f, 0.95f, 1f)),     // Cyan elétrico
    O(new int[][]{{1, 1}, {1, 1}}, new Color(0.98f, 0.85f, 0.15f, 1f)),   // Ouro soviético
    T(new int[][]{{0, 1, 0}, {1, 1, 1}}, new Color(0.80f, 0.35f, 0.90f, 1f)), // Púrpura arcade
    S(new int[][]{{0, 1, 1}, {1, 1, 0}}, new Color(0.20f, 0.88f, 0.35f, 1f)), // Verde esmeralda
    Z(new int[][]{{1, 1, 0}, {0, 1, 1}}, new Color(0.95f, 0.20f, 0.20f, 1f)), // Vermelho rubi / Kremlin
    J(new int[][]{{1, 0, 0}, {1, 1, 1}}, new Color(0.25f, 0.55f, 0.98f, 1f)), // Azul cobalto
    L(new int[][]{{0, 0, 1}, {1, 1, 1}}, new Color(0.98f, 0.55f, 0.12f, 1f)); // Laranja âmbar

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
