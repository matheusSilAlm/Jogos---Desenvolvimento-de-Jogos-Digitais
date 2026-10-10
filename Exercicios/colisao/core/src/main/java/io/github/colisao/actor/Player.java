package io.github.colisao.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private Texture texture;
    private Rectangle boudinBox;
    private Vector2 position;
    private float speed;


    public Player(Texture texture,float x, float y) {
        this.texture = texture;
        this.position = new Vector2(x, y);
        this.boudinBox = new Rectangle(x,y,texture.getWidth(), texture.getHeight());
        this.speed = 5f;
    }

    public void update(){
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            position.x -= speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            position.x += speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP)){
            position.y += speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            position.y -= speed;
        }

        boudinBox.setPosition(position);
    }

    public Rectangle getBoudinBox() {
        return boudinBox;
    }

    public void dispose(){
        texture.dispose();
    }

    public void render(SpriteBatch batch){
        batch.draw(texture, position.x, position.y);

    }

}


