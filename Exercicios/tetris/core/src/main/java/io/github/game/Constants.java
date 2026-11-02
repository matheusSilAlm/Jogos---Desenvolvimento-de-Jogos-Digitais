package io.github.game;

public class Constants {
    public static final int BOARD_COLS = 10;
    public static final int BOARD_ROWS = 20;
    public static final int BLOCK_SIZE = 30;

    public static final int SCREEN_WIDTH = BOARD_COLS * BLOCK_SIZE + 200;  // 500
    public static final int SCREEN_HEIGHT = BOARD_ROWS * BLOCK_SIZE;      // 600

    public static final float BASE_DROP_INTERVAL = 1.0f;
    public static final float DROP_SPEED_FACTOR = 0.9f;
}
