package io.github.airhockey.fx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.airhockey.model.Table;

/** Efeitos visuais de fliperama: explosões de gol, ondas de choque, flash, tremor de tela e fogos de vitória. */
public class Effects {

    private enum Kind { SPARK, CONFETTI, ROCKET, TRAIL }

    private static class Particle {
        Kind kind;
        float x, y, vx, vy;
        float life, maxLife;
        float size;
        float gravity, drag;
        float rot, rotSpeed;
        float r, g, b;
        float burstSize; // foguete: tamanho da explosão
    }

    private static class Ring {
        float x, y, radius, speed, thickness, life, maxLife;
        float r, g, b;
    }

    private static class Flash {
        float x, w, life, maxLife;
        float r, g, b;
    }

    private static final int MAX_PARTICLES = 2500;

    private final Array<Particle> particles = new Array<>(false, 512);
    private final Array<Ring> rings = new Array<>(false, 16);
    private final Array<Flash> flashes = new Array<>(false, 8);

    private float shake = 0f;
    private float shakeX = 0f;
    private float shakeY = 0f;

    private boolean victoryActive = false;
    private boolean victoryLeftSide = true;
    private float victoryR, victoryG, victoryB;
    private float rocketTimer = 0f;
    private float confettiTimer = 0f;

    // ---------------------------------------------------------------- API

    /** Explosão de gol no gol onde o disco entrou. */
    public void goalScored(boolean leftGoal, float y, Color scorerColor) {
        float gx = leftGoal ? Table.FIELD_LEFT : Table.FIELD_RIGHT;
        float dir = leftGoal ? 1f : -1f; // para dentro da quadra
        float cr = scorerColor.r, cg = scorerColor.g, cb = scorerColor.b;

        // Ondas de choque
        addRing(gx, y, 30f, 700f, 14f, 0.55f, cr, cg, cb);
        addRing(gx, y, 10f, 480f, 8f, 0.7f, 1f, 1f, 1f);
        addRing(gx, y, 0f, 900f, 20f, 0.45f, 1f, 0.85f, 0.2f);

        // Flash na metade do gol
        Flash f = new Flash();
        f.x = leftGoal ? Table.FIELD_LEFT : Table.WORLD_WIDTH / 2f;
        f.w = (Table.FIELD_RIGHT - Table.FIELD_LEFT) / 2f;
        f.maxLife = f.life = 0.45f;
        f.r = cr; f.g = cg; f.b = cb;
        flashes.add(f);

        // Faíscas em leque
        for (int i = 0; i < 140; i++) {
            float ang = MathUtils.random(-80f, 80f);
            float spd = MathUtils.random(150f, 1100f);
            Particle p = spark(gx, y + MathUtils.random(-25f, 25f),
                dir * MathUtils.cosDeg(ang) * spd, MathUtils.sinDeg(ang) * spd,
                MathUtils.random(0.5f, 1.3f), MathUtils.random(3f, 7f), pickColor(cr, cg, cb));
            p.gravity = -120f;
            p.drag = 1.6f;
            addParticle(p);
        }
        // Brilho branco central
        for (int i = 0; i < 40; i++) {
            float ang = MathUtils.random(360f);
            float spd = MathUtils.random(60f, 450f);
            Particle p = spark(gx, y, MathUtils.cosDeg(ang) * spd, MathUtils.sinDeg(ang) * spd,
                MathUtils.random(0.25f, 0.6f), MathUtils.random(5f, 10f), new float[]{1f, 1f, 1f});
            p.drag = 3f;
            addParticle(p);
        }
        // Confete curto saindo do gol
        for (int i = 0; i < 50; i++) {
            float ang = MathUtils.random(-70f, 70f);
            float spd = MathUtils.random(250f, 800f);
            Particle p = confetti(gx, y, dir * MathUtils.cosDeg(ang) * spd, MathUtils.sinDeg(ang) * spd,
                pickColor(cr, cg, cb));
            p.maxLife = p.life = MathUtils.random(1.0f, 1.8f);
            addParticle(p);
        }

        shake = Math.max(shake, 16f);
    }

    /** Coleta de powerup: anel + faíscas radiais na cor do item. */
    public void pickup(float x, float y, float r, float g, float b) {
        addRing(x, y, 10f, 420f, 8f, 0.45f, r, g, b);
        addRing(x, y, 0f, 260f, 5f, 0.35f, 1f, 1f, 1f);
        for (int i = 0; i < 50; i++) {
            float ang = MathUtils.random(360f);
            float spd = MathUtils.random(80f, 520f);
            Particle p = spark(x, y, MathUtils.cosDeg(ang) * spd, MathUtils.sinDeg(ang) * spd,
                MathUtils.random(0.4f, 0.9f), MathUtils.random(3f, 6f),
                MathUtils.randomBoolean(0.7f) ? new float[]{r, g, b} : new float[]{1f, 1f, 1f});
            p.drag = 2.5f;
            addParticle(p);
        }
        shake = Math.max(shake, 4f);
    }

    /** Faísca dourada isolada (rastro do disco dourado). */
    public void sparkle(float x, float y) {
        float ang = MathUtils.random(360f);
        float spd = MathUtils.random(20f, 120f);
        Particle p = spark(x + MathUtils.random(-8f, 8f), y + MathUtils.random(-8f, 8f),
            MathUtils.cosDeg(ang) * spd, MathUtils.sinDeg(ang) * spd,
            MathUtils.random(0.3f, 0.7f), MathUtils.random(2f, 5f),
            MathUtils.randomBoolean(0.7f) ? new float[]{1f, 0.84f, 0.1f} : new float[]{1f, 1f, 0.8f});
        p.drag = 2f;
        addParticle(p);
    }

    /** Liga a celebração contínua da vitória no lado do vencedor. */
    public void startVictory(boolean leftSide, Color winnerColor) {
        victoryActive = true;
        victoryLeftSide = leftSide;
        victoryR = winnerColor.r;
        victoryG = winnerColor.g;
        victoryB = winnerColor.b;
        rocketTimer = 0f;
        confettiTimer = 0f;

        // Estouro inicial
        float cx = victoryCenterX();
        addRing(cx, Table.WORLD_HEIGHT / 2f, 20f, 900f, 18f, 0.9f, victoryR, victoryG, victoryB);
        addRing(cx, Table.WORLD_HEIGHT / 2f, 0f, 1200f, 10f, 0.8f, 1f, 1f, 1f);
        Flash f = new Flash();
        f.x = leftSide ? Table.FIELD_LEFT : Table.WORLD_WIDTH / 2f;
        f.w = (Table.FIELD_RIGHT - Table.FIELD_LEFT) / 2f;
        f.maxLife = f.life = 0.8f;
        f.r = victoryR; f.g = victoryG; f.b = victoryB;
        flashes.add(f);
        shake = Math.max(shake, 24f);
        for (int i = 0; i < 5; i++) launchRocket();
    }

    public void stopVictory() {
        victoryActive = false;
    }

    public void clear() {
        particles.clear();
        rings.clear();
        flashes.clear();
        shake = 0f;
        shakeX = shakeY = 0f;
        victoryActive = false;
    }

    public float getShakeX() { return shakeX; }
    public float getShakeY() { return shakeY; }

    // ---------------------------------------------------------------- Update

    public void update(float dt) {
        // Tremor de tela
        if (shake > 0.2f) {
            shakeX = MathUtils.random(-shake, shake);
            shakeY = MathUtils.random(-shake, shake);
            shake = Math.max(0f, shake - shake * 5f * dt - 10f * dt);
        } else {
            shake = 0f;
            shakeX = shakeY = 0f;
        }

        if (victoryActive) {
            rocketTimer -= dt;
            if (rocketTimer <= 0f) {
                launchRocket();
                rocketTimer = MathUtils.random(0.12f, 0.4f);
            }
            confettiTimer -= dt;
            while (confettiTimer <= 0f) {
                spawnFallingConfetti();
                confettiTimer += 0.012f;
            }
        }

        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= dt;
            if (p.life <= 0f) {
                if (p.kind == Kind.ROCKET) explode(p);
                particles.removeIndex(i);
                continue;
            }
            float damp = Math.max(0f, 1f - p.drag * dt);
            p.vx *= damp;
            p.vy = p.vy * damp - p.gravity * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.rot += p.rotSpeed * dt;

            if (p.kind == Kind.CONFETTI) {
                p.vx += MathUtils.sin(p.rot * 0.05f) * 30f * dt; // balanço
            } else if (p.kind == Kind.ROCKET) {
                if (MathUtils.randomBoolean(0.8f)) {
                    Particle t = spark(p.x, p.y, MathUtils.random(-30f, 30f), MathUtils.random(-60f, -10f),
                        MathUtils.random(0.2f, 0.45f), MathUtils.random(2f, 4f), new float[]{1f, 0.85f, 0.4f});
                    t.kind = Kind.TRAIL;
                    addParticle(t);
                }
            }
        }

        for (int i = rings.size - 1; i >= 0; i--) {
            Ring r = rings.get(i);
            r.life -= dt;
            if (r.life <= 0f) { rings.removeIndex(i); continue; }
            r.radius += r.speed * dt;
            r.speed *= Math.max(0f, 1f - 2.2f * dt);
        }

        for (int i = flashes.size - 1; i >= 0; i--) {
            Flash f = flashes.get(i);
            f.life -= dt;
            if (f.life <= 0f) flashes.removeIndex(i);
        }
    }

    // ---------------------------------------------------------------- Render

    /** Desenhar dentro de shapes.begin(Filled) com blending ativo. */
    public void render(ShapeRenderer shapes) {
        for (Flash f : flashes) {
            float t = f.life / f.maxLife;
            // camadas sobrepostas: simula gradiente
            for (int i = 0; i < 4; i++) {
                float layer = 1f - i * 0.22f;
                shapes.setColor(f.r, f.g, f.b, 0.16f * t * t * layer);
                shapes.rect(f.x, Table.FIELD_BOTTOM, f.w, Table.FIELD_TOP - Table.FIELD_BOTTOM);
            }
        }

        for (Ring r : rings) {
            float t = r.life / r.maxLife;
            drawRing(shapes, r.x, r.y, r.radius, r.thickness * (0.4f + t), r.r, r.g, r.b, t);
        }

        for (Particle p : particles) {
            float t = p.life / p.maxLife;
            switch (p.kind) {
                case CONFETTI: {
                    float flip = Math.abs(MathUtils.sinDeg(p.rot * 3f)); // efeito de giro 3D
                    shapes.setColor(p.r, p.g, p.b, Math.min(1f, t * 3f));
                    shapes.rect(p.x, p.y, p.size / 2f, p.size / 2f, p.size, p.size * 0.5f * (0.2f + flip), 1f, 1f, p.rot);
                    break;
                }
                case ROCKET:
                    shapes.setColor(1f, 0.95f, 0.7f, 1f);
                    shapes.circle(p.x, p.y, p.size, 8);
                    break;
                default: {
                    float a = Math.min(1f, t * 1.6f);
                    float s = p.size * (0.4f + 0.6f * t);
                    shapes.setColor(p.r, p.g, p.b, a * 0.35f);
                    shapes.circle(p.x, p.y, s * 2.2f, 8); // halo
                    shapes.setColor(p.r, p.g, p.b, a);
                    shapes.circle(p.x, p.y, s, 8);
                }
            }
        }
    }

    private void drawRing(ShapeRenderer shapes, float x, float y, float radius, float thickness,
                          float r, float g, float b, float alpha) {
        if (radius <= 0f) return;
        // Anel como polígono de segmentos para não cobrir o interior
        int seg = 64;
        float inner = Math.max(0f, radius - thickness / 2f);
        float outer = radius + thickness / 2f;
        shapes.setColor(r, g, b, alpha * 0.8f);
        for (int i = 0; i < seg; i++) {
            float a0 = MathUtils.PI2 * i / seg;
            float a1 = MathUtils.PI2 * (i + 1) / seg;
            float c0 = MathUtils.cos(a0), s0 = MathUtils.sin(a0);
            float c1 = MathUtils.cos(a1), s1 = MathUtils.sin(a1);
            float ox0 = x + c0 * outer, oy0 = y + s0 * outer;
            float ox1 = x + c1 * outer, oy1 = y + s1 * outer;
            float ix0 = x + c0 * inner, iy0 = y + s0 * inner;
            float ix1 = x + c1 * inner, iy1 = y + s1 * inner;
            shapes.triangle(ox0, oy0, ox1, oy1, ix0, iy0);
            shapes.triangle(ix0, iy0, ox1, oy1, ix1, iy1);
        }
    }

    // ---------------------------------------------------------------- Helpers

    private float victoryCenterX() {
        float left = Table.FIELD_LEFT;
        float mid = Table.WORLD_WIDTH / 2f;
        float right = Table.FIELD_RIGHT;
        return victoryLeftSide ? (left + mid) / 2f : (mid + right) / 2f;
    }

    private float victoryMinX() { return victoryLeftSide ? Table.FIELD_LEFT : Table.WORLD_WIDTH / 2f; }
    private float victoryMaxX() { return victoryLeftSide ? Table.WORLD_WIDTH / 2f : Table.FIELD_RIGHT; }

    private void launchRocket() {
        Particle p = new Particle();
        p.kind = Kind.ROCKET;
        p.x = MathUtils.random(victoryMinX() + 40f, victoryMaxX() - 40f);
        p.y = Table.FIELD_BOTTOM;
        p.vx = MathUtils.random(-40f, 40f);
        p.vy = MathUtils.random(520f, 760f);
        p.maxLife = p.life = MathUtils.random(0.7f, 1.0f);
        p.size = 4f;
        p.gravity = 380f;
        p.drag = 0.2f;
        p.burstSize = MathUtils.random(0.8f, 1.4f);
        addParticle(p);
    }

    private void explode(Particle rocket) {
        float[] base = pickColor(victoryR, victoryG, victoryB);
        float[] alt = MathUtils.randomBoolean(0.5f) ? new float[]{1f, 0.85f, 0.2f} : new float[]{1f, 1f, 1f};
        int n = (int) (70 * rocket.burstSize);
        for (int i = 0; i < n; i++) {
            float ang = MathUtils.random(360f);
            float spd = MathUtils.random(100f, 420f) * rocket.burstSize;
            float[] c = (i % 3 == 0) ? alt : base;
            Particle p = spark(rocket.x, rocket.y, MathUtils.cosDeg(ang) * spd, MathUtils.sinDeg(ang) * spd,
                MathUtils.random(0.8f, 1.6f), MathUtils.random(3f, 6f), c);
            p.gravity = 140f;
            p.drag = 1.4f;
            addParticle(p);
        }
        addRing(rocket.x, rocket.y, 5f, 300f * rocket.burstSize, 5f, 0.35f, base[0], base[1], base[2]);
        shake = Math.max(shake, 3.5f);
    }

    private void spawnFallingConfetti() {
        float[] c = confettiColor();
        Particle p = confetti(MathUtils.random(victoryMinX(), victoryMaxX()),
            Table.FIELD_TOP + 10f, MathUtils.random(-60f, 60f), MathUtils.random(-220f, -80f), c);
        p.maxLife = p.life = MathUtils.random(2.2f, 3.6f);
        addParticle(p);
    }

    private Particle spark(float x, float y, float vx, float vy, float life, float size, float[] c) {
        Particle p = new Particle();
        p.kind = Kind.SPARK;
        p.x = x; p.y = y; p.vx = vx; p.vy = vy;
        p.maxLife = p.life = life;
        p.size = size;
        p.r = c[0]; p.g = c[1]; p.b = c[2];
        return p;
    }

    private Particle confetti(float x, float y, float vx, float vy, float[] c) {
        Particle p = new Particle();
        p.kind = Kind.CONFETTI;
        p.x = x; p.y = y; p.vx = vx; p.vy = vy;
        p.size = MathUtils.random(8f, 16f);
        p.gravity = 260f;
        p.drag = 1.1f;
        p.rot = MathUtils.random(360f);
        p.rotSpeed = MathUtils.random(-400f, 400f);
        p.maxLife = p.life = 2f;
        p.r = c[0]; p.g = c[1]; p.b = c[2];
        return p;
    }

    private void addParticle(Particle p) {
        if (particles.size < MAX_PARTICLES) particles.add(p);
    }

    private void addRing(float x, float y, float startRadius, float speed, float thickness, float life,
                         float r, float g, float b) {
        Ring ring = new Ring();
        ring.x = x; ring.y = y; ring.radius = startRadius; ring.speed = speed;
        ring.thickness = thickness;
        ring.maxLife = ring.life = life;
        ring.r = r; ring.g = g; ring.b = b;
        rings.add(ring);
    }

    /** Cor do jogador, com variação para branco/dourado. */
    private float[] pickColor(float r, float g, float b) {
        float roll = MathUtils.random();
        if (roll < 0.55f) return new float[]{r, g, b};
        if (roll < 0.75f) return new float[]{1f, 1f, 1f};
        if (roll < 0.90f) return new float[]{1f, 0.85f, 0.2f};
        return new float[]{MathUtils.clamp(r + 0.25f, 0f, 1f), MathUtils.clamp(g + 0.25f, 0f, 1f),
            MathUtils.clamp(b + 0.25f, 0f, 1f)};
    }

    private float[] confettiColor() {
        float roll = MathUtils.random();
        if (roll < 0.45f) return new float[]{victoryR, victoryG, victoryB};
        if (roll < 0.60f) return new float[]{1f, 1f, 1f};
        if (roll < 0.75f) return new float[]{1f, 0.85f, 0.2f};
        if (roll < 0.88f) return new float[]{0.75f, 0.35f, 1f};
        return new float[]{0.3f, 1f, 0.5f};
    }
}
