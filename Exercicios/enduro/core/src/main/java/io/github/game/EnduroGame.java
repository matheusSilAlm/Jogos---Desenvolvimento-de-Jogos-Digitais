package io.github.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class EnduroGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private TrackRender trackRender;
    private Texture playerCarTexture, enemyCarTexture;
    private float playerX, playerY;
    private List<EnemyCar> enemyCars;



    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        float initialLeftX = 150;
        float initialRightX = 450;
        float trackWidth = 300;
        float screenWidth = Gdx.graphics.getWidth();
        trackRender = new TrackRender(initialLeftX, initialRightX, trackWidth, screenWidth);


        playerCarTexture = new Texture("car.png");
        enemyCarTexture = new Texture("enemy_car.png");


        playerX = 300;
        playerY = 50;

        enemyCars = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            float x = MathUtils.random(initialLeftX, initialRightX - enemyCarTexture.getWidth());
            float y = MathUtils.random(200, 800);
            float speed = MathUtils.random(100, 200);
            enemyCars.add(new EnemyCar(enemyCarTexture, speed, x, y));
        }
    }

    private void handelInput() {
        float playerSpeed = 200 * Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= playerSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += playerSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerY += playerSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            playerY -= playerSpeed;
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        trackRender.update(Gdx.graphics.getDeltaTime());
        handelInput();
        trackRender.draw(shapeRenderer);

        batch.begin();
        float leftBound = trackRender.getLeftBound();
        float rightBound = trackRender.getRightBound() - playerCarTexture.getWidth();
        playerX = MathUtils.clamp(playerX, leftBound, rightBound);
        batch.draw(playerCarTexture, playerX, playerY);
        for(EnemyCar enemyCar : enemyCars){
            enemyCar.update(Gdx.graphics.getDeltaTime());

            float ajustX = enemyCar.getX() + trackRender.getTrackOffset();
            ajustX = MathUtils.clamp(ajustX, leftBound, rightBound);
            batch.draw(enemyCarTexture, ajustX, enemyCar.getY());
        }
        batch.end();

    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        playerCarTexture.dispose();
        enemyCarTexture.dispose();
    }
}

