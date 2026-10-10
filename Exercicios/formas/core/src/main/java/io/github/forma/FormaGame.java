package io.github.forma;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class FormaGame extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private Rectangle player;
    private float w;
    private float h;

    // Espessura da borda da janela (0.4 unidades do mundo)
    private final float BORDER_SIZE = 0.4f;

    @Override
    public void create() {
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera(30, 30 * (h/w));
        camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
        camera.update();

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);

        player = new Rectangle();
        player.width = 2;
        player.height = 2;
        player.x = (camera.viewportWidth / 2) - (player.width / 2);
        player.y = (camera.viewportHeight / 2) - (player.height / 2);

        System.out.println("camera.viewportWidth: " + camera.viewportWidth);
        System.out.println("camera.viewportHeight: " + camera.viewportHeight);

        System.out.println("player.width: " + player.width);
        System.out.println("player.height: " + player.height);

        System.out.println("Gdx.graphics.getWidth(): " + Gdx.graphics.getWidth());
        System.out.println("Gdx.graphics.getHeight(): " + Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        handleInput();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // 1. Desenha o jogador (quadrado branco)
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);

        // 2. Desenha a borda azul nas quatro extremidades da janela
        shapeRenderer.setColor(Color.BLUE);
        // Borda Inferior
        shapeRenderer.rect(0, 0, camera.viewportWidth, BORDER_SIZE);
        // Borda Superior
        shapeRenderer.rect(0, camera.viewportHeight - BORDER_SIZE, camera.viewportWidth, BORDER_SIZE);
        // Borda Esquerda
        shapeRenderer.rect(0, 0, BORDER_SIZE, camera.viewportHeight);
        // Borda Direita
        shapeRenderer.rect(camera.viewportWidth - BORDER_SIZE, 0, BORDER_SIZE, camera.viewportHeight);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    private void handleInput(){
        float speed = 0.09f;

        // Limites do movimento ajustados para o jogador não ultrapassar a borda da janela
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && (player.x > BORDER_SIZE)){
            player.x -= speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && (player.x < (camera.viewportWidth - player.width - BORDER_SIZE))){
            player.x += speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && (player.y < (camera.viewportHeight - player.height - BORDER_SIZE))){
            player.y += speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && (player.y > BORDER_SIZE)){
            player.y -= speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }
    }
}
