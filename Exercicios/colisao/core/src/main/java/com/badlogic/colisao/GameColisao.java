package com.badlogic.colisao;

import com.badlogic.colisao.actor.Player;
import com.badlogic.colisao.screen.Obstacle;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.math.MathUtils.random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GameColisao extends ApplicationAdapter {
    private SpriteBatch batch;
    private Player player;
    private List<Obstacle> obstacles;

    @Override
    public void create() {
        batch = new SpriteBatch();
        player = new Player(new Texture( "player.png"), 100, 100);
        obstacles = new ArrayList<>();
        // obstacle = new Obstacle(new Texture( "obstacle.png"), 50, 50);
        for(int i=0; i<10; i++){
            float x, y;
            x = random.nextInt(Gdx.graphics.getWidth());
            y = random.nextInt(Gdx.graphics.getHeight());
            obstacles.add(new Obstacle(new Texture("obstacle.png"), x, y));
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update();

        batch.begin();
            player.render(batch);
            for (Obstacle obstacle: obstacles) {
                obstacle.render(batch);
                if(player.getBoundinBox().overlaps(obstacle.getBoundinBox())) {
                    System.out.println("Houve Colisao!!!!");
                }
            }
        batch.end();

    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
          for(Obstacle obstacle: obstacles) {
              obstacle.dispose();
          }
    }
}
