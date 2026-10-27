package io.github.airhockey.physics;

import com.badlogic.gdx.math.Vector2;
import io.github.airhockey.model.Paddle;
import io.github.airhockey.model.Puck;

public class CollisionHandler {
    public static final float MIN_EXIT_SPEED = 90f;
    public static final float BOOST_FACTOR = 1.9f;

    private final Vector2 normal = new Vector2();
    private final Vector2 relativeVel = new Vector2();

    public void checkAndResolve(Puck puck, Paddle paddle) {
        normal.set(puck.getPosition()).sub(paddle.getPosition());
        float dist = normal.len();
        float minDist = paddle.getRadius() + puck.getRadius();
        if (dist >= minDist) return;

        if (dist < 0.0001f) {
            normal.set(1f, 0f);
            dist = 1f;
        }
        normal.scl(1f / dist); // normal unitária (do rebatedor para o disco)

        // Separa para evitar afundamento
        puck.getPosition().set(paddle.getPosition()).mulAdd(normal, minDist);

        // Velocidade relativa no eixo da normal
        relativeVel.set(puck.getVelocity()).sub(paddle.getVelocity());
        float vn = relativeVel.dot(normal);

        if (vn < 0f) { // disco aproximando
            float impulse = -(1f + Puck.RESTITUTION) * vn;
            if (paddle.isBoosting()) {
                impulse *= BOOST_FACTOR;
            }
            puck.getVelocity().mulAdd(normal, impulse);
            if (paddle.hasSpeedPowerUp()) {
                puck.getVelocity().scl(Paddle.SPEED_HIT_MULTIPLIER);
            }

            // Garante velocidade mínima de saída
            float exit = puck.getVelocity().dot(normal);
            if (exit < MIN_EXIT_SPEED) {
                puck.getVelocity().mulAdd(normal, MIN_EXIT_SPEED - exit);
            }
        }

        float maxSpeed = paddle.hasSpeedPowerUp() ? Puck.MAX_SPEED * Paddle.SPEED_HIT_MULTIPLIER : Puck.MAX_SPEED;
        if (puck.getVelocity().len() > maxSpeed) {
            puck.getVelocity().setLength(maxSpeed);
        }
    }
}
