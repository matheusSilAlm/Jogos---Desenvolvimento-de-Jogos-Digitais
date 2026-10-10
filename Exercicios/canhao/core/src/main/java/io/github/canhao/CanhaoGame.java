package io.github.canhao;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class CanhaoGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture cannonTexture, bulletTexture, targetTexture;
    private Rectangle cannon, bullet, target;
    private OrthographicCamera camera;
    private float cannonAngle = 45;
    private boolean bulletFired = false;
    private Vector2 bulletVelocity = new Vector2();
    private final float GRAVITY = -59.8f;



    @Override
    public void create() {
        batch = new SpriteBatch();
        cannonTexture = new Texture("canhao.png");
        bulletTexture = new Texture("projetil.png");
        targetTexture = new Texture("alvo.png");

        camera = new OrthographicCamera();
        camera.setToOrtho(false,800,480);

        cannon = new Rectangle();
        cannon.x = 20;
        cannon.y = 20;
        cannon.width = 64;
        cannon.height = 32;

        target = new Rectangle();
        target.y = 20;
        target.x = 700;
        target.width = 32;
        target.height = 32;

        bullet = new Rectangle();
        bullet.width = 16;
        bullet.height = 16;



    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            cannonAngle = MathUtils.clamp(cannonAngle + 1, 0,90);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            cannonAngle = MathUtils.clamp(cannonAngle - 1, 0,90);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && !bulletFired){
            bullet.x = cannon.x;
            bullet.y = cannon.y;
            float radians = MathUtils.degreesToRadians * cannonAngle;
            bulletVelocity.set(250 * MathUtils.cos(radians), 250 * MathUtils.sin(radians));
            bulletFired = true;

        }

        if (bulletFired){
            float deltaTime = Gdx.graphics.getDeltaTime();
            bulletVelocity.y += GRAVITY * deltaTime;
            bullet.x += bulletVelocity.x * deltaTime;
            bullet.y += bulletVelocity.y * deltaTime;

            if(bullet.overlaps(target)){
                bulletFired = false;
                bullet.x = -100;

                target.y = MathUtils.random(MathUtils.random(0,400)) ;
                System.out.println(target.y);

            }

            if(bullet.y < 0 || bullet.x > camera.viewportWidth){
                bulletFired = false;
                bullet.x = -100;
            }

        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

            batch.draw(cannonTexture, cannon.x, cannon.y, 0, 0,
                        cannon.width,cannon.height, 1, 1,
                        cannonAngle, 0 , 0, cannonTexture.getWidth(), cannonTexture.getHeight()
                        ,false,false);

            if (bulletFired){
                batch.draw(bulletTexture, bullet.x, bullet.y);
            }

            batch.draw(targetTexture, target.x, target.y);

        batch.end();

    }

    @Override
    public void dispose() {
        batch.dispose();
        cannonTexture.dispose();
        bulletTexture.dispose();
        targetTexture.dispose();
    }

}
