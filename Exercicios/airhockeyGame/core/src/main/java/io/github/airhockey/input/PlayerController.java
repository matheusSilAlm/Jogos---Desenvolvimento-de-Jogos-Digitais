package io.github.airhockey.input;

public interface PlayerController {
    default void update(float dt) {}
    float getMoveX();
    float getMoveY();
    boolean isBoost();
}

