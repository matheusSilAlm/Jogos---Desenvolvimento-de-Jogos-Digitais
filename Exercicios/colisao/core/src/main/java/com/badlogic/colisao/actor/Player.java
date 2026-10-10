package com.badlogic.colisao.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private Texture texture;
    private Rectangle boundinBox;
    private Vector2 position;
    private float speed;

    public Player(Texture texture, float x, float y) {
        this.texture = texture;
        this.position = new Vector2(x, y);
        this.boundinBox = new Rectangle (x, y, texture.getWidth(), texture.getHeight());
        this.speed = 5f;
    }

    public void update(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            position.x -= speed;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            position.x += speed;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
            position.y += speed;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
            position.y -= speed;
        }
        boundinBox.setPosition(position);
    }

    public Rectangle getBoundinBox() {
        return boundinBox;
    }

    public void dispose(){
        texture.dispose();
    }

    public void render(SpriteBatch batch){
        batch.draw(texture, position.x, position.y);
    }
}
