package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class EnemyCar {
    private Texture texture;
//    private Rectangle bounds;
    private float speed;
    private float x;
    private float y;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public EnemyCar(Texture texture, float speed, float x, float y) {
        this.texture = texture;
        this.speed = speed;
        this.x = x;
        this.y = y;
    }

    public void update(float deltaTime){
        y -= speed * deltaTime;
        if (y < -texture.getHeight()){
            y = 800;
            x = 150 + (float) Math.random() * 300;
        }
    }

    public void draw(SpriteBatch batch){
        batch.draw(texture, x, y);
    }
}
