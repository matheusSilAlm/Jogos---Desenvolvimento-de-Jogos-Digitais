package io.github.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class EnemyCar {
    public static final int LANES = 3;
    private static final float TRACK_LEFT = 150;
    private static final float LANE_WIDTH = 100;   // 300 de pista / 3 faixas
    private static final float MAX_Y = 1000;        // limite acima da tela quando o inimigo te ultrapassa

    private Texture texture;
    private Rectangle bounds = new Rectangle();
    private float baseSpeed; // velocidade "natural" do carro
    private float speed;     // velocidade atual (pode reduzir para seguir o da frente)
    private float x;
    private float y;
    private int lane;
    private boolean active = false; // false = esperando faixa livre para nascer

    // true quando ja pontuou (desviou) ou bateu nesse carro -> nao conta de novo
    private boolean resolved = false;

    public EnemyCar(Texture texture) {
        this.texture = texture;
    }

    public void spawn(int lane, float y) {
        this.lane = lane;
        this.y = y;
        this.x = TRACK_LEFT + lane * LANE_WIDTH + (LANE_WIDTH - texture.getWidth()) / 2f;
        this.baseSpeed = MathUtils.random(100, 200);
        this.speed = baseSpeed;
        this.resolved = false;
        this.active = true;
    }

    /** Se estiver perto do carro da frente (mesma faixa), anda na velocidade dele. */
    public void follow(EnemyCar leader, float followDistance) {
        speed = baseSpeed;
        if (leader != null && gapTo(leader) < followDistance) {
            speed = Math.min(baseSpeed, leader.speed);
        }
    }

    /** Move pela velocidade relativa: se o jogador for mais rapido o inimigo desce, se for mais lento ele sobe. */
    public void update(float deltaTime, float playerSpeed){
        y -= (playerSpeed - speed) * deltaTime;
        if (y > MAX_Y) {
            y = MAX_Y;
        }
    }

    /** Garante distancia minima do carro da frente (nunca sobrepoe). */
    public void keepBehind(EnemyCar leader, float minGap) {
        if (leader != null && gapTo(leader) < minGap) {
            y = leader.y - texture.getHeight() - minGap;
        }
    }

    /** Espaco livre entre o topo deste carro e a traseira do carro da frente. */
    public float gapTo(EnemyCar leader) {
        return leader.y - (y + texture.getHeight());
    }

    public void deactivate() {
        active = false;
    }

    public boolean isOffScreen() {
        return y < -texture.getHeight();
    }

    public boolean isActive() {
        return active;
    }

    public int getLane() {
        return lane;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getHeight() {
        return texture.getHeight();
    }

    /** Retangulo de colisao na posicao em que o carro foi desenhado. */
    public Rectangle getBounds(float drawX) {
        return bounds.set(drawX, y, texture.getWidth(), texture.getHeight());
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public void draw(SpriteBatch batch){
        batch.draw(texture, x, y);
    }
}
