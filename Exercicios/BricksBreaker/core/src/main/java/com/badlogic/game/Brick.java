package com.badlogic.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Brick {
    private Texture texture;
    private Rectangle bounds;
    private  boolean destroyed = false;

    public Brick(float x, float y) {
        texture = new Texture("brick.png");
        bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public void render(SpriteBatch batch){
        if (!destroyed){
            batch.draw(texture, bounds.x, bounds.y);
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
