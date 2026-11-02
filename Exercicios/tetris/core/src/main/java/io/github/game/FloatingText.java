package io.github.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

public class FloatingText {
    public String text;
    public float x, y;
    public Color color;
    public float life;
    public float maxLife;

    public FloatingText(String text, float x, float y, Color color, float maxLife) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = new Color(color);
        this.life = maxLife;
        this.maxLife = maxLife;
    }

    public boolean update(float delta) {
        y += 45 * delta; // Flutua suavemente para cima
        life -= delta;
        return life > 0;
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        float alpha = Math.min(1.0f, life * 2.2f);
        font.setColor(color.r, color.g, color.b, alpha);
        font.draw(batch, text, x - 150, y, 300, Align.center, false);
    }
}
