package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Gerenciador de audio: Musicas de fundo e Efeitos Sonoros Arcade 8-bit.
 */
public class AudioManager {
    private Music menuMusic;
    private Music gameMusic;
    private boolean muted = false;
    private float musicVolume = 0.65f;
    private float soundVolume = 0.85f;

    // SFX
    private Sound sfxMove;
    private Sound sfxRotate;
    private Sound sfxDrop;
    private Sound sfxHardDrop;
    private Sound sfxHold;
    private Sound sfxClear;
    private Sound sfxTetris;
    private Sound sfxGameOver;

    public AudioManager() {
        // Musicas
        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("04 - Northern countries' Mambo.mp3"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(musicVolume);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Falha musica menu", e);
        }

        try {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("01 - Hawker's song.mp3"));
            gameMusic.setLooping(true);
            gameMusic.setVolume(musicVolume);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Falha musica jogo", e);
        }

        // SFX
        sfxMove = loadSound("sfx_move.wav");
        sfxRotate = loadSound("sfx_rotate.wav");
        sfxDrop = loadSound("sfx_drop.wav");
        sfxHardDrop = loadSound("sfx_hard_drop.wav");
        sfxHold = loadSound("sfx_hold.wav");
        sfxClear = loadSound("sfx_clear.wav");
        sfxTetris = loadSound("sfx_tetris.wav");
        sfxGameOver = loadSound("sfx_game_over.wav");
    }

    private Sound loadSound(String file) {
        try {
            return Gdx.audio.newSound(Gdx.files.internal(file));
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Falha som: " + file, e);
            return null;
        }
    }

    public void playMenuMusic() {
        if (gameMusic != null && gameMusic.isPlaying()) {
            gameMusic.stop();
        }
        if (menuMusic != null && !muted && !menuMusic.isPlaying()) {
            menuMusic.setPosition(0);
            menuMusic.play();
        }
    }

    public void playGameMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.stop();
        }
        if (gameMusic != null && !muted && !gameMusic.isPlaying()) {
            gameMusic.setPosition(0);
            gameMusic.play();
        }
    }

    public void pauseGameMusic() {
        if (gameMusic != null && gameMusic.isPlaying()) {
            gameMusic.pause();
        }
    }

    public void resumeGameMusic() {
        if (gameMusic != null && !muted && !gameMusic.isPlaying()) {
            gameMusic.play();
        }
    }

    public void stopAll() {
        if (menuMusic != null && menuMusic.isPlaying()) menuMusic.stop();
        if (gameMusic != null && gameMusic.isPlaying()) gameMusic.stop();
    }

    // Gatilhos de SFX
    public void playMove() {
        if (!muted && sfxMove != null) sfxMove.play(soundVolume * 0.7f);
    }

    public void playRotate() {
        if (!muted && sfxRotate != null) sfxRotate.play(soundVolume);
    }

    public void playDrop() {
        if (!muted && sfxDrop != null) sfxDrop.play(soundVolume * 0.8f);
    }

    public void playHardDrop() {
        if (!muted && sfxHardDrop != null) sfxHardDrop.play(soundVolume);
    }

    public void playHold() {
        if (!muted && sfxHold != null) sfxHold.play(soundVolume);
    }

    public void playClear() {
        if (!muted && sfxClear != null) sfxClear.play(soundVolume);
    }

    public void playTetris() {
        if (!muted && sfxTetris != null) sfxTetris.play(soundVolume);
    }

    public void playGameOver() {
        if (!muted && sfxGameOver != null) sfxGameOver.play(soundVolume);
    }

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            if (menuMusic != null && menuMusic.isPlaying()) menuMusic.pause();
            if (gameMusic != null && gameMusic.isPlaying()) gameMusic.pause();
        } else {
            if (menuMusic != null) menuMusic.play();
            if (gameMusic != null) gameMusic.play();
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void dispose() {
        stopAll();
        if (menuMusic != null) { menuMusic.dispose(); menuMusic = null; }
        if (gameMusic != null) { gameMusic.dispose(); gameMusic = null; }
        if (sfxMove != null) { sfxMove.dispose(); sfxMove = null; }
        if (sfxRotate != null) { sfxRotate.dispose(); sfxRotate = null; }
        if (sfxDrop != null) { sfxDrop.dispose(); sfxDrop = null; }
        if (sfxHardDrop != null) { sfxHardDrop.dispose(); sfxHardDrop = null; }
        if (sfxHold != null) { sfxHold.dispose(); sfxHold = null; }
        if (sfxClear != null) { sfxClear.dispose(); sfxClear = null; }
        if (sfxTetris != null) { sfxTetris.dispose(); sfxTetris = null; }
        if (sfxGameOver != null) { sfxGameOver.dispose(); sfxGameOver = null; }
    }
}
