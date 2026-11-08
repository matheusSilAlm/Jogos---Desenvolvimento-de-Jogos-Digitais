package com.meuprojeto.tanks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Tela inicial: o jogador digita seu nome e o IP do servidor antes de conectar.
 * Suporta foco com TAB, setas ou clique do mouse, e salva as preferências localmente.
 */
public class ConnectScreen extends ScreenAdapter {

    public static final int FIELD_NAME = 0;
    public static final int FIELD_HOST = 1;

    private final TankClient game;
    private final String message;           // erro da tentativa anterior (pode ser null)
    private final StringBuilder playerName;
    private final StringBuilder host;
    private int focusedField = FIELD_NAME;

    private final FitViewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shape;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 touchPoint = new Vector2();
    private float time;

    public ConnectScreen(TankClient game, String defaultHost, String defaultName, String message) {
        this.game = game;
        this.message = message;

        Preferences prefs = Gdx.app.getPreferences("tank_multiplayer");
        String initialName = defaultName;
        if (initialName == null || initialName.trim().isEmpty()) {
            initialName = prefs.getString("playerName", System.getProperty("user.name", "Jogador"));
        }
        if (initialName != null && initialName.length() > 16) {
            initialName = initialName.substring(0, 16);
        }
        this.playerName = new StringBuilder(initialName != null ? initialName : "Jogador");

        String initialHost = defaultHost;
        if (initialHost == null || initialHost.trim().isEmpty()) {
            initialHost = prefs.getString("host", "localhost");
        }
        this.host = new StringBuilder(initialHost != null ? initialHost : "localhost");

        viewport = new FitViewport(GameState.WORLD_WIDTH, GameState.WORLD_HEIGHT);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);
    }

    public ConnectScreen(TankClient game, String defaultHost, String message) {
        this(game, defaultHost, null, message);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.TAB) {
                    focusedField = (focusedField == FIELD_NAME) ? FIELD_HOST : FIELD_NAME;
                    return true;
                } else if (keycode == Input.Keys.UP) {
                    focusedField = FIELD_NAME;
                    return true;
                } else if (keycode == Input.Keys.DOWN) {
                    focusedField = FIELD_HOST;
                    return true;
                } else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                    connect();
                    return true;
                } else if (keycode == Input.Keys.BACKSPACE) {
                    StringBuilder target = (focusedField == FIELD_NAME) ? playerName : host;
                    if (target.length() > 0) {
                        target.setLength(target.length() - 1);
                    }
                    return true;
                } else if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char c) {
                if (focusedField == FIELD_NAME) {
                    boolean allowed = Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-';
                    if (c == ' ' && playerName.length() == 0) return true; // sem espaço no início
                    if (allowed && playerName.length() < 16) {
                        playerName.append(c);
                    }
                    return true;
                } else {
                    boolean allowed = Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == ':';
                    if (allowed && host.length() < 40) {
                        host.append(c);
                    }
                    return true;
                }
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPoint.set(screenX, screenY);
                viewport.unproject(touchPoint);

                float cx = GameState.WORLD_WIDTH / 2f;
                // Caixa Nome: x=[cx-220, cx+220], y=[390, 438]
                if (touchPoint.x >= cx - 220 && touchPoint.x <= cx + 220 && touchPoint.y >= 390 && touchPoint.y <= 438) {
                    focusedField = FIELD_NAME;
                    return true;
                }
                // Caixa Host: x=[cx-220, cx+220], y=[285, 333]
                if (touchPoint.x >= cx - 220 && touchPoint.x <= cx + 220 && touchPoint.y >= 285 && touchPoint.y <= 333) {
                    focusedField = FIELD_HOST;
                    return true;
                }
                return false;
            }
        });
    }

    private void connect() {
        String pName = playerName.toString().trim();
        if (pName.isEmpty()) pName = "Jogador";

        String h = host.toString().trim();
        if (h.isEmpty()) return;

        Preferences prefs = Gdx.app.getPreferences("tank_multiplayer");
        prefs.putString("playerName", pName);
        prefs.putString("host", h);
        prefs.flush();

        game.setScreen(new GameScreen(game, h, pName));
        dispose();
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0.10f, 0.13f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        float cx = GameState.WORLD_WIDTH / 2f;

        // Fileira decorativa com as 10 cores
        shape.setProjectionMatrix(viewport.getCamera().combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < GameState.MAX_PLAYERS; i++) {
            float x = cx - 270f + i * 60f;
            shape.setColor(TankColors.DARK[i]);
            shape.rect(x - 18, 590, 36, 36);
            shape.setColor(TankColors.BODY[i]);
            shape.rect(x - 12, 594, 24, 28);
        }

        // Fundo das caixas de input
        shape.setColor(focusedField == FIELD_NAME ? new Color(0.08f, 0.12f, 0.08f, 1f) : new Color(0.04f, 0.05f, 0.04f, 1f));
        shape.rect(cx - 220, 390, 440, 48);

        shape.setColor(focusedField == FIELD_HOST ? new Color(0.08f, 0.12f, 0.08f, 1f) : new Color(0.04f, 0.05f, 0.04f, 1f));
        shape.rect(cx - 220, 285, 440, 48);
        shape.end();

        // Bordas das caixas de input
        shape.begin(ShapeRenderer.ShapeType.Line);
        if (focusedField == FIELD_NAME) {
            shape.setColor(0.45f, 0.95f, 0.45f, 1f);
            shape.rect(cx - 221, 389, 442, 50);
        } else {
            shape.setColor(0.35f, 0.45f, 0.35f, 0.8f);
            shape.rect(cx - 220, 390, 440, 48);
        }

        if (focusedField == FIELD_HOST) {
            shape.setColor(0.45f, 0.95f, 0.45f, 1f);
            shape.rect(cx - 221, 284, 442, 50);
        } else {
            shape.setColor(0.35f, 0.45f, 0.35f, 0.8f);
            shape.rect(cx - 220, 285, 440, 48);
        }
        shape.end();

        // Textos
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.getData().setScale(2.8f);
        font.setColor(Color.WHITE);
        drawCentered("BATALHA DE TANQUES", cx, 535);

        // Campo 1: Nome
        font.getData().setScale(1.3f);
        font.setColor(focusedField == FIELD_NAME ? Color.WHITE : new Color(0.75f, 0.8f, 0.7f, 1f));
        drawCentered("Seu Nome / Apelido:", cx, 465);

        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
        boolean cursor = ((int) (time * 2)) % 2 == 0;
        String nameDisplay = playerName.toString() + (focusedField == FIELD_NAME && cursor ? "_" : " ");
        drawCentered(nameDisplay, cx, 422);

        // Campo 2: IP
        font.getData().setScale(1.3f);
        font.setColor(focusedField == FIELD_HOST ? Color.WHITE : new Color(0.75f, 0.8f, 0.7f, 1f));
        drawCentered("IP do Servidor:", cx, 360);

        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
        String hostDisplay = host.toString() + (focusedField == FIELD_HOST && cursor ? "_" : " ");
        drawCentered(hostDisplay, cx, 317);

        // Instruções
        font.getData().setScale(1.2f);
        font.setColor(0.75f, 0.8f, 0.7f, 1f);
        drawCentered("TAB / Clique: alternar campo   -   ENTER: jogar   -   ESC: sair", cx, 240);
        font.getData().setScale(1.1f);
        drawCentered("Até " + GameState.MAX_PLAYERS + " jogadores por partida", cx, 205);

        if (message != null) {
            font.getData().setScale(1.2f);
            font.setColor(1f, 0.4f, 0.35f, 1f);
            drawCentered(message, cx, 155);
        }
        batch.end();
    }

    private void drawCentered(String text, float cx, float y) {
        layout.setText(font, text);
        font.draw(batch, layout, cx - layout.width / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
    }
}
