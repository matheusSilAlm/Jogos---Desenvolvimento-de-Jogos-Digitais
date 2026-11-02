package io.github.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Classe principal do jogo Tetris Edicao Sovietica X68000 (1987-1988).
 * Gerencia ciclo de vida, audio e telas de Menu e Gameplay.
 */
public class TetrisGame extends Game {
    private AudioManager audioManager;
    private Preferences preferences;

    @Override
    public void create() {
        preferences = Gdx.app.getPreferences("tetris_soviet_1987");
        audioManager = new AudioManager();
        showMenu();
    }

    public void showMenu() {
        setScreen(new MenuScreen(this));
    }

    public void startGame() {
        setScreen(new GameScreen(this));
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audioManager != null) {
            audioManager.dispose();
        }
    }
}
