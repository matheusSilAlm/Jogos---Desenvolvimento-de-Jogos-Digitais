package io.github.airhockey.powerup;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.airhockey.fx.Effects;
import io.github.airhockey.model.Paddle;
import io.github.airhockey.model.Puck;
import io.github.airhockey.model.Table;

/** Gerencia surgimento, coleta, aplicação e desenho dos powerups no campo. */
public class PowerUpManager {

    public static class PowerUp {
        public PowerUpType type;
        public float x, y, age;
        public boolean left;
    }

    public static final float PICKUP_RADIUS = 24f;
    private static final float LIFETIME = 12f;
    private static final float BLINK_TIME = 3f;
    private static final float MIN_SPAWN = 7f;
    private static final float MAX_SPAWN = 12f;

    private final Array<PowerUp> items = new Array<>(false, 4);
    private final Effects effects;
    private float spawnTimer = MathUtils.random(4f, 7f);

    public PowerUpManager(Effects effects) {
        this.effects = effects;
    }

    public void clear() {
        items.clear();
        spawnTimer = MathUtils.random(4f, 7f);
    }

    /** Powerup disponível em um dos lados do campo, ou null. */
    public PowerUp findOnSide(boolean left) {
        for (PowerUp p : items) {
            if (p.left == left) return p;
        }
        return null;
    }

    public void update(float dt, Paddle p1, Paddle p2, Puck puck) {
        for (int i = items.size - 1; i >= 0; i--) {
            PowerUp item = items.get(i);
            item.age += dt;
            if (item.age >= LIFETIME) {
                items.removeIndex(i);
                continue;
            }
            if (touches(p1, item)) {
                collect(item, p1, puck);
                items.removeIndex(i);
            } else if (touches(p2, item)) {
                collect(item, p2, puck);
                items.removeIndex(i);
            }
        }

        spawnTimer -= dt;
        if (spawnTimer <= 0f) {
            trySpawn(p1, p2, puck);
            spawnTimer = MathUtils.random(MIN_SPAWN, MAX_SPAWN);
        }
    }

    private boolean touches(Paddle paddle, PowerUp item) {
        float reach = paddle.getRadius() + PICKUP_RADIUS;
        return paddle.getPosition().dst2(item.x, item.y) < reach * reach;
    }

    private void collect(PowerUp item, Paddle paddle, Puck puck) {
        switch (item.type) {
            case SPEED:
                paddle.activateSpeed();
                break;
            case SIZE:
                paddle.activateSize();
                break;
            case GOLD:
                puck.setGolden(true);
                break;
        }
        effects.pickup(item.x, item.y, item.type.r, item.type.g, item.type.b);
    }

    private void trySpawn(Paddle p1, Paddle p2, Puck puck) {
        boolean left = MathUtils.randomBoolean();
        if (findOnSide(left) != null) left = !left;
        if (findOnSide(left) != null) return;

        PowerUpType[] all = PowerUpType.values();
        PowerUpType type = all[MathUtils.random(all.length - 1)];
        if (type == PowerUpType.GOLD && puck.isGolden()) {
            type = all[MathUtils.random(all.length - 2)]; // GOLD é o último
        }

        float mid = Table.WORLD_WIDTH / 2f;
        float minX = left ? Table.FIELD_LEFT + 70f : mid + 70f;
        float maxX = left ? mid - 70f : Table.FIELD_RIGHT - 70f;
        float minY = Table.FIELD_BOTTOM + 70f;
        float maxY = Table.FIELD_TOP - 70f;

        for (int attempt = 0; attempt < 12; attempt++) {
            float x = MathUtils.random(minX, maxX);
            float y = MathUtils.random(minY, maxY);
            if (p1.getPosition().dst2(x, y) < 140f * 140f) continue;
            if (p2.getPosition().dst2(x, y) < 140f * 140f) continue;
            if (puck.getPosition().dst2(x, y) < 140f * 140f) continue;

            PowerUp item = new PowerUp();
            item.type = type;
            item.x = x;
            item.y = y;
            item.left = left;
            items.add(item);
            return;
        }
    }

    // ---------------------------------------------------------------- Render

    public void render(ShapeRenderer shapes) {
        for (PowerUp item : items) {
            float appear = Math.min(1f, item.age * 4f);
            float scale = appear < 1f ? 1.25f * MathUtils.sin(appear * MathUtils.PI * 0.5f) : 1f;
            scale *= 1f + 0.07f * MathUtils.sin(item.age * 6f);

            float remaining = LIFETIME - item.age;
            float blink = remaining < BLINK_TIME ? (MathUtils.sin(item.age * 22f) > 0f ? 1f : 0.3f) : 1f;

            float r = PICKUP_RADIUS * scale;
            PowerUpType t = item.type;

            // Brilho
            shapes.setColor(t.r, t.g, t.b, 0.16f * blink);
            shapes.circle(item.x, item.y, r * 2.0f, 32);
            shapes.setColor(t.r, t.g, t.b, 0.26f * blink);
            shapes.circle(item.x, item.y, r * 1.45f, 32);

            // Moeda: anel colorido + miolo escuro
            shapes.setColor(t.r, t.g, t.b, blink);
            shapes.circle(item.x, item.y, r, 32);
            shapes.setColor(0.05f, 0.06f, 0.11f, 1f);
            shapes.circle(item.x, item.y, r * 0.8f, 32);

            drawIcon(shapes, item, scale, blink);
        }
    }

    private void drawIcon(ShapeRenderer shapes, PowerUp item, float s, float blink) {
        PowerUpType t = item.type;
        float x = item.x, y = item.y;
        shapes.setColor(t.r, t.g, t.b, blink);
        switch (t) {
            case SPEED: // duas setas ">>"
                shapes.triangle(x - 11f * s, y - 9f * s, x - 11f * s, y + 9f * s, x - 1f * s, y);
                shapes.triangle(x - 1f * s, y - 9f * s, x - 1f * s, y + 9f * s, x + 10f * s, y);
                break;
            case SIZE: // anéis concêntricos (crescer)
                shapes.circle(x, y, 12f * s, 24);
                shapes.setColor(0.05f, 0.06f, 0.11f, 1f);
                shapes.circle(x, y, 8f * s, 24);
                shapes.setColor(t.r, t.g, t.b, blink);
                shapes.circle(x, y, 4f * s, 16);
                break;
            case GOLD: // estrela de seis pontas
                shapes.triangle(x, y + 13f * s, x - 11.3f * s, y - 6.5f * s, x + 11.3f * s, y - 6.5f * s);
                shapes.triangle(x, y - 13f * s, x - 11.3f * s, y + 6.5f * s, x + 11.3f * s, y + 6.5f * s);
                break;
        }
    }
}
