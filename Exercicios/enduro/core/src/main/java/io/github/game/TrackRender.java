package io.github.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TrackRender {
    private float leftX;
    private float rightX;
    private float trackOffset;
    private float trackWidth;
    private float screenWidth;
    private float trackMoveDirection = 1; // Direcao do movimento da pista
    private float trackMoveSpeed = 50; // Velocidade do movimento da pista


    public TrackRender(float initialLeftX, float initialRightX, float trackWidth, float screenWidth){
        this.leftX = initialLeftX;
        this.rightX = initialRightX;
        this.trackWidth = trackWidth;
        this.screenWidth = screenWidth;
        this.trackOffset = 0;
    }

    public void update(float deltaTime){
        trackOffset += trackMoveDirection * trackMoveSpeed * deltaTime;


        if(leftX + trackOffset < 0 ){
            trackOffset = -leftX;
            trackMoveDirection = 1;
        }

        if (rightX + trackOffset > screenWidth){
            trackOffset = screenWidth - rightX;
            trackMoveDirection = -1;
        }
    }

    public void draw(ShapeRenderer shapeRenderer){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.line(leftX + trackOffset, 0 , leftX + trackOffset, 800);
        shapeRenderer.line(rightX + trackOffset, 0, rightX + trackOffset, 800);
        shapeRenderer.end();
    }

    public float getTrackOffset(){
        return trackOffset;
    }

    public float getLeftBound(){
        return leftX + trackOffset;

    }

    public float getRightBound(){
        return rightX + trackOffset;
    }
}
