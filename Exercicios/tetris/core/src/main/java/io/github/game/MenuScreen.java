package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

/**
 * Tela de Menu Inicial no estilo Sovietico do Tetris X68000 (1987-1988).
 * Toca '04 - Northern countries\' Mambo.mp3' e apresenta instrucoes completas.
 */
public class MenuScreen extends ScreenAdapter {
    private final TetrisGame game;
    private SpriteBatch batch;
    private Texture menuBg;
    private BitmapFont font;
    private GlyphLayout layout;

    private float blinkTimer = 0f;
    private int highScore = 0;

    public MenuScreen(TetrisGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        menuBg = new Texture("menu_bg.png");
        menuBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        try {
            font = new BitmapFont(Gdx.files.internal("retro_font.fnt"));
        } catch (Exception e) {
            font = new BitmapFont();
        }
        font.getData().setScale(1.0f);
        layout = new GlyphLayout();

        if (game != null && game.getPreferences() != null) {
            highScore = game.getPreferences().getInteger("highScore", 0);
        }

        if (game != null && game.getAudioManager() != null) {
            game.getAudioManager().playMenuMusic();
        }
    }

    @Override
    public void render(float delta) {
        blinkTimer += delta;
        handleInput();

        Gdx.gl.glClearColor(0.05f, 0.06f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Fundo tematico sovietico (Moscou / Catedral de Sao Basilio / Kremlin)
        batch.setColor(Color.WHITE);
        batch.draw(menuBg, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Recorde / High Score no topo do painel
        font.setColor(new Color(0.98f, 0.85f, 0.20f, 1f));
        String recordText = "RECORDE: " + highScore;
        font.draw(batch, recordText, 0, 215, Constants.SCREEN_WIDTH, Align.center, false);

        // Botao piscante para iniciar
        boolean visible = (blinkTimer % 0.8f) < 0.55f;
        if (visible) {
            font.setColor(new Color(1f, 0.95f, 0.60f, 1f));
            font.draw(batch, ">>> PRESSIONE ESPACO PARA JOGAR <<<",
                0, 163, Constants.SCREEN_WIDTH, Align.center, false);
        }

        // Indicador de som / mudo
        if (game != null && game.getAudioManager() != null && game.getAudioManager().isMuted()) {
            font.setColor(new Color(0.95f, 0.35f, 0.35f, 1f));
            font.draw(batch, "[AUDIO MUTADO - TECLA M]", 20, 25);
        }

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (game != null) {
                game.startGame();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (game != null && game.getAudioManager() != null) {
                game.getAudioManager().toggleMute();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (menuBg != null) menuBg.dispose();
        if (font != null) font.dispose();
    }
}
