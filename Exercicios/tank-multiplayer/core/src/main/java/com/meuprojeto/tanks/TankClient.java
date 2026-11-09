package com.meuprojeto.tanks;

import com.badlogic.gdx.Game;

/** Classe principal do cliente LibGDX. Começa na tela de conexão. */
public class TankClient extends Game {
    private final String defaultHost;
    private AudioManager audio;

    public TankClient(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    @Override
    public void create() {
        audio = new AudioManager();
        setScreen(new ConnectScreen(this, defaultHost, null));
    }

    public AudioManager getAudio() {
        return audio;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audio != null) audio.dispose();
    }
}
