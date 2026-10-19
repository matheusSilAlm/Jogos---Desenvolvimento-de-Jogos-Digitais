package io.github.game;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

public class MyInputProcessor implements InputProcessor {
    private boolean moveLeft;
    private boolean moveRight;

    public boolean isMoveLeft() {
        return moveLeft;
    }

    public boolean isMoveRight() {
        return moveRight;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.LEFT) {
            moveLeft = true;
        } else if (keycode == Keys.RIGHT) {
            moveRight = true;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.LEFT) {
            moveLeft = false;
        } else if (keycode == Keys.RIGHT) {
            moveRight = false;
        }
        return true;
    }

    // Outros métodos não utilizados do InputProcessor devem retornar false ou true conforme necessário
    @Override
    public boolean keyTyped(char character) { return false; }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
}
