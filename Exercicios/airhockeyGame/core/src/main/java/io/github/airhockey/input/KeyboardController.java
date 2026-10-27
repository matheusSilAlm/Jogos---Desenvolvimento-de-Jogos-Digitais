package io.github.airhockey.input;

import com.badlogic.gdx.Gdx;

public class KeyboardController implements PlayerController {
    private final int upKey;
    private final int downKey;
    private final int leftKey;
    private final int rightKey;
    private final int[] boostKeys;

    public KeyboardController(int upKey, int downKey, int leftKey, int rightKey, int... boostKeys) {
        this.upKey = upKey;
        this.downKey = downKey;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.boostKeys = boostKeys;
    }

    @Override
    public float getMoveX() {
        float dx = 0f;
        if (Gdx.input.isKeyPressed(leftKey)) dx -= 1f;
        if (Gdx.input.isKeyPressed(rightKey)) dx += 1f;
        return dx;
    }

    @Override
    public float getMoveY() {
        float dy = 0f;
        if (Gdx.input.isKeyPressed(downKey)) dy -= 1f;
        if (Gdx.input.isKeyPressed(upKey)) dy += 1f;
        return dy;
    }

    @Override
    public boolean isBoost() {
        if (boostKeys == null) return false;
        for (int key : boostKeys) {
            if (Gdx.input.isKeyPressed(key)) return true;
        }
        return false;
    }
}
