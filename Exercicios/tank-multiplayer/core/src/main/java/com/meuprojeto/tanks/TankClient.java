package com.meuprojeto.tanks;

import com.badlogic.gdx.Game;

/** Classe principal do cliente LibGDX. Começa na tela de conexão. */
public class TankClient extends Game {
    private final String defaultHost;

    public TankClient(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    @Override
    public void create() {
        setScreen(new ConnectScreen(this, defaultHost, null));
    }
}
