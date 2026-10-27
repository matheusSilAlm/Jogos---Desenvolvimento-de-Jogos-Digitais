package io.github.airhockey.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Paddle {
    public static final float RADIUS = 40f;
    public static final float SPEED = 820f;
    public static final float BOOST_SPEED_MULTIPLIER = 1.45f;
    public static final float BOOST_DURATION = 0.55f;
    public static final float BOOST_COOLDOWN = 2.2f;
    public static final float ACCEL_RATE = 28f;
    public static final float DECEL_RATE = 22f;

    public static final float POWERUP_DURATION = 10f;
    public static final float SIZE_FACTOR = 1.2f;
    public static final float SPEED_HIT_MULTIPLIER = 2f;

    private static final float TABLE_R = 0.07f, TABLE_G = 0.09f, TABLE_B = 0.16f;

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 targetVel = new Vector2();
    private final Vector2 moveDir = new Vector2();
    private final boolean leftSide;
    private final Color color;
    private boolean boosting;
    private float boostTimer = 0f;
    private float cooldownTimer = 0f;

    private float radius = RADIUS;
    private float speedTimer = 0f;
    private float sizeTimer = 0f;

    public Paddle(float startX, float startY, boolean leftSide, Color color) {
        this.position.set(startX, startY);
        this.leftSide = leftSide;
        this.color = new Color(color);
    }

    public void update(float dx, float dy, boolean boostInput, float dt, Table table) {
        // Lógica de Boost com Cooldown
        if (boostTimer > 0f) {
            boostTimer -= dt;
            boosting = true;
            if (boostTimer <= 0f) {
                boosting = false;
                cooldownTimer = BOOST_COOLDOWN;
            }
        } else {
            boosting = false;
            if (cooldownTimer > 0f) {
                cooldownTimer = Math.max(0f, cooldownTimer - dt);
            } else if (boostInput) {
                boostTimer = BOOST_DURATION;
                boosting = true;
            }
        }

        speedTimer = Math.max(0f, speedTimer - dt);
        sizeTimer = Math.max(0f, sizeTimer - dt);
        float targetRadius = sizeTimer > 0f ? RADIUS * SIZE_FACTOR : RADIUS;
        radius += (targetRadius - radius) * Math.min(1f, 12f * dt);

        float minX = leftSide ? Table.FIELD_LEFT + radius : Table.WORLD_WIDTH / 2f + radius;
        float maxX = leftSide ? Table.WORLD_WIDTH / 2f - radius : Table.FIELD_RIGHT - radius;
        float minY = Table.FIELD_BOTTOM + radius;
        float maxY = Table.FIELD_TOP - radius;

        // Se estiver colado na parede e forçando contra ela, cancela o componente para não diminuir a velocidade diagonal
        if (position.x <= minX + 1f && dx < 0f) dx = 0f;
        if (position.x >= maxX - 1f && dx > 0f) dx = 0f;
        if (position.y <= minY + 1f && dy < 0f) dy = 0f;
        if (position.y >= maxY - 1f && dy > 0f) dy = 0f;

        float targetSpeed = boosting ? SPEED * BOOST_SPEED_MULTIPLIER : SPEED;
        if (dx != 0f || dy != 0f) {
            moveDir.set(dx, dy).nor();
            targetVel.set(moveDir).scl(targetSpeed);
        } else {
            targetVel.setZero();
        }

        // Interpolação de velocidade (inércia e aceleração suaves)
        float lerpRate = (dx != 0f || dy != 0f) ? ACCEL_RATE : DECEL_RATE;
        velocity.lerp(targetVel, Math.min(1f, lerpRate * dt));

        position.mulAdd(velocity, dt);

        table.clampPaddle(this);

        // Anula inércia na direção em que está bloqueado pela parede
        if ((position.x <= minX && velocity.x < 0f) || (position.x >= maxX && velocity.x > 0f)) {
            velocity.x = 0f;
        }
        if ((position.y <= minY && velocity.y < 0f) || (position.y >= maxY && velocity.y > 0f)) {
            velocity.y = 0f;
        }
    }

    public void reset(float x, float y) {
        position.set(x, y);
        velocity.setZero();
        boosting = false;
        boostTimer = 0f;
        cooldownTimer = 0f;
    }

    public void activateSpeed() {
        speedTimer = POWERUP_DURATION;
    }

    public void activateSize() {
        sizeTimer = POWERUP_DURATION;
    }

    public boolean hasSpeedPowerUp() {
        return speedTimer > 0f;
    }

    public boolean hasSizePowerUp() {
        return sizeTimer > 0f;
    }

    public void render(ShapeRenderer shapes) {
        // 1. Sombra e aura neon
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.circle(position.x, position.y - 4f, radius + 4f, 48);

        // Indicadores de powerup: anéis que esvaziam conforme o tempo acaba
        if (speedTimer > 0f) {
            shapes.setColor(1.0f, 0.45f, 0.10f, 0.95f);
            shapes.arc(position.x, position.y, radius + 15f, 90f, 360f * speedTimer / POWERUP_DURATION, 48);
            shapes.setColor(TABLE_R, TABLE_G, TABLE_B, 1f);
            shapes.circle(position.x, position.y, radius + 11f, 48);
        }
        if (sizeTimer > 0f) {
            shapes.setColor(0.30f, 1.0f, 0.50f, 0.95f);
            shapes.arc(position.x, position.y, radius + 9f, 90f, 360f * sizeTimer / POWERUP_DURATION, 48);
            shapes.setColor(TABLE_R, TABLE_G, TABLE_B, 1f);
            shapes.circle(position.x, position.y, radius + 5f, 48);
        }

        // 1. Halo neon difuso multicamada (sensação real de luz neon brilhante)
        float boostExtra = boosting ? 14f : 0f;
        float boostAlpha = boosting ? 0.35f : 0f;

        // Aura externa ampla
        shapes.setColor(color.r, color.g, color.b, 0.08f + boostAlpha * 0.3f);
        shapes.circle(position.x, position.y, radius + 22f + boostExtra, 48);

        // Aura intermediária
        shapes.setColor(color.r, color.g, color.b, 0.18f + boostAlpha * 0.5f);
        shapes.circle(position.x, position.y, radius + 12f + boostExtra * 0.6f, 48);

        // Aura próxima intensa
        shapes.setColor(color.r, color.g, color.b, 0.38f + boostAlpha);
        shapes.circle(position.x, position.y, radius + 5f, 48);

        if (boosting) {
            shapes.setColor(1f, 1f, 1f, 0.45f);
            shapes.circle(position.x, position.y, radius + 10f, 48);
        }

        // 2. Base exterior escura / anel de impacto
        shapes.setColor(0.08f, 0.10f, 0.15f, 1f);
        shapes.circle(position.x, position.y, radius, 48);

        // 3. Tubo neon principal (cor saturada)
        shapes.setColor(color.r, color.g, color.b, 1f);
        shapes.circle(position.x, position.y, radius * 0.88f, 48);

        // Núcleo branco-quente do tubo neon (efeito que dá a sensação de luz fluorescente)
        shapes.setColor(color.r * 0.40f + 0.60f, color.g * 0.40f + 0.60f, color.b * 0.40f + 0.60f, 0.95f);
        shapes.circle(position.x, position.y, radius * 0.74f, 48);

        // 4. Tampa intermediária do pegador
        shapes.setColor(0.06f, 0.08f, 0.12f, 1f);
        shapes.circle(position.x, position.y, radius * 0.60f, 36);

        // 5. Manopla central (grip ergonômico arcade)
        shapes.setColor(0.18f, 0.22f, 0.30f, 1f);
        shapes.circle(position.x, position.y, radius * 0.38f, 28);

        // 6. Indicador visual de Boost e Recarga NA COR DO JOGADOR
        if (boosting) {
            // Boost ativo: núcleo incandescente branco na cor do jogador
            shapes.setColor(color.r, color.g, color.b, 0.85f);
            shapes.circle(position.x, position.y, radius * 0.30f, 24);
            shapes.setColor(1f, 1f, 1f, 1f);
            shapes.circle(position.x, position.y, radius * 0.20f, 20);
        } else if (cooldownTimer > 0f) {
            // Em recarga: medidor circular enchendo em sentido horário na cor do jogador
            float progress = 1f - (cooldownTimer / BOOST_COOLDOWN);
            shapes.setColor(color.r * 0.30f, color.g * 0.30f, color.b * 0.30f, 0.60f);
            shapes.circle(position.x, position.y, radius * 0.32f, 24);

            shapes.setColor(color.r, color.g, color.b, 0.95f);
            shapes.arc(position.x, position.y, radius * 0.32f, 90f, -360f * progress, 32);

            shapes.setColor(0.08f, 0.10f, 0.15f, 1f);
            shapes.circle(position.x, position.y, radius * 0.20f, 20);

            // LED central pulsando na cor do próprio jogador
            shapes.setColor(color.r, color.g, color.b, 0.30f + 0.60f * progress);
            shapes.circle(position.x, position.y, radius * 0.12f, 16);
        } else {
            // Boost 100% carregado e pronto: anel neon e LED NA COR PRIMÁRIA DO JOGADOR
            shapes.setColor(color.r, color.g, color.b, 0.95f);
            shapes.circle(position.x, position.y, radius * 0.32f, 32);

            shapes.setColor(0.08f, 0.10f, 0.15f, 1f);
            shapes.circle(position.x, position.y, radius * 0.20f, 20);

            // LED central na cor primária com núcleo branco
            shapes.setColor(color.r, color.g, color.b, 1f);
            shapes.circle(position.x, position.y, radius * 0.13f, 16);

            shapes.setColor(color.r * 0.40f + 0.60f, color.g * 0.40f + 0.60f, color.b * 0.40f + 0.60f, 1f);
            shapes.circle(position.x, position.y, radius * 0.06f, 12);
        }
    }

    public boolean isBoostReady() {
        return cooldownTimer <= 0f && !boosting;
    }

    public float getBoostCooldownProgress() {
        return cooldownTimer <= 0f ? 1f : 1f - (cooldownTimer / BOOST_COOLDOWN);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isLeftSide() {
        return leftSide;
    }

    public boolean isBoosting() {
        return boosting;
    }

    public Color getColor() {
        return color;
    }
}
