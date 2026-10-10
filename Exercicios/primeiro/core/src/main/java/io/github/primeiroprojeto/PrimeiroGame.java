package io.github.primeiroprojeto;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PrimeiroGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private int y;
    private boolean direcao;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        y = 0;
        direcao = true;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.255f, 0.2f, 0.2f, 1f);
        batch.begin();
            batch.draw(image, 0, y);
            if(y == 406) direcao =  false;
            if (y == -1) direcao = true;

            if(direcao) {
                if (y <= 405) y++;
            }

            else{
                if(if >= 0) y--;
            }
        System.out.println(y);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
