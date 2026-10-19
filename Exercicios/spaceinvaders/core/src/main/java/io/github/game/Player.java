package io.github.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private Vector2 position;
    private Vector2 position_bullet;
    private Sprite sprite;
    private Sprite sprite_bullet;
    private float speed = 200L;
    private float speed_bullet = 1000L;

    public Player(Texture img, Texture img_bullet, Color color) {
        sprite = new Sprite(img);
        sprite_bullet = new Sprite(img_bullet);
        sprite_bullet.setScale(4);
        sprite.setScale(4);
        sprite_bullet.setColor(color);
        sprite.setColor(color);
        position = new Vector2((float) Gdx.graphics.getWidth() /2, (sprite.getScaleY()*sprite.getHeight()) /2);
        position_bullet = new Vector2(0,10000);
    }

    public void Update(float deltaTime){

        if(Gdx.input.isKeyPressed(Input.Keys.SPACE) &&
            position_bullet.y>=Gdx.graphics.getHeight()){
              position_bullet.x = position.x+3;
            position_bullet.y = 0;
        }
        position_bullet.y += deltaTime * speed_bullet;
    }

    public void moveLeft(float deltaTime) {
        position.x -= speed * deltaTime;
        if(position.x-(sprite.getWidth()*sprite.getScaleX()/2) <=0) position.x = (sprite.getWidth()*sprite.getScaleX()/2);
        if(position.y+(sprite.getWidth()*sprite.getScaleX()/2) >= Gdx.graphics.getWidth()) position.y = Gdx.graphics.getWidth() - (sprite.getWidth()*sprite.getScaleX()/2);
        System.out.println("LEFT:" + position.x);
    }

    public void moveRight(float deltaTime) {
        position.x += speed * deltaTime;
        if(position.x-(sprite.getWidth()*sprite.getScaleX()/2) <=0) position.x = (sprite.getWidth()*sprite.getScaleX()/2);
        if(position.y+(sprite.getWidth()*sprite.getScaleX()/2) >= Gdx.graphics.getWidth()) position.y = Gdx.graphics.getWidth() - (sprite.getWidth()*sprite.getScaleX()/2);
        System.out.println("RIGHT:" + position.x);
    }

    public void Draw(SpriteBatch batch){
        Update(Gdx.graphics.getDeltaTime());
        sprite.setPosition(position.x, position.y);
        sprite.draw(batch);
        sprite_bullet.setPosition(position_bullet.x, position_bullet.y);
        sprite_bullet.draw(batch);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getPosition_bullet() {
        return position_bullet;
    }

    public void setPosition_bullet(Vector2 position_bullet) {
        this.position_bullet = position_bullet;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public Sprite getSprite_bullet() {
        return sprite_bullet;
    }

    public void setSprite_bullet(Sprite sprite_bullet) {
        this.sprite_bullet = sprite_bullet;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed_bullet() {
        return speed_bullet;
    }

    public void setSpeed_bullet(float speed_bullet) {
        this.speed_bullet = speed_bullet;
    }
}
