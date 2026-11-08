package com.meuprojeto.tanks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Tela da partida.
 *
 * Rede (mesmo modelo da base do Pong):
 *   - Thread "NetworkReader": lê snapshots do servidor e publica o mais recente
 *     em {@link #latest} (referência volatile, troca atômica).
 *   - Thread de render (LibGDX): lê teclado/mouse, manda o PlayerInput e desenha
 *     o último snapshot recebido.
 */
public class GameScreen extends ScreenAdapter {

    public static final int SERVER_PORT = 12345;
    private static final int CONNECT_TIMEOUT_MS = 5000;

    private final TankClient game;
    private final String host;
    private final String playerName;

    // ---- rede ----
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private volatile int playerId = -1;
    private volatile GameState latest;
    private volatile String error;          // != null => conexão falhou/caiu

    // ---- render ----
    private final FitViewport viewport;
    private final ShapeRenderer shape;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 mouse = new Vector2();

    // ---- input ----
    private final PlayerInput input = new PlayerInput();
    private int heldDirection = GameState.DIR_NONE;
    private static final int[][] DIR_KEYS = {
        { Input.Keys.W, Input.Keys.UP },
        { Input.Keys.D, Input.Keys.RIGHT },
        { Input.Keys.S, Input.Keys.DOWN },
        { Input.Keys.A, Input.Keys.LEFT },
    };

    // ---- efeitos visuais (só no cliente, deduzidos comparando snapshots) ----
    private GameState lastProcessed;
    private final boolean[] prevAlive = new boolean[GameState.MAX_PLAYERS];
    private final int[] prevHp = new int[GameState.MAX_PLAYERS];
    private final float[] hitFlash = new float[GameState.MAX_PLAYERS];
    private final List<float[]> explosions = new ArrayList<float[]>(); // {x, y, tempo}
    private static final float EXPLOSION_TIME = 0.6f;
    private float time;

    public GameScreen(TankClient game, String host, String playerName) {
        this.game = game;
        this.host = host;
        this.playerName = (playerName != null && !playerName.trim().isEmpty()) ? playerName.trim() : "Jogador";

        viewport = new FitViewport(GameState.WORLD_WIDTH, GameState.WORLD_HEIGHT);
        shape = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);

        Thread t = new Thread(this::connectAndRead, "NetworkReader");
        t.setDaemon(true);
        t.start();
    }

    public GameScreen(TankClient game, String host) {
        this(game, host, "Jogador");
    }

    // =========================================================================
    // REDE
    // =========================================================================

    private void connectAndRead() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, SERVER_PORT), CONNECT_TIMEOUT_MS);
            socket.setTcpNoDelay(true);
            in  = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            int id = in.readInt();
            if (id < 0) {
                error = "Servidor cheio (" + GameState.MAX_PLAYERS + "/" + GameState.MAX_PLAYERS + ")";
                closeSocket();
                return;
            }
            playerId = id;

            // Envia nome do jogador no handshake
            out.writeUTF(playerName);
            out.flush();

            while (true) {
                latest = GameState.read(in);
            }
        } catch (IOException e) {
            if (error == null) {
                error = playerId < 0
                    ? "Falha ao conectar em " + host + ":" + SERVER_PORT
                    : "Conexão com o servidor perdida";
            }
            closeSocket();
        }
    }

    private void sendInput() {
        try {
            input.write(out);
            out.flush();
        } catch (IOException e) {
            if (error == null) error = "Conexão com o servidor perdida";
        }
    }

    private void closeSocket() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    private void backToMenu(String message) {
        closeSocket();
        game.setScreen(new ConnectScreen(game, host, playerName, message));
        dispose();
    }

    // =========================================================================
    // LOOP
    // =========================================================================

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0.05f, 0.06f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        shape.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        if (error != null) { backToMenu(error); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { backToMenu(null); return; }

        GameState s = latest;
        if (s == null || playerId < 0) {
            drawCenterMessage("Conectando a " + host + "...");
            return;
        }

        Tank me = s.tanks[playerId];
        readInput(me);
        sendInput();
        updateEffects(s, delta);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawField();
        for (Tank t : s.tanks) if (t.active && !t.alive) drawWreck(t);
        if (me.alive) drawTrajectory(me);
        for (Tank t : s.tanks) if (t.active && t.alive) drawTank(t, t.id == playerId);
        drawBullets(s);
        drawExplosions();
        for (Tank t : s.tanks) if (t.active && t.alive) drawHpPips(t);
        if (me.alive) drawReloadBar(me);
        shape.end();

        drawShieldRings(s);
        drawText(s, me);
    }

    private void readInput(Tank me) {
        // Direção: só cardinal. Se apertar duas teclas, vale a última pressionada.
        for (int d = 0; d < 4; d++) {
            if (Gdx.input.isKeyJustPressed(DIR_KEYS[d][0]) || Gdx.input.isKeyJustPressed(DIR_KEYS[d][1])) {
                heldDirection = d;
            }
        }
        if (heldDirection == GameState.DIR_NONE || !isDirHeld(heldDirection)) {
            heldDirection = GameState.DIR_NONE;
            for (int d = 0; d < 4; d++) {
                if (isDirHeld(d)) { heldDirection = d; break; }
            }
        }
        input.direction = heldDirection;

        // Mira: torre aponta para o mouse
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
        input.aimAngle = (float) Math.atan2(mouse.y - me.y, mouse.x - me.x);

        input.fire = Gdx.input.isButtonPressed(Input.Buttons.LEFT) || Gdx.input.isKeyPressed(Input.Keys.SPACE);
    }

    private boolean isDirHeld(int d) {
        return Gdx.input.isKeyPressed(DIR_KEYS[d][0]) || Gdx.input.isKeyPressed(DIR_KEYS[d][1]);
    }

    /** Detecta acertos e mortes comparando o snapshot novo com o anterior. */
    private void updateEffects(GameState s, float delta) {
        if (s != lastProcessed) {
            for (Tank t : s.tanks) {
                int i = t.id;
                if (t.active) {
                    if (prevAlive[i] && !t.alive) explosions.add(new float[] { t.x, t.y, 0f });
                    if (prevAlive[i] && t.alive && t.hp < prevHp[i]) hitFlash[i] = 0.15f;
                }
                prevAlive[i] = t.active && t.alive;
                prevHp[i] = t.hp;
            }
            lastProcessed = s;
        }

        for (int i = 0; i < hitFlash.length; i++) hitFlash[i] = Math.max(0f, hitFlash[i] - delta);
        Iterator<float[]> it = explosions.iterator();
        while (it.hasNext()) {
            float[] e = it.next();
            e[2] += delta;
            if (e[2] >= EXPLOSION_TIME) it.remove();
        }
    }

    // =========================================================================
    // DESENHO (ShapeRenderer)
    // =========================================================================

    private void drawField() {
        shape.setColor(0.16f, 0.20f, 0.13f, 1f);
        shape.rect(0, 0, GameState.WORLD_WIDTH, GameState.WORLD_HEIGHT);

        shape.setColor(0.19f, 0.24f, 0.16f, 1f);
        for (float x = 40; x < GameState.WORLD_WIDTH; x += 40) shape.rect(x - 1, 0, 2, GameState.WORLD_HEIGHT);
        for (float y = 40; y < GameState.WORLD_HEIGHT; y += 40) shape.rect(0, y - 1, GameState.WORLD_WIDTH, 2);

        // borda do campo
        shape.setColor(0.35f, 0.42f, 0.28f, 1f);
        float b = 4f;
        shape.rect(0, 0, GameState.WORLD_WIDTH, b);
        shape.rect(0, GameState.WORLD_HEIGHT - b, GameState.WORLD_WIDTH, b);
        shape.rect(0, 0, b, GameState.WORLD_HEIGHT);
        shape.rect(GameState.WORLD_WIDTH - b, 0, b, GameState.WORLD_HEIGHT);
    }

    private void drawTank(Tank t, boolean isMe) {
        float h = GameState.TANK_HALF, size = GameState.TANK_SIZE;
        float x = t.x, y = t.y;
        Color body = hitFlash[t.id] > 0 ? Color.WHITE : TankColors.BODY[t.id];
        Color dark = TankColors.DARK[t.id];
        boolean vertical = t.facing == GameState.DIR_UP || t.facing == GameState.DIR_DOWN;

        // sombra
        shape.setColor(0f, 0f, 0f, 0.3f);
        shape.rect(x - h + 3, y - h - 3, size, size);

        // esteiras (orientadas conforme a direção do corpo)
        float tread = 9f;
        shape.setColor(0.12f, 0.12f, 0.12f, 1f);
        if (vertical) {
            shape.rect(x - h, y - h, tread, size);
            shape.rect(x + h - tread, y - h, tread, size);
        } else {
            shape.rect(x - h, y - h, size, tread);
            shape.rect(x - h, y + h - tread, size, tread);
        }
        shape.setColor(0.25f, 0.25f, 0.25f, 1f);
        for (float k = 3; k < size - 2; k += 6) {
            if (vertical) {
                shape.rect(x - h + 1, y - h + k, tread - 2, 2);
                shape.rect(x + h - tread + 1, y - h + k, tread - 2, 2);
            } else {
                shape.rect(x - h + k, y - h + 1, 2, tread - 2);
                shape.rect(x - h + k, y + h - tread + 1, 2, tread - 2);
            }
        }

        // casco
        shape.setColor(body);
        if (vertical) shape.rect(x - h + tread - 2, y - h + 3, size - 2 * tread + 4, size - 6);
        else          shape.rect(x - h + 3, y - h + tread - 2, size - 6, size - 2 * tread + 4);

        // marca da frente do casco
        shape.setColor(dark);
        float fx = x + GameState.DIR_X[t.facing] * (h - 7);
        float fy = y + GameState.DIR_Y[t.facing] * (h - 7);
        shape.rect(fx - 4, fy - 4, 8, 8);

        // torre + cano (o meu usa a mira local, sem esperar o servidor)
        float angle = isMe ? input.aimAngle : t.turretAngle;
        float tipX = x + (float) Math.cos(angle) * GameState.BARREL_LENGTH;
        float tipY = y + (float) Math.sin(angle) * GameState.BARREL_LENGTH;
        shape.setColor(0.10f, 0.10f, 0.10f, 1f);
        shape.rectLine(x, y, tipX, tipY, 7f);
        shape.setColor(dark);
        shape.rectLine(x, y, tipX, tipY, 5f);
        shape.circle(x, y, 10f, 20);
        shape.setColor(body);
        shape.circle(x, y, 7f, 20);
    }

    private void drawWreck(Tank t) {
        float h = GameState.TANK_HALF;
        shape.setColor(0.08f, 0.08f, 0.07f, 0.85f);
        shape.rect(t.x - h, t.y - h, GameState.TANK_SIZE, GameState.TANK_SIZE);
        shape.setColor(TankColors.DARK[t.id].r * 0.5f, TankColors.DARK[t.id].g * 0.5f, TankColors.DARK[t.id].b * 0.5f, 1f);
        shape.circle(t.x, t.y, 8f, 16);
    }

    /**
     * Linha tracejada mostrando por onde o tiro vai passar, até o alcance máximo
     * (ou até a borda do campo, onde o projétil some).
     */
    private void drawTrajectory(Tank me) {
        float cos = (float) Math.cos(input.aimAngle);
        float sin = (float) Math.sin(input.aimAngle);
        float sx = me.x + cos * GameState.BARREL_LENGTH;
        float sy = me.y + sin * GameState.BARREL_LENGTH;

        float length = GameState.BULLET_RANGE;
        if (cos > 0.0001f)  length = Math.min(length, (GameState.WORLD_WIDTH - sx) / cos);
        if (cos < -0.0001f) length = Math.min(length, -sx / cos);
        if (sin > 0.0001f)  length = Math.min(length, (GameState.WORLD_HEIGHT - sy) / sin);
        if (sin < -0.0001f) length = Math.min(length, -sy / sin);
        length = Math.max(0f, length);

        boolean ready = me.reload <= 0f;
        Color c = TankColors.BODY[me.id];
        if (ready) shape.setColor(c.r, c.g, c.b, 0.75f);
        else       shape.setColor(1f, 1f, 1f, 0.22f);

        float dash = 10f, gap = 8f;
        for (float d = 0; d < length; d += dash + gap) {
            float e = Math.min(d + dash, length);
            shape.rectLine(sx + cos * d, sy + sin * d, sx + cos * e, sy + sin * e, 2.5f);
        }

        // marcador do ponto final (alcance máximo)
        float ex = sx + cos * length, ey = sy + sin * length;
        shape.circle(ex, ey, 5f, 14);
    }

    private void drawBullets(GameState s) {
        for (Bullet b : s.bullets) {
            shape.setColor(0.05f, 0.05f, 0.05f, 1f);
            shape.circle(b.x, b.y, GameState.BULLET_RADIUS + 1.5f, 12);
            shape.setColor(TankColors.BODY[b.owner]);
            shape.circle(b.x, b.y, GameState.BULLET_RADIUS, 12);
        }
    }

    private void drawExplosions() {
        for (float[] e : explosions) {
            float p = e[2] / EXPLOSION_TIME;   // 0..1
            float a = 1f - p;
            shape.setColor(1f, 0.45f, 0.1f, a * 0.8f);
            shape.circle(e[0], e[1], 12f + 50f * p, 28);
            shape.setColor(1f, 0.9f, 0.3f, a);
            shape.circle(e[0], e[1], 6f + 25f * p, 24);
        }
    }

    private void drawHpPips(Tank t) {
        float w = 9f, gap = 3f;
        float total = GameState.TANK_MAX_HP * w + (GameState.TANK_MAX_HP - 1) * gap;
        float x0 = t.x - total / 2f;
        float y0 = t.y + GameState.TANK_HALF + 6f;
        for (int i = 0; i < GameState.TANK_MAX_HP; i++) {
            if (i < t.hp) shape.setColor(0.35f, 0.95f, 0.35f, 1f);
            else          shape.setColor(0.3f, 0.1f, 0.1f, 1f);
            shape.rect(x0 + i * (w + gap), y0, w, 4f);
        }
    }

    private void drawReloadBar(Tank me) {
        float w = 40f, hgt = 5f;
        float x0 = me.x - w / 2f;
        float y0 = me.y - GameState.TANK_HALF - 11f;
        float progress = 1f - me.reload / GameState.RELOAD_TIME;
        shape.setColor(0f, 0f, 0f, 0.6f);
        shape.rect(x0 - 1, y0 - 1, w + 2, hgt + 2);
        if (progress >= 1f) shape.setColor(TankColors.BODY[me.id]);
        else                shape.setColor(0.8f, 0.8f, 0.8f, 1f);
        shape.rect(x0, y0, w * Math.min(1f, progress), hgt);
    }

    /** Anel piscando nos tanques com proteção de nascimento. */
    private void drawShieldRings(GameState s) {
        shape.begin(ShapeRenderer.ShapeType.Line);
        float pulse = 0.5f + 0.5f * (float) Math.sin(time * 12f);
        for (Tank t : s.tanks) {
            if (!t.active || !t.alive || t.shield <= 0f) continue;
            shape.setColor(0.6f, 0.9f, 1f, 0.4f + 0.5f * pulse);
            shape.circle(t.x, t.y, GameState.TANK_HALF + 8f, 32);
            shape.circle(t.x, t.y, GameState.TANK_HALF + 9f, 32);
        }
        // anel indicando "este é você"
        Tank me = s.tanks[playerId];
        if (me.alive) {
            Color c = TankColors.BODY[me.id];
            shape.setColor(c.r, c.g, c.b, 0.5f);
            shape.circle(me.x, me.y, GameState.TANK_HALF + 14f, 36);
        }
        shape.end();
    }

    // =========================================================================
    // TEXTO / HUD
    // =========================================================================

    private void drawText(GameState s, Tank me) {
        // Quadradinhos de cor para o HUD (precisam do ShapeRenderer)
        Tank[] ranking = sortedByScore(s);
        float boardWidth = 280f;
        float boardX = GameState.WORLD_WIDTH - boardWidth - 10f, boardTop = GameState.WORLD_HEIGHT - 20f;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.45f);
        shape.rect(boardX - 10, boardTop - 34 - ranking.length * 22f, boardWidth, 44 + ranking.length * 22f);
        shape.rect(10, GameState.WORLD_HEIGHT - 92, 320, 82);
        for (int i = 0; i < ranking.length; i++) {
            shape.setColor(TankColors.BODY[ranking[i].id]);
            shape.rect(boardX, boardTop - 46 - i * 22f, 12, 12);
        }
        shape.setColor(TankColors.BODY[me.id]);
        shape.rect(22, GameState.WORLD_HEIGHT - 38, 16, 16);
        shape.end();

        batch.begin();
        font.getData().setScale(1f);

        // rótulos sobre os tanques
        for (Tank t : s.tanks) {
            if (!t.active || !t.alive) continue;
            String displayName = (t.name != null && !t.name.isEmpty()) ? t.name : ("J" + (t.id + 1));
            String label = t.id == playerId ? displayName + " (VOCÊ)" : displayName;
            font.setColor(t.id == playerId ? Color.YELLOW : new Color(1f, 1f, 1f, 0.85f));
            layout.setText(font, label);
            font.draw(batch, layout, t.x - layout.width / 2f, t.y + GameState.TANK_HALF + 26f);
        }

        // placar
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        font.draw(batch, "PLACAR  (" + ranking.length + "/" + GameState.MAX_PLAYERS + ")", boardX, boardTop - 4);
        font.getData().setScale(1f);
        for (int i = 0; i < ranking.length; i++) {
            Tank t = ranking[i];
            float y = boardTop - 35 - i * 22f;
            font.setColor(t.id == playerId ? Color.YELLOW : Color.WHITE);
            String displayName = (t.name != null && !t.name.isEmpty()) ? t.name : ("J" + (t.id + 1));
            if (displayName.length() > 14) displayName = displayName.substring(0, 14);
            font.draw(batch, displayName, boardX + 20, y);
            font.draw(batch, t.kills + " / " + t.deaths, boardX + 195, y);
        }

        // HUD do jogador
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        String myName = (me.name != null && !me.name.isEmpty()) ? me.name : ("Jogador " + (me.id + 1));
        font.draw(batch, myName + " - " + TankColors.NAMES[me.id], 48, GameState.WORLD_HEIGHT - 22);
        font.getData().setScale(1f);
        font.draw(batch, "Abates: " + me.kills + "   Mortes: " + me.deaths, 22, GameState.WORLD_HEIGHT - 50);
        String shot;
        if (!me.alive)            shot = "Tiro: --";
        else if (me.reload <= 0f) shot = "Tiro: PRONTO";
        else                      shot = String.format("Tiro: recarregando %.1fs", me.reload);
        font.setColor(me.alive && me.reload <= 0f ? Color.GREEN : Color.LIGHT_GRAY);
        font.draw(batch, shot, 22, GameState.WORLD_HEIGHT - 72);

        // controles
        font.setColor(0.7f, 0.75f, 0.65f, 0.8f);
        font.draw(batch, "WASD/Setas: mover   Mouse: mirar   Clique/Espaço: atirar   ESC: sair", 14, 22);

        // morto
        if (!me.alive) {
            font.getData().setScale(2.5f);
            font.setColor(1f, 0.35f, 0.3f, 1f);
            drawCentered("DESTRUÍDO!", GameState.WORLD_HEIGHT / 2f + 40);
            font.getData().setScale(1.5f);
            font.setColor(Color.WHITE);
            drawCentered(String.format("Renascendo em %.1fs", Math.max(0f, me.respawnTimer)), GameState.WORLD_HEIGHT / 2f);
        }
        font.getData().setScale(1f);
        batch.end();
    }

    private Tank[] sortedByScore(GameState s) {
        List<Tank> list = new ArrayList<Tank>();
        for (Tank t : s.tanks) if (t.active) list.add(t);
        Tank[] arr = list.toArray(new Tank[0]);
        Arrays.sort(arr, new Comparator<Tank>() {
            @Override
            public int compare(Tank a, Tank b) {
                if (a.kills != b.kills) return b.kills - a.kills;
                return a.deaths - b.deaths;
            }
        });
        return arr;
    }

    private void drawCenterMessage(String text) {
        batch.begin();
        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
        drawCentered(text, GameState.WORLD_HEIGHT / 2f);
        font.getData().setScale(1f);
        batch.end();
    }

    private void drawCentered(String text, float y) {
        layout.setText(font, text);
        font.draw(batch, layout, GameState.WORLD_WIDTH / 2f - layout.width / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        closeSocket();
        shape.dispose();
        batch.dispose();
        font.dispose();
    }
}
