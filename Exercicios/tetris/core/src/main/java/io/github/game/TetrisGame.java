package io.github.game;

import com.badlogic.gdx.Game;

/** Classe principal do jogo. Delega tudo para a GameScreen. */
public class TetrisGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
