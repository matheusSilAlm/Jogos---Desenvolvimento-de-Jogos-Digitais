package com.badlogic.pacman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Fantasma {
    private Texture texture;
    private Rectangle fantasmaRect;
    private float speed;

    public Fantasma(float x, float y, Labirinto labirinto) {
        texture = new Texture("fantasma.png");
        fantasmaRect = new Rectangle(x, y, 32,32);
        while (labirinto.colidiuCom(fantasmaRect)){
            reposicionar(labirinto);
        }
    }

    private float calculoDistancia(PacMan pacMan){
        float distX = pacMan.getPacManRect().x - fantasmaRect.x;
        float distY = pacMan.getPacManRect().y - fantasmaRect.y;
        return (float) Math.sqrt(distX * distX - distY*distY);
    }

    public void update(Labirinto labirinto, PacMan pacMan){
        float distancia = calculoDistancia(pacMan);
        Vector2 direcao = new Vector2(
            pacMan.getPacManRect().x - fantasmaRect.x,
            pacMan.getPacManRect().y - fantasmaRect.y
        );
        float fatorAtracao = Math.max(1 - (distancia/300), 0);
        float fatorSpeed = speed * fatorAtracao/8;
        float oldX = fantasmaRect.x;
        float oldY = fantasmaRect.y;
        fantasmaRect.x += direcao.x * fatorSpeed * Gdx.graphics.getDeltaTime();
        fantasmaRect.y += direcao.y * fatorSpeed * Gdx.graphics.getDeltaTime();
        if(labirinto.colidiuCom(fantasmaRect)){
            fantasmaRect.x = oldX;
            fantasmaRect.y = oldY;
        }
    }

    public void render(SpriteBatch batch){
        batch.draw(texture, fantasmaRect.x, fantasmaRect.y);
    }
    public boolean colidiuCom(PacMan pacMan){
        return fantasmaRect.overlaps(pacMan.getPacManRect());
    }
    public void dispose(){
        texture.dispose();
    }
    private void reposicionar(Labirinto labirinto){
        fantasmaRect.x = (float)( Math.random() * (800 - fantasmaRect.width) );
        fantasmaRect.y = (float) ( Math.random() * (600 - fantasmaRect.height) );
        while (labirinto.colidiuCom(fantasmaRect)){
            fantasmaRect.x = (float)( Math.random() * (800 - fantasmaRect.width) );
            fantasmaRect.y = (float) ( Math.random() * (600 - fantasmaRect.height) );
        }
    }

}
