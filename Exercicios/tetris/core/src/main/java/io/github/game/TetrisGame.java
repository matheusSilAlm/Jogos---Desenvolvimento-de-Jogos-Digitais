package io.github.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Classe principal do jogo Tetris Edicao Sovietica 1987.
 * Gerencia ciclo de vida, audio, configuracoes e transicao de telas.
 */
public class TetrisGame extends Game {
    private AudioManager audioManager;
    private Preferences preferences;
    private int startingLevel = 1;
    private boolean challengeMode = false;

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

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = Math.max(1, Math.min(10, startingLevel));
    }

    public boolean isChallengeMode() {
        return challengeMode;
    }

    public void setChallengeMode(boolean challengeMode) {
        this.challengeMode = challengeMode;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audioManager != null) {
            audioManager.dispose();
        }
    }
}
