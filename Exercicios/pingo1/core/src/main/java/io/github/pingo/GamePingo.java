package io.github.pingo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GamePingo extends ApplicationAdapter {
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont fontGrande;
    private BitmapFont fontPequena;



    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        viewport.apply();
        camera.position.set(viewport.getWorldHeight()/2,viewport.getWorldHeight()/2,0);
        camera.update();
        batch = new SpriteBatch();
        fontGrande = new BitmapFont(Gdx.files.internal("fonts/minhafontegrande.fnt"));
        fontPequena = new BitmapFont(Gdx.files.internal("fonts/minhafontepequena.fnt"));
        fontGrande.setColor(Color.RED);
        fontPequena.setColor(Color.GREEN);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
            String titulo = "Anuncie aqui";
            GlyphLayout layout = new GlyphLayout(fontGrande,titulo);
            float x = (viewport.getWorldWidth() - layout.width)/2;
            float y = 420f;
            fontGrande.draw(batch,layout,x,y);

            String outroTexto = "Anuncie aqui agora";
            layout.setText(fontPequena,outroTexto);

            y = 240f;
            fontPequena.draw(batch,layout,x,y);
        batch.end();
    }

    @Override
    public void resize(int w, int h){
        viewport.update(w, h);
        camera.position.set(viewport.getWorldHeight()/2,viewport.getWorldHeight()/2,0);
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        fontGrande.dispose();
        fontPequena.dispose();
    }
}
