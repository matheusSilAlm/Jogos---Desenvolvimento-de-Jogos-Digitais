package io.github.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

public class Particle {
    public float x, y;
    public float vx, vy;
    public float size;
    public Color color;
    public float life;
    public float maxLife;

    public Particle(float x, float y, float vx, float vy, float size, Color color, float maxLife) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        this.color = new Color(color);
        this.life = maxLife;
        this.maxLife = maxLife;
    }

    public boolean update(float delta) {
        x += vx * delta;
        y += vy * delta;
        vy -= 600 * delta; // Gravidade arcade
        life -= delta;
        return life > 0;
    }

    public void draw(SpriteBatch batch, Texture pixelTexture) {
        float alpha = Math.max(0, life / maxLife);
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixelTexture, x, y, size, size);
    }
}
