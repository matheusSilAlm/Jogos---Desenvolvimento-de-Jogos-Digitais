package com.badlogic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class GameScreen {
    private Ball ball;
    private Paddle paddle;
    private Array<Brick> bricks;
    private boolean isGameOver = false;

    public GameScreen() {
        ball = new Ball();
        paddle = new Paddle();
        bricks = new Array<>();

        for(int i=0 ; i<5; i++){
            for(int j = 0; j<10; j++){
                bricks.add(new Brick(60 + j * 70, 400 + i * 30));
            }
        }
    }

    public void update(){
        if(isGameOver) return;

        float delta = Gdx.graphics.getDeltaTime();

        ball.update(delta);
        paddle.update(delta);

        if(ball.getBounds().overlaps(paddle.getBounds())){
            ball.reverseY();
            //Calcular a onde a bola bateu na raquete para variar o ângulo de rebate
            float hitPosition = (ball.getBounds().x + ball.getBounds().width/2) -
                (paddle.getBounds().x + paddle.getBounds().width / 2);
            ball.getVelocity().x = hitPosition * 5;
        }

        for(Brick brick : bricks){
            if(!brick.isDestroyed() && ball.getBounds().overlaps(brick.getBounds())){
                brick.setDestroyed(true);
                ball.reverseY();
            }
        }

        if(ball.getBounds().y < 0){
            isGameOver = true;
        }
    }

    public void render(SpriteBatch batch){
        batch.begin();
        ball.render(batch);
        paddle.render(batch);
        for(Brick brick: bricks){
            brick.render(batch);
        }
        batch.end();
    }

    public boolean isGameOver(){
        return isGameOver;
    }

}
