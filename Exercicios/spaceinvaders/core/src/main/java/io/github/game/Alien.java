package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Alien {
    private Vector2 position;
    private Vector2 position_initial;
    private Sprite sprite;
    private Boolean Alive = true;

    public Alien(Vector2 _position, Texture img, Color color){
        position = _position;
        position_initial = _position;
        sprite = new Sprite(img);
        sprite.setScale(4);
        sprite.setColor(color);
    }

    public void Draw(SpriteBatch batch){
        //Update(Gdx.graphics.getDeltaTime());
        sprite.setPosition(position.x, position.y);
        sprite.draw(batch);
        //sprite_bullet.setPosition(position_bullet.x, position_bullet.y);
        //sprite_bullet.draw(batch);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public Boolean getAlive() {
        return Alive;
    }

    public void setAlive(Boolean alive) {
        Alive = alive;
    }

    public Vector2 getPosition_initial() {
        return position_initial;
    }

    public void setPosition_initial(Vector2 position_initial) {
        this.position_initial = position_initial;
    }
}
