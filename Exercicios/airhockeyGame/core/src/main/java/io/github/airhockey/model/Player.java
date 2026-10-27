package io.github.airhockey.model;

import com.badlogic.gdx.graphics.Color;
import io.github.airhockey.input.PlayerController;

public class Player {
    private final String name;
    private final Paddle paddle;
    private final PlayerController controller;
    private final Color color;
    private int score;

    public Player(String name, Paddle paddle, PlayerController controller, Color color) {
        this.name = name;
        this.paddle = paddle;
        this.controller = controller;
        this.color = new Color(color);
        this.score = 0;
    }

    public void update(float dt, Table table) {
        controller.update(dt);
        float dx = controller.getMoveX();
        float dy = controller.getMoveY();
        boolean boost = controller.isBoost();
        paddle.update(dx, dy, boost, dt, table);
    }

    public void addScore() {
        this.score++;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void resetScore() {
        this.score = 0;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public Color getColor() {
        return color;
    }

    public PlayerController getController() {
        return controller;
    }
}
