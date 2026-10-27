package io.github.airhockey.powerup;

public enum PowerUpType {
    /** Rebatedor que coleta rebate o disco com o dobro da velocidade. */
    SPEED(1.0f, 0.45f, 0.10f),
    /** Rebatedor que coleta fica 1.2x maior. */
    SIZE(0.30f, 1.0f, 0.50f),
    /** Disco dourado: vai sozinho ao gol mais próximo e vale 2 gols. */
    GOLD(1.0f, 0.84f, 0.10f);

    public final float r, g, b;

    PowerUpType(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
