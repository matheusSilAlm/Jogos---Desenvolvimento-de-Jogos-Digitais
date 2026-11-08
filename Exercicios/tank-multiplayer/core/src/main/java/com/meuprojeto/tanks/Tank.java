package com.meuprojeto.tanks;

/**
 * Dados de um tanque (um por slot de jogador). Sem LibGDX.
 * A cor NÃO é guardada aqui: ela é derivada do id no cliente (TankColors).
 */
public class Tank {
    public final int id;
    public String name = "";

    public boolean active;        // tem jogador conectado neste slot?
    public boolean alive;         // está vivo ou esperando renascer?

    public float x, y;            // centro do tanque
    public int   facing = GameState.DIR_UP;  // direção do corpo (cardinal)
    public float turretAngle;     // ângulo da torre em radianos (livre, segue o mouse)

    public int   hp;
    public float reload;          // segundos até poder atirar de novo (0 = pronto)
    public float respawnTimer;    // segundos até renascer (quando morto)
    public float shield;          // segundos de invulnerabilidade restantes (nascimento)

    // ---- power-ups ----
    public int   armor;           // 1 = escudo extra (quadradinho azul); some ao tomar um tiro
    public float damageBoost;     // segundos restantes de dano dobrado (0 = sem bônus)

    public int kills;
    public int deaths;

    public Tank(int id) {
        this.id = id;
        this.name = "Jogador " + (id + 1);
    }

    /** Ponta do cano, de onde sai o projétil. */
    public float barrelTipX() {
        return x + (float) Math.cos(turretAngle) * GameState.BARREL_LENGTH;
    }

    public float barrelTipY() {
        return y + (float) Math.sin(turretAngle) * GameState.BARREL_LENGTH;
    }
}
