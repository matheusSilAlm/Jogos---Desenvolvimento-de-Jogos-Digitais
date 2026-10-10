package io.github.colisao;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.colisao.actor.Player;
import io.github.colisao.screen.Obstacle;

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
        player = new Player(new Texture("player.png"), 100,100);
        obstacles = new ArrayList<>();
        for(int i = 0; i <10; i++ ){
            float x, y;
            x = random.nextInt(Gdx.graphics.getWidth());
            y = random.nextInt(Gdx.graphics.getWidth());
            obstacles.add(new Obstacle(new Texture("obstacle.png"), x, y));
        }
//        obstacle = new Obstacle(new Texture("obstacle.png"), 200, 200);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1,1,1,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update();

        batch.begin();


            for (Obstacle obstacle : obstacles){
                obstacle.render(batch);
                if(player.getBoudinBox().overlaps(obstacle.getBoudingBox())){
                    System.out.println("BATEU");
                }
            }

            player.render(batch);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
        for (Obstacle obstacle : obstacles){
            obstacle.dispose();
        }

    }
}
