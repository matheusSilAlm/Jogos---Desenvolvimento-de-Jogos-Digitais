package com.badlogic.pacman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class PacMan {
    private Texture texture;
    private Rectangle pacManRect;
    private float speed;

    public PacMan(Labirinto labirinto) {
        texture = new Texture("pacman.png");
        pacManRect = new Rectangle(64,64,32,32);
        speed = 100f;
        while(labirinto.colidiuCom(pacManRect)){
            reposicionar(labirinto);
        }
    }

    private void reposicionar(Labirinto labirinto){
        pacManRect.x = (float)( Math.random() * (800 - pacManRect.width) );
        pacManRect.y = (float) ( Math.random() * (600 - pacManRect.height) );
        while (labirinto.colidiuCom(pacManRect)){
            pacManRect.x = (float)( Math.random() * (800 - pacManRect.width) );
            pacManRect.y = (float) ( Math.random() * (600 - pacManRect.height) );
        }
    }

    public void atualizaPacMan(Labirinto labirinto){
        float oldX = pacManRect.x;
        float oldY = pacManRect.y;
        float deltaTime = Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            pacManRect.x -= speed * deltaTime;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            pacManRect.x += speed * deltaTime;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP)){
            pacManRect.y += speed * deltaTime;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            pacManRect.y -= speed * deltaTime;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }

        if(labirinto.colidiuCom(pacManRect)){
            pacManRect.x = oldX;
            pacManRect.y = oldY;
        }

    }

    public void render(SpriteBatch batch){
        batch.draw(texture, pacManRect.x, pacManRect.y);
    }

    public  Rectangle getPacManRect(){
        return pacManRect;
    }

    public void dispose(){
        texture.dispose();
    }

}

