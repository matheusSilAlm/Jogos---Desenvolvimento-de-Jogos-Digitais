package io.github.airhockey.model;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class Table {
    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    public static final float FIELD_LEFT = 60f;
    public static final float FIELD_RIGHT = WORLD_WIDTH - 60f;
    public static final float FIELD_BOTTOM = 60f;
    public static final float FIELD_TOP = WORLD_HEIGHT - 60f;

    public static final float GOAL_HALF = 105f;
    public static final float GOAL_TOP = WORLD_HEIGHT / 2f + GOAL_HALF;
    public static final float GOAL_BOTTOM = WORLD_HEIGHT / 2f - GOAL_HALF;

    public enum GoalSide {
        NONE,
        LEFT,  // Gol no lado esquerdo (ponto do Jogador 2 / IA)
        RIGHT  // Gol no lado direito (ponto do Jogador 1)
    }

    public boolean isInGoalBand(float y) {
        return y > GOAL_BOTTOM && y < GOAL_TOP;
    }

    public void clampPaddle(Paddle paddle) {
        float minX = paddle.isLeftSide() ? FIELD_LEFT + paddle.getRadius() : WORLD_WIDTH / 2f + paddle.getRadius();
        float maxX = paddle.isLeftSide() ? WORLD_WIDTH / 2f - paddle.getRadius() : FIELD_RIGHT - paddle.getRadius();

        float x = MathUtils.clamp(paddle.getPosition().x, minX, maxX);
        float y = MathUtils.clamp(paddle.getPosition().y, FIELD_BOTTOM + paddle.getRadius(), FIELD_TOP - paddle.getRadius());

        paddle.getPosition().set(x, y);
    }

    public GoalSide handlePuckBounds(Puck puck) {
        // Paredes superior / inferior
        if (puck.getPosition().y + puck.getRadius() > FIELD_TOP) {
            puck.getPosition().y = FIELD_TOP - puck.getRadius();
            puck.getVelocity().y = -puck.getVelocity().y * Puck.RESTITUTION;
            puck.redirectDrift(0f, -1f);
        }
        if (puck.getPosition().y - puck.getRadius() < FIELD_BOTTOM) {
            puck.getPosition().y = FIELD_BOTTOM + puck.getRadius();
            puck.getVelocity().y = -puck.getVelocity().y * Puck.RESTITUTION;
            puck.redirectDrift(0f, 1f);
        }

        if (isInGoalBand(puck.getPosition().y)) {
            if (puck.getPosition().x < FIELD_LEFT) {
                return GoalSide.LEFT;
            }
            if (puck.getPosition().x > FIELD_RIGHT) {
                return GoalSide.RIGHT;
            }
        } else {
            if (puck.getPosition().x - puck.getRadius() < FIELD_LEFT) {
                puck.getPosition().x = FIELD_LEFT + puck.getRadius();
                puck.getVelocity().x = -puck.getVelocity().x * Puck.RESTITUTION;
                puck.redirectDrift(1f, 0f);
            }
            if (puck.getPosition().x + puck.getRadius() > FIELD_RIGHT) {
                puck.getPosition().x = FIELD_RIGHT - puck.getRadius();
                puck.getVelocity().x = -puck.getVelocity().x * Puck.RESTITUTION;
                puck.redirectDrift(-1f, 0f);
            }
        }

        return GoalSide.NONE;
    }

    public void render(ShapeRenderer shapes) {
        // 1. Moldura externa / gabinete de fliperama (carbono escuro)
        shapes.setColor(0.04f, 0.05f, 0.09f, 1f);
        shapes.rect(FIELD_LEFT - 18f, FIELD_BOTTOM - 18f,
            (FIELD_RIGHT - FIELD_LEFT) + 36f, (FIELD_TOP - FIELD_BOTTOM) + 36f);

        // 2. Superfície da mesa (azul escuro cyberpunk)
        shapes.setColor(0.07f, 0.09f, 0.16f, 1f);
        shapes.rect(FIELD_LEFT, FIELD_BOTTOM, FIELD_RIGHT - FIELD_LEFT, FIELD_TOP - FIELD_BOTTOM);

        // 3. Grid de orifícios de ar (pontos clássicos de mesa de fliperama)
        shapes.setColor(0.12f, 0.17f, 0.28f, 0.45f);
        for (float x = FIELD_LEFT + 32f; x < FIELD_RIGHT - 16f; x += 32f) {
            for (float y = FIELD_BOTTOM + 30f; y < FIELD_TOP - 16f; y += 30f) {
                shapes.rect(x, y, 2f, 2f);
            }
        }

        // 4. Linha central com brilho neon multicamada
        shapes.setColor(0.65f, 0.20f, 0.95f, 0.08f); // Glow amplo
        shapes.rect(WORLD_WIDTH / 2f - 14f, FIELD_BOTTOM, 28f, FIELD_TOP - FIELD_BOTTOM);
        shapes.setColor(0.65f, 0.20f, 0.95f, 0.25f); // Glow médio
        shapes.rect(WORLD_WIDTH / 2f - 7f, FIELD_BOTTOM, 14f, FIELD_TOP - FIELD_BOTTOM);
        shapes.setColor(0.75f, 0.35f, 1f, 0.85f); // Tubo neon
        shapes.rect(WORLD_WIDTH / 2f - 2f, FIELD_BOTTOM, 4f, FIELD_TOP - FIELD_BOTTOM);
        shapes.setColor(0.95f, 0.85f, 1f, 0.90f); // Hot core branco
        shapes.rect(WORLD_WIDTH / 2f - 0.75f, FIELD_BOTTOM, 1.5f, FIELD_TOP - FIELD_BOTTOM);

        // 5. Círculo central estilo fliperama
        shapes.setColor(0.65f, 0.20f, 0.95f, 0.08f); // Glow amplo
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 106f, 48);
        shapes.setColor(0.65f, 0.20f, 0.95f, 0.22f); // Glow médio
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 98f, 48);

        shapes.setColor(0.07f, 0.09f, 0.16f, 1f); // Fundo para criar anel
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 94f, 48);

        shapes.setColor(0.75f, 0.35f, 1f, 0.90f); // Anel neon
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 90f, 48);

        shapes.setColor(0.92f, 0.80f, 1f, 0.70f); // Hot core do anel
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 88f, 48);

        shapes.setColor(0.07f, 0.09f, 0.16f, 1f);
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 85f, 48);

        shapes.setColor(0f, 0.95f, 1f, 0.25f); // Glow ponto central
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 16f, 24);
        shapes.setColor(0f, 0.95f, 1f, 0.95f); // Ponto ciano
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 9f, 24);
        shapes.setColor(0.85f, 1f, 1f, 1f); // Hot core branco
        shapes.circle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 4f, 16);

        // 6. Arcos de defesa / grandes áreas neon com hot core
        // Lado 1 (Rosa neon)
        shapes.setColor(1.0f, 0.1f, 0.55f, 0.08f);
        shapes.arc(FIELD_LEFT, WORLD_HEIGHT / 2f, 166f, -90f, 180f, 48);
        shapes.setColor(1.0f, 0.1f, 0.55f, 0.25f);
        shapes.arc(FIELD_LEFT, WORLD_HEIGHT / 2f, 160f, -90f, 180f, 48);
        shapes.setColor(0.07f, 0.09f, 0.16f, 1f);
        shapes.arc(FIELD_LEFT, WORLD_HEIGHT / 2f, 154f, -90f, 180f, 48);

        // Lado 2 (Ciano neon)
        shapes.setColor(0.0f, 0.9f, 1f, 0.08f);
        shapes.arc(FIELD_RIGHT, WORLD_HEIGHT / 2f, 166f, 90f, 180f, 48);
        shapes.setColor(0.0f, 0.9f, 1f, 0.25f);
        shapes.arc(FIELD_RIGHT, WORLD_HEIGHT / 2f, 160f, 90f, 180f, 48);
        shapes.setColor(0.07f, 0.09f, 0.16f, 1f);
        shapes.arc(FIELD_RIGHT, WORLD_HEIGHT / 2f, 154f, 90f, 180f, 48);

        // 7. Gols com iluminação arcade neon intensa
        // Gol esquerdo (defendido pelo J1 - Neon Magenta)
        shapes.setColor(1f, 0.1f, 0.5f, 0.12f);
        shapes.rect(FIELD_LEFT - 36f, GOAL_BOTTOM - 16f, 36f, (GOAL_TOP - GOAL_BOTTOM) + 32f);
        shapes.setColor(1f, 0.1f, 0.5f, 0.35f);
        shapes.rect(FIELD_LEFT - 24f, GOAL_BOTTOM - 6f, 24f, (GOAL_TOP - GOAL_BOTTOM) + 12f);
        shapes.setColor(1f, 0.1f, 0.5f, 0.90f);
        shapes.rect(FIELD_LEFT - 14f, GOAL_BOTTOM, 14f, GOAL_TOP - GOAL_BOTTOM);
        shapes.setColor(1f, 0.7f, 0.9f, 0.80f); // Hot core
        shapes.rect(FIELD_LEFT - 6f, GOAL_BOTTOM + 2f, 4f, (GOAL_TOP - GOAL_BOTTOM) - 4f);

        // Gol direito (defendido pelo J2 / IA - Neon Cyan)
        shapes.setColor(0f, 0.9f, 1f, 0.12f);
        shapes.rect(FIELD_RIGHT, GOAL_BOTTOM - 16f, 36f, (GOAL_TOP - GOAL_BOTTOM) + 32f);
        shapes.setColor(0f, 0.9f, 1f, 0.35f);
        shapes.rect(FIELD_RIGHT, GOAL_BOTTOM - 6f, 24f, (GOAL_TOP - GOAL_BOTTOM) + 12f);
        shapes.setColor(0f, 0.9f, 1f, 0.90f);
        shapes.rect(FIELD_RIGHT, GOAL_BOTTOM, 14f, GOAL_TOP - GOAL_BOTTOM);
        shapes.setColor(0.7f, 1f, 1f, 0.80f); // Hot core
        shapes.rect(FIELD_RIGHT + 2f, GOAL_BOTTOM + 2f, 4f, (GOAL_TOP - GOAL_BOTTOM) - 4f);

        // 8. Bordas da quadra com neon duplo
        float borderT = 6f;
        // Borda superior (metade magenta, metade ciano)
        shapes.setColor(1f, 0.1f, 0.5f, 0.7f);
        shapes.rect(FIELD_LEFT, FIELD_TOP - borderT, (FIELD_RIGHT - FIELD_LEFT) / 2f, borderT);
        shapes.setColor(0f, 0.9f, 1f, 0.7f);
        shapes.rect(WORLD_WIDTH / 2f, FIELD_TOP - borderT, (FIELD_RIGHT - FIELD_LEFT) / 2f, borderT);

        // Borda inferior
        shapes.setColor(1f, 0.1f, 0.5f, 0.7f);
        shapes.rect(FIELD_LEFT, FIELD_BOTTOM, (FIELD_RIGHT - FIELD_LEFT) / 2f, borderT);
        shapes.setColor(0f, 0.9f, 1f, 0.7f);
        shapes.rect(WORLD_WIDTH / 2f, FIELD_BOTTOM, (FIELD_RIGHT - FIELD_LEFT) / 2f, borderT);

        // Paredes laterais (fora da abertura do gol)
        shapes.setColor(1f, 0.1f, 0.5f, 0.8f);
        shapes.rect(FIELD_LEFT, FIELD_BOTTOM, borderT, GOAL_BOTTOM - FIELD_BOTTOM);
        shapes.rect(FIELD_LEFT, GOAL_TOP, borderT, FIELD_TOP - GOAL_TOP);

        shapes.setColor(0f, 0.9f, 1f, 0.8f);
        shapes.rect(FIELD_RIGHT - borderT, FIELD_BOTTOM, borderT, GOAL_BOTTOM - FIELD_BOTTOM);
        shapes.rect(FIELD_RIGHT - borderT, GOAL_TOP, borderT, FIELD_TOP - GOAL_TOP);
    }
}
