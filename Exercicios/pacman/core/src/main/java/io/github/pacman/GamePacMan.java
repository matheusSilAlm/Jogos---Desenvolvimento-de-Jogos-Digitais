package io.github.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GamePacMan extends ApplicationAdapter {
    private SpriteBatch batch;
    private Labirinto labirinto;
    private OrthographicCamera camera;
    private PacMan pacMan;


    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        labirinto = new Labirinto();
        pacMan = new PacMan(labirinto);

    }

    @Override
    public void render() {
        ScreenUtils.clear(0,0,0,1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
            labirinto.render(batch);
            pacMan.render(batch);
            for (int i = 0; i< labirinto.getComidas().size; i++){
                Rectangle comida = labirinto.getComidas().get(i);
                if(pacMan.getPacManRect().overlaps(comida)){
                    labirinto.getComidas().removeIndex(i);
                    break;
                }
            }
        batch.end();
        pacMan.atualizaPacMan(labirinto);
    }

    @Override
    public void dispose() {
        batch.dispose();
        labirinto.dispose();

    }
}
