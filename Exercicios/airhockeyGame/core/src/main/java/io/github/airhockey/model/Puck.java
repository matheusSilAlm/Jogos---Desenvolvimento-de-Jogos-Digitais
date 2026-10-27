package io.github.airhockey.model;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Puck {
    public static final float RADIUS = 16f;
    public static final float MAX_SPEED = 1500f;
    public static final float FRICTION = 0.55f;
    public static final float RESTITUTION = 1.0f;

    // Corrente de ar: aceleração suave e direção que varia devagar (nunca para por completo)
    public static final float AIR_DRIFT_ACCEL = 28f;      // px/s²
    public static final float AIR_TURN_RATE = 70f;        // graus/s (variação máx. da direção)
    public static final float MIN_DRIFT_SPEED = 18f;      // px/s, piso de velocidade

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    // Disco dourado: busca sozinho o gol mais próximo e vale 2 gols
    public static final float GOLD_HOMING_ACCEL = 380f;
    private boolean golden = false;
    private float time = 0f;
    private final Vector2 homing = new Vector2();

    private float driftAngle = MathUtils.random(360f);
    private float driftTurn = 0f;
    private float driftTurnTimer = 0f;

    public Puck(float startX, float startY) {
        reset(startX, startY);
    }

    public void reset(float x, float y) {
        position.set(x, y);
        velocity.setZero();
        golden = false;
        driftAngle = MathUtils.random(360f);
        driftTurn = 0f;
        driftTurnTimer = 0f;
    }

    /** Chamado quando o disco bate numa parede: a corrente passa a empurrá-lo para longe dela. */
    public void redirectDrift(float normalX, float normalY) {
        float base = MathUtils.atan2(normalY, normalX) * MathUtils.radiansToDegrees;
        driftAngle = base + MathUtils.random(-55f, 55f);
    }

    public void update(float dt) {
        time += dt;
        position.mulAdd(velocity, dt);

        float speed = velocity.len();
        if (speed > 0f) {
            float newSpeed = Math.max(0f, speed - FRICTION * speed * dt);
            if (newSpeed > 0f) {
                velocity.setLength(newSpeed);
            } else {
                velocity.setZero();
            }
        }

        applyAirDrift(dt);
        if (golden) applyHoming(dt);
    }

    private void applyHoming(float dt) {
        float tx = position.x < Table.WORLD_WIDTH / 2f ? Table.FIELD_LEFT - 30f : Table.FIELD_RIGHT + 30f;
        float ty = Table.WORLD_HEIGHT / 2f;
        homing.set(tx - position.x, ty - position.y).nor();
        velocity.mulAdd(homing, GOLD_HOMING_ACCEL * dt);
    }

    public boolean isGolden() {
        return golden;
    }

    public void setGolden(boolean golden) {
        this.golden = golden;
    }

    private void applyAirDrift(float dt) {
        // Escolhe periodicamente uma nova tendência de curva (random walk suave)
        driftTurnTimer -= dt;
        if (driftTurnTimer <= 0f) {
            driftTurn = MathUtils.random(-AIR_TURN_RATE, AIR_TURN_RATE);
            driftTurnTimer = MathUtils.random(0.6f, 1.6f);
        }
        driftAngle += driftTurn * dt;

        velocity.x += MathUtils.cosDeg(driftAngle) * AIR_DRIFT_ACCEL * dt;
        velocity.y += MathUtils.sinDeg(driftAngle) * AIR_DRIFT_ACCEL * dt;

        // Piso de velocidade: o disco nunca fica parado
        float speed = velocity.len();
        if (speed < MIN_DRIFT_SPEED) {
            if (speed < 0.001f) {
                velocity.set(MathUtils.cosDeg(driftAngle), MathUtils.sinDeg(driftAngle));
            }
            velocity.setLength(MIN_DRIFT_SPEED);
        }
    }

    public void render(ShapeRenderer shapes) {
        // 1. Sombra do disco
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.circle(position.x, position.y - 3f, RADIUS + 3f, 32);

        if (golden) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(time * 9f);
            shapes.setColor(1f, 0.80f, 0.10f, 0.14f + 0.10f * pulse);
            shapes.circle(position.x, position.y, RADIUS + 18f + 4f * pulse, 40);
            shapes.setColor(1f, 0.80f, 0.10f, 0.30f + 0.15f * pulse);
            shapes.circle(position.x, position.y, RADIUS + 8f, 32);

            shapes.setColor(0.35f, 0.22f, 0.02f, 1f);
            shapes.circle(position.x, position.y, RADIUS, 32);
            shapes.setColor(1f, 0.84f, 0.10f, 1f);
            shapes.circle(position.x, position.y, RADIUS * 0.84f, 32);
            shapes.setColor(1f, 0.62f, 0.05f, 1f);
            shapes.circle(position.x, position.y, RADIUS * 0.50f, 24);
            shapes.setColor(1f, 0.97f, 0.75f, 1f);
            shapes.circle(position.x, position.y, RADIUS * 0.22f, 16);
            return;
        }

        // 2. Halo neon fluorescente multicamada (brilho difuso)
        shapes.setColor(0.92f, 1.0f, 0.15f, 0.08f);
        shapes.circle(position.x, position.y, RADIUS + 18f, 32);

        shapes.setColor(0.92f, 1.0f, 0.15f, 0.22f);
        shapes.circle(position.x, position.y, RADIUS + 10f, 32);

        shapes.setColor(0.92f, 1.0f, 0.15f, 0.45f);
        shapes.circle(position.x, position.y, RADIUS + 4f, 32);

        // 3. Borda chanfrada escura de nylon
        shapes.setColor(0.08f, 0.10f, 0.12f, 1f);
        shapes.circle(position.x, position.y, RADIUS, 32);

        // 4. Superfície neon fluorescente
        shapes.setColor(0.92f, 1.0f, 0.15f, 1f);
        shapes.circle(position.x, position.y, RADIUS * 0.84f, 32);

        // 5. Núcleo fluorescente quente (hot core neon)
        shapes.setColor(0.98f, 1.0f, 0.65f, 0.95f);
        shapes.circle(position.x, position.y, RADIUS * 0.55f, 24);

        // 6. Centro brilhante incandescente
        shapes.setColor(1.0f, 1.0f, 0.95f, 1f);
        shapes.circle(position.x, position.y, RADIUS * 0.24f, 16);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getRadius() {
        return RADIUS;
    }
}
