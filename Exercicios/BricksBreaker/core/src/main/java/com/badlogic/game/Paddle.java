package com.badlogic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Paddle {
    private Texture texture;
    private Rectangle bounds;
    private float speed = 300;

    public Paddle() {
        texture = new Texture("paddle.png");
        bounds = new Rectangle(400 - texture.getWidth() / 2,
            50,
            texture.getWidth(),
            texture.getHeight());
    }

    public void update(float delta){
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            bounds.x -= speed * delta;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            bounds.x += speed * delta;
        }
        if(bounds.x <0) bounds.x =0;
        if(bounds.x >800 - bounds.width) bounds.x = 800 - bounds.width;
    }

    public void render(SpriteBatch batch){
        batch.draw(texture, bounds.x, bounds.y);
    }

    public Rectangle getBounds(){
        return bounds;
    }


}
