package io.github.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class EnduroGame extends ApplicationAdapter {
    // --- Velocidade ---
    private static final float MAX_SPEED = 300;       // velocidade de cruzeiro do jogador
    private static final float CRASH_SPEED = 60;      // velocidade apos bater
    private static final float SPEED_RECOVERY = 120;  // quanto recupera por segundo

    // --- Pontuacao ---
    private static final int DODGE_POINTS = 10;       // por carro ultrapassado
    private static final int CRASH_PENALTY = 50;      // perdido ao bater
    private static final int STREAK_FOR_BONUS = 10;   // desvios seguidos para ganhar 2x
    private static final float BONUS_DURATION = 8f;   // duracao do 2x (s)
    private static final float INVULNERABLE_TIME = 1.5f;

    // --- Trafego ---
    private static final float MIN_GAP = 30;          // distancia minima entre carros na mesma faixa
    private static final float FOLLOW_DISTANCE = 80;  // a partir daqui o carro de tras reduz
    private static final float SPAWN_MIN_Y = 800;
    private static final float SPAWN_MAX_Y = 950;

    // --- Item de bonus (circulo amarelo) ---
    private static final float PICKUP_RADIUS = 12;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private TrackRender trackRender;
    private Texture playerCarTexture, enemyCarTexture;
    private float playerX, playerY;
    private List<EnemyCar> enemyCars;

    private float playerSpeed = MAX_SPEED;
    private int score = 0;
    private int streak = 0;              // desvios seguidos sem bater
    private float bonusTimer = 0;        // > 0 => pontuacao 2x
    private float invulnerableTimer = 0; // evita varias colisoes seguidas

    private boolean pickupActive = false;
    private float pickupX, pickupY;      // pickupX relativo a pista (igual aos inimigos)
    private float pickupSpawnTimer = MathUtils.random(8f, 15f);

    private final Rectangle playerBounds = new Rectangle();
    private final Rectangle pickupBounds = new Rectangle();

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        float initialLeftX = 150;
        float initialRightX = 450;
        float trackWidth = 300;
        float screenWidth = Gdx.graphics.getWidth();
        trackRender = new TrackRender(initialLeftX, initialRightX, trackWidth, screenWidth);


        playerCarTexture = new Texture("car.png");
        enemyCarTexture = new Texture("enemy_car.png");


        playerX = 300;
        playerY = 50;

        enemyCars = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            EnemyCar car = new EnemyCar(enemyCarTexture);
            enemyCars.add(car);
            trySpawn(car, MathUtils.random(250, 900));
        }
    }

    /** Coloca o carro numa faixa aleatoria que tenha espaco livre na altura y. Se nao houver, fica inativo e tenta depois. */
    private void trySpawn(EnemyCar car, float y) {
        car.deactivate();
        int start = MathUtils.random(EnemyCar.LANES - 1);
        for (int i = 0; i < EnemyCar.LANES; i++) {
            int lane = (start + i) % EnemyCar.LANES;
            if (isLaneFree(lane, y, car)) {
                car.spawn(lane, y);
                return;
            }
        }
    }

    private boolean isLaneFree(int lane, float y, EnemyCar self) {
        float h = enemyCarTexture.getHeight();
        for (EnemyCar other : enemyCars) {
            if (other == self || !other.isActive() || other.getLane() != lane) continue;
            // sobreposicao vertical considerando a distancia minima
            if (y < other.getY() + h + MIN_GAP && y + h + MIN_GAP > other.getY()) return false;
        }
        return true;
    }

    /** Atualiza do carro mais alto para o mais baixo: assim o "carro da frente" de cada faixa ja foi movido. */
    private void updateEnemies(float delta) {
        enemyCars.sort((a, b) -> Float.compare(b.getY(), a.getY()));
        EnemyCar[] leaderInLane = new EnemyCar[EnemyCar.LANES];

        for (EnemyCar car : enemyCars) {
            if (!car.isActive()) {
                trySpawn(car, MathUtils.random(SPAWN_MIN_Y, SPAWN_MAX_Y));
                continue;
            }
            EnemyCar leader = leaderInLane[car.getLane()];
            car.follow(leader, FOLLOW_DISTANCE);
            car.update(delta, playerSpeed);
            car.keepBehind(leader, MIN_GAP);

            if (car.isOffScreen()) {
                trySpawn(car, MathUtils.random(SPAWN_MIN_Y, SPAWN_MAX_Y));
            } else {
                leaderInLane[car.getLane()] = car;
            }
        }
    }

    private void handelInput() {
        float playerSpeed = 200 * Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= playerSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += playerSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerY += playerSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            playerY -= playerSpeed;
        }
    }

    private int multiplier() {
        return bonusTimer > 0 ? 2 : 1;
    }

    private void activateBonus() {
        bonusTimer = BONUS_DURATION;
    }

    private void onCrash(EnemyCar enemy) {
        enemy.setResolved(true);          // carro batido nao gera ponto
        playerSpeed = CRASH_SPEED;        // desaceleracao
        score = Math.max(0, score - CRASH_PENALTY);
        streak = 0;
        bonusTimer = 0;                   // perde o 2x ao bater
        invulnerableTimer = INVULNERABLE_TIME;
    }

    private void onDodge(EnemyCar enemy) {
        enemy.setResolved(true);
        score += DODGE_POINTS * multiplier();
        streak++;
        if (streak % STREAK_FOR_BONUS == 0) {
            activateBonus();
        }
    }

    private void updateTimers(float delta) {
        playerSpeed = Math.min(MAX_SPEED, playerSpeed + SPEED_RECOVERY * delta);
        if (bonusTimer > 0) bonusTimer -= delta;
        if (invulnerableTimer > 0) invulnerableTimer -= delta;
    }

    private void updatePickup(float delta, float leftBound, float rightBound) {
        if (!pickupActive) {
            pickupSpawnTimer -= delta;
            if (pickupSpawnTimer <= 0) {
                pickupActive = true;
                pickupX = MathUtils.random(150 + PICKUP_RADIUS, 450 - PICKUP_RADIUS);
                pickupY = Gdx.graphics.getHeight() + PICKUP_RADIUS;
            }
            return;
        }
        pickupY -= playerSpeed * delta; // item parado na pista, desce conforme o jogador anda
        if (pickupY < -PICKUP_RADIUS) {
            resetPickup();
            return;
        }
        float drawX = MathUtils.clamp(pickupX + trackRender.getTrackOffset(), leftBound + PICKUP_RADIUS, rightBound - PICKUP_RADIUS);
        pickupBounds.set(drawX - PICKUP_RADIUS, pickupY - PICKUP_RADIUS, PICKUP_RADIUS * 2, PICKUP_RADIUS * 2);
        if (pickupBounds.overlaps(playerBounds)) {
            activateBonus();
            resetPickup();
        }
    }

    private void resetPickup() {
        pickupActive = false;
        pickupSpawnTimer = MathUtils.random(10f, 20f);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        trackRender.update(delta, playerSpeed);
        handelInput();
        updateTimers(delta);
        trackRender.draw(shapeRenderer);

        float leftBound = trackRender.getLeftBound();
        float rightBound = trackRender.getRightBound() - playerCarTexture.getWidth();
        playerX = MathUtils.clamp(playerX, leftBound, rightBound);
        playerBounds.set(playerX, playerY, playerCarTexture.getWidth(), playerCarTexture.getHeight());

        // Item de bonus
        updatePickup(delta, leftBound, trackRender.getRightBound());
        if (pickupActive) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.GOLD);
            shapeRenderer.circle(pickupBounds.x + PICKUP_RADIUS, pickupY, PICKUP_RADIUS);
            shapeRenderer.end();
        }

        batch.begin();
        batch.setColor(trackRender.getAmbient()); // escurece os carros a noite
        // Pisca enquanto invulneravel
        boolean visible = invulnerableTimer <= 0 || ((int) (invulnerableTimer * 10)) % 2 == 0;
        if (visible) {
            batch.draw(playerCarTexture, playerX, playerY);
        }

        updateEnemies(delta);
        for(EnemyCar enemyCar : enemyCars){
            if (!enemyCar.isActive()) continue;

            float ajustX = enemyCar.getX() + trackRender.getTrackOffset();
            ajustX = MathUtils.clamp(ajustX, leftBound, rightBound);
            batch.draw(enemyCarTexture, ajustX, enemyCar.getY());

            if (enemyCar.isResolved()) continue;

            // Colisao
            if (invulnerableTimer <= 0 && enemyCar.getBounds(ajustX).overlaps(playerBounds)) {
                onCrash(enemyCar);
            }
            // Desvio: inimigo passou totalmente abaixo do jogador
            else if (enemyCar.getY() + enemyCarTexture.getHeight() < playerY) {
                onDodge(enemyCar);
            }
        }

        // HUD
        batch.setColor(Color.WHITE);
        float top = Gdx.graphics.getHeight() - 10;
        font.setColor(Color.WHITE);
        font.draw(batch, "Pontos: " + score, 10, top);
        font.draw(batch, "Velocidade: " + (int) playerSpeed, 10, top - 20);
        font.draw(batch, "Sequencia: " + streak, 10, top - 40);
        if (bonusTimer > 0) {
            font.setColor(Color.GOLD);
            font.draw(batch, "2x PONTOS! " + String.format("%.1f", bonusTimer) + "s", 10, top - 60);
        }
        batch.end();

    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        playerCarTexture.dispose();
        enemyCarTexture.dispose();
    }
}
