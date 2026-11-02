package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class AudioManager {
    private Music menuMusic;
    private Music gameMusic;
    private boolean muted = false;
    private float volume = 0.70f;

    public AudioManager() {
        try {
            // Musica do menu: 04 - Northern countries' Mambo
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("04 - Northern countries' Mambo.mp3"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(volume);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Falha ao carregar musica do menu", e);
        }

        try {
            // Musica de gameplay: 01 - Hawker's song
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("01 - Hawker's song.mp3"));
            gameMusic.setLooping(true);
            gameMusic.setVolume(volume);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Falha ao carregar musica do jogo", e);
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
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.stop();
        }
        if (gameMusic != null && gameMusic.isPlaying()) {
            gameMusic.stop();
        }
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
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
            menuMusic = null;
        }
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.dispose();
            gameMusic = null;
        }
    }
}
