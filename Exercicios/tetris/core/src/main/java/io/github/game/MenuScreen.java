package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

/**
 * Menu Inicial em 2 etapas com estetica sovietica:
 * 1. Tela de Titulo com Catedral e Recorde.
 * 2. Painel de Configuracao da Missao (Modo e Nivel) apos pressionar Espaco.
 */
public class MenuScreen extends ScreenAdapter {

    public enum MenuState {
        TITLE,
        CONFIG
    }

    private final TetrisGame game;
    private SpriteBatch batch;
    private Texture menuBg;
    private Texture selectBox;
    private BitmapFont font;

    private MenuState state = MenuState.TITLE;
    private int configFocus = 0; // 0 = Modo de jogo, 1 = Nivel inicial
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

        selectBox = new Texture("select_box.png");
        selectBox.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        try {
            font = new BitmapFont(Gdx.files.internal("retro_font.fnt"));
        } catch (Exception e) {
            font = new BitmapFont();
        }
        font.getData().setScale(1.0f);

        state = MenuState.TITLE;
        configFocus = 0;

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

        if (state == MenuState.TITLE) {
            renderTitleState();
        } else {
            renderConfigState();
        }

        // Indicador de mudo (canto inferior esquerdo)
        if (game != null && game.getAudioManager() != null && game.getAudioManager().isMuted()) {
            font.setColor(new Color(0.95f, 0.35f, 0.35f, 1f));
            font.draw(batch, "[AUDIO MUTADO - TECLA M]", 20, 25);
        }

        batch.end();
    }

    private void renderTitleState() {
        // Fundo sovietico limpo e espacoso
        batch.setColor(Color.WHITE);
        batch.draw(menuBg, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Recorde
        if (game != null && game.getPreferences() != null) {
            highScore = game.getPreferences().getInteger("highScore", 0);
        }
        font.setColor(new Color(0.98f, 0.85f, 0.20f, 1f));
        String recordText = "RECORDE: " + highScore;
        font.draw(batch, recordText, 0, 205, Constants.SCREEN_WIDTH, Align.center, false);

        // Botao piscante no centro da barra vermelha inferior (centro Y = 119)
        boolean visible = (blinkTimer % 0.8f) < 0.55f;
        if (visible) {
            font.setColor(new Color(1f, 0.95f, 0.60f, 1f));
            font.draw(batch, ">>> PRESSIONE ESPACO PARA JOGAR <<<",
                0, 125, Constants.SCREEN_WIDTH, Align.center, false);
        }
    }

    private void renderConfigState() {
        // Fundo escurecido para destaque do painel
        batch.setColor(0.35f, 0.35f, 0.40f, 1f);
        batch.draw(menuBg, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Console Sovietico de Configuracao (420x410 em x=40, y=110)
        batch.setColor(Color.WHITE);
        batch.draw(selectBox, 40, 110);

        boolean isChallenge = game != null && game.isChallengeMode();
        int lvl = game != null ? game.getStartingLevel() : 1;

        // SEÇÃO 1: MODO DE JOGO (x=40, largura=420)
        if (configFocus == 0) {
            font.setColor(new Color(1.0f, 0.95f, 0.35f, 1f));
            String prefix = isChallenge ? "◀  [ DESAFIO SOVIETICO ]  ▶" : "◀  [ MARATONA PADRAO ]  ▶";
            font.draw(batch, prefix, 40, 404, 420, Align.center, false);
        } else {
            font.setColor(new Color(0.80f, 0.85f, 0.90f, 0.85f));
            String prefix = isChallenge ? "DESAFIO SOVIETICO" : "MARATONA PADRAO";
            font.draw(batch, prefix, 40, 404, 420, Align.center, false);
        }

        font.setColor(new Color(0.70f, 0.75f, 0.90f, 1f));
        String modeDesc = isChallenge
            ? "Inicie com entulho e escave a matriz!"
            : "Matriz limpa. Sobreviva e alcance o recorde!";
        font.draw(batch, modeDesc, 40, 380, 420, Align.center, false);

        // SEÇÃO 2: NÍVEL INICIAL (x=40, largura=420)
        if (configFocus == 1) {
            font.setColor(new Color(1.0f, 0.95f, 0.35f, 1f));
            font.draw(batch, "◀  NIVEL INICIAL: " + lvl + "  ▶", 40, 281, 420, Align.center, false);
        } else {
            font.setColor(new Color(0.80f, 0.85f, 0.90f, 0.85f));
            font.draw(batch, "NIVEL INICIAL: " + lvl, 40, 281, 420, Align.center, false);
        }

        font.setColor(new Color(0.70f, 0.75f, 0.90f, 1f));
        font.draw(batch, getSpeedDescription(lvl), 40, 257, 420, Align.center, false);

        // BOTÃO DE CONFIRMAÇÃO PISCANTE
        boolean visible = (blinkTimer % 0.8f) < 0.55f;
        if (visible) {
            font.setColor(new Color(1f, 0.95f, 0.55f, 1f));
            font.draw(batch, ">>> PRESSIONE ESPACO PARA INICIAR <<<",
                40, 184, 420, Align.center, false);
        }
    }

    private String getSpeedDescription(int lvl) {
        if (lvl <= 2) return "Velocidade: Cadencia suave (Nivel " + lvl + ")";
        if (lvl <= 5) return "Velocidade: Ritmo moderado (Nivel " + lvl + ")";
        if (lvl <= 8) return "Velocidade: Ritmo acelerado (Nivel " + lvl + ")";
        return "Velocidade: FRENESI SOVIETICO (Nivel " + lvl + ")";
    }

    private void handleInput() {
        if (state == MenuState.TITLE) {
            handleTitleInput();
        } else {
            handleConfigInput();
        }
    }

    private void handleTitleInput() {
        // Avancar para selecao de missao
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            state = MenuState.CONFIG;
            configFocus = 0;
            if (game != null && game.getAudioManager() != null) {
                game.getAudioManager().playRotate();
            }
            return;
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

    private void handleConfigInput() {
        // Alternar foco entre Modo (0) e Nivel (1)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            configFocus = (configFocus == 0) ? 1 : 0;
            if (game != null && game.getAudioManager() != null) {
                game.getAudioManager().playMove();
            }
            return;
        }

        // Alterar valor do item focado
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (configFocus == 0) {
                if (game != null) {
                    game.setChallengeMode(!game.isChallengeMode());
                    game.getAudioManager().playRotate();
                }
            } else {
                if (game != null) {
                    game.setStartingLevel(game.getStartingLevel() - 1);
                    game.getAudioManager().playMove();
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (configFocus == 0) {
                if (game != null) {
                    game.setChallengeMode(!game.isChallengeMode());
                    game.getAudioManager().playRotate();
                }
            } else {
                if (game != null) {
                    game.setStartingLevel(game.getStartingLevel() + 1);
                    game.getAudioManager().playMove();
                }
            }
        }

        // TAB alterna modo rapidamente
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            if (game != null) {
                game.setChallengeMode(!game.isChallengeMode());
                game.getAudioManager().playRotate();
            }
        }

        // Iniciar partida
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (game != null) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playTetris();
                }
                game.startGame();
            }
            return;
        }

        // Voltar a tela de titulo
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = MenuState.TITLE;
            if (game != null && game.getAudioManager() != null) {
                game.getAudioManager().playMove();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (game != null && game.getAudioManager() != null) {
                game.getAudioManager().toggleMute();
            }
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (menuBg != null) menuBg.dispose();
        if (selectBox != null) selectBox.dispose();
        if (font != null) font.dispose();
    }
}
