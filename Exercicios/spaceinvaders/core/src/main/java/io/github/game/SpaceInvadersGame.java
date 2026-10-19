package io.github.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class SpaceInvadersGame extends ApplicationAdapter {
    private MyInputProcessor inputProcessor;
    private SpriteBatch batch;
    private Texture img;
    private Texture img_bullet;
    private Texture img_alien;
    private Player player;
    private Alien[] aliens;
    private int NumWidt_aliens = 11;
    private int NumHeight_aliens = 5;
    private int spacing_alien = 40;
    private int minX_aliens;
    private int minY_aliens;
    private int maxX_aliens;
    private int maxY_aliens;
    private Vector2 offset_aliens;
    private int direction_aliens = 1;
    private float speed_aliens = 50;

    private int amount_alive_aliens = 0;

    @Override
    public void create () {
        inputProcessor = new MyInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);
        offset_aliens = Vector2.Zero;
        batch = new SpriteBatch();
        img = new Texture("player.png");
        img_alien = new Texture("alien.png");
        img_bullet = new Texture("bullet.png");
        player = new Player(img, img_bullet, Color.GREEN);
        aliens = new Alien[NumWidt_aliens * NumHeight_aliens];
        int i = 0;
        for(int y = 0 ; y<NumHeight_aliens; y++){
            for(int x = 0 ; x<NumWidt_aliens; x++){
                Vector2 position = new Vector2(x*spacing_alien, y*spacing_alien);
                position.x += Gdx.graphics.getWidth()/2;
                position.y += Gdx.graphics.getHeight();
                position.x -= (NumWidt_aliens/2)*spacing_alien;
                position.y -= (NumHeight_aliens)*spacing_alien;
                aliens[i] = new Alien(position,  img_alien, Color.GREEN);
                i++;
            }
        }
    }

    public void update(float deltaTime) {
        if (inputProcessor.isMoveLeft()) {
            player.moveLeft(deltaTime);
        }
        if (inputProcessor.isMoveRight()) {
            player.moveRight(deltaTime);
        }
    }

    @Override
    public void render () {
        float deltaTime = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0, 0, 0, 1);


        batch.begin();

        player.Draw(batch);
        update(deltaTime);


        for (Alien alien : aliens) {
            if (alien.getAlive()){
                if (player.getSprite_bullet().getBoundingRectangle().overlaps(alien.getSprite().getBoundingRectangle())) {
                    player.getPosition_bullet().y = 10000;
                    alien.setAlive(false);
                    break;
                }
            }

        }
        minX_aliens = 10000;
        minY_aliens = 10000;
        maxX_aliens = 0;
        maxY_aliens = 0;
        // 7,16
        for (int i = 0; i<aliens.length;i++){
            if(aliens[1].getAlive()){
                int IndexX = i%NumWidt_aliens;
                int IndexY = i%NumHeight_aliens;
                if(IndexX > maxX_aliens) maxX_aliens = IndexX;
                if(IndexX < minX_aliens) minX_aliens = IndexX;
                if(IndexY > maxY_aliens) maxY_aliens = IndexY;
                if(IndexY < minY_aliens) minY_aliens = IndexY;
                amount_alive_aliens++;
            }
        }
        if(amount_alive_aliens == 0){
            for (Alien alien : aliens) {
                alien.setAlive(true);
            }
            offset_aliens = new Vector2(0,0);
            batch.end();
            return;
        }

        offset_aliens.x += direction_aliens * deltaTime * speed_aliens;
        if(aliens[maxX_aliens].getPosition().x >= Gdx.graphics.getWidth()){
            offset_aliens.y -= aliens[0].getSprite().getHeight() * aliens[0].getSprite().getScaleY() * 0.25f;
            direction_aliens = -1;
            speed_aliens += 5;
        }
        if(aliens[minX_aliens].getPosition().x <= 0){
            direction_aliens = 1;
            offset_aliens.y -= aliens[0].getSprite().getHeight() * aliens[0].getSprite().getScaleY() * 0.25f;
            speed_aliens += 5;
        }

        if(aliens[minX_aliens].getPosition().y<=0) Gdx.app.exit();

        for (Alien alien : aliens) {
            alien.setPosition(new Vector2(alien.getPosition_initial().x + offset_aliens.x, alien.getPosition_initial().y + offset_aliens.y));
            if (alien.getAlive()) {
                alien.Draw(batch);
                if(alien.getSprite().getBoundingRectangle().overlaps(player.getSprite().getBoundingRectangle())){
                    Gdx.app.exit();
                }
            }
        }
        batch.end();
    }

    @Override
    public void dispose () {
        batch.dispose();
        img_alien.dispose();
        img_bullet.dispose();
        img.dispose();
    }
}

