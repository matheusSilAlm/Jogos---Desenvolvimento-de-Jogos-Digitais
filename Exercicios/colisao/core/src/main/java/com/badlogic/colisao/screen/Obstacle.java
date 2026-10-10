package com.badlogic.colisao.screen;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Obstacle {
    private Rectangle boundinBox;
    private Texture texture;
    private Vector2 position;

    public  Obstacle(Texture texture, float x, float y) {
        this.texture = texture;
        this.position = new Vector2(x, y);
        this.boundinBox = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public Rectangle getBoundinBox () {
       return boundinBox;
    }

    public void dispose () {
       texture.dispose();
    }

    public void render (SpriteBatch batch){
        batch.draw(texture, position.x, position.y);
    }

}
