package io.github.airhockey.input;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.airhockey.model.Paddle;
import io.github.airhockey.model.Puck;
import io.github.airhockey.model.Table;
import io.github.airhockey.powerup.PowerUpManager;

public class AIController implements PlayerController {

    private final Puck puck;
    private final Paddle paddle;
    private final Table table;
    private final PowerUpManager powerUpManager;

    private float moveX = 0f;
    private float moveY = 0f;
    private boolean boost = false;

    private final Vector2 target = new Vector2();
    private final Vector2 defaultDefensePos = new Vector2();

    public AIController(Puck puck, Paddle paddle, Table table, PowerUpManager powerUpManager) {
        this.puck = puck;
        this.paddle = paddle;
        this.table = table;
        this.powerUpManager = powerUpManager;
    }

    public AIController(Puck puck, Paddle paddle, Table table) {
        this(puck, paddle, table, null);
    }

    @Override
    public void update(float dt) {
        float midX = Table.WORLD_WIDTH / 2f;
        float goalCenterX = Table.FIELD_RIGHT;
        float goalCenterY = Table.WORLD_HEIGHT / 2f;

        defaultDefensePos.set(Table.FIELD_RIGHT - 140f, goalCenterY);

        Vector2 puckPos = puck.getPosition();
        Vector2 puckVel = puck.getVelocity();
        Vector2 aiPos = paddle.getPosition();

        boost = false;

        boolean puckInMyZone = puckPos.x > midX;

        if (!puckInMyZone) {
            // Disco no campo adversário: busca powerup no seu campo ou postura de defesa
            PowerUpManager.PowerUp item = powerUpManager != null ? powerUpManager.findOnSide(false) : null;
            if (item != null && puckVel.x <= 50f) {
                target.set(item.x, item.y);
            } else {
                float targetY = MathUtils.clamp(puckPos.y, Table.GOAL_BOTTOM - 20f, Table.GOAL_TOP + 20f);
                target.set(defaultDefensePos.x, targetY);
            }
        } else {
            // Disco no nosso campo
            if (puckPos.x > aiPos.x + 5f) {
                // Disco atrás do taco (perigo de gol contra): contorna para trás
                target.set(Math.min(Table.FIELD_RIGHT - paddle.getRadius(), puckPos.x + 40f),
                           puckPos.y > goalCenterY ? puckPos.y + 60f : puckPos.y - 60f);
            } else {
                // Disco à frente do taco: intercepta / ataca
                // Calcula interceptação simples com base na velocidade
                float timeToIntercept = 0.15f;
                float predictedY = puckPos.y + puckVel.y * timeToIntercept;
                predictedY = MathUtils.clamp(predictedY, Table.FIELD_BOTTOM + paddle.getRadius(), Table.FIELD_TOP - paddle.getRadius());

                target.set(puckPos.x, predictedY);

                // Ativa impulso (boost) quando estiver perto e avançando contra o disco
                float distToPuck = aiPos.dst(puckPos);
                if (distToPuck < 130f && puckPos.x < aiPos.x) {
                    boost = true;
                }
            }
        }

        // Calcula direção e normaliza
        float dx = target.x - aiPos.x;
        float dy = target.y - aiPos.y;

        // Zona morta para evitar tremor
        float deadzone = 8f;
        if (Math.abs(dx) < deadzone) dx = 0f;
        if (Math.abs(dy) < deadzone) dy = 0f;

        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0.001f) {
            moveX = dx / len;
            moveY = dy / len;
        } else {
            moveX = 0f;
            moveY = 0f;
        }
    }

    @Override
    public float getMoveX() {
        return moveX;
    }

    @Override
    public float getMoveY() {
        return moveY;
    }

    @Override
    public boolean isBoost() {
        return boost;
    }
}
