package com.badlogic.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Ball {
    private Texture texture;
    private Vector2 position;
    private Vector2 velocity;
    private Rectangle bounds;

    public Ball() {
        texture = new Texture("ball.png");
        position = new Vector2(400, 300);
        velocity = new Vector2(200, 200);
        bounds = new Rectangle(position.x, position.y, texture.getWidth(), texture.getHeight());
    }

    public void update(float delta){
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        if(position.x < 0 || position.x > 800 - texture.getWidth()){
            velocity.x = -velocity.x;
        }
        if(position.y < 0 || position.y > 600 - texture.getHeight()){
            velocity.y = -velocity.y;;
        }
        bounds.setPosition(position);
    }

    public void render(SpriteBatch batch){
        batch.draw(texture, position.x, position.y);
    }

    public Rectangle getBounds(){
        return bounds;
    }

    public void reverseY(){
        velocity.y = -velocity.y;
    }

    public Vector2 getVelocity(){
        return velocity;
    }
}
