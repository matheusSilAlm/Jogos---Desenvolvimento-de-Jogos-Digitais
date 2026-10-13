package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TrackRender {
    private float leftX;
    private float rightX;
    private float trackOffset;
    private float trackWidth;
    private float screenWidth;
    private float trackMoveDirection = 1; // Direcao do movimento da pista
    private float trackMoveSpeed = 50; // Velocidade do movimento da pista

    // --- Visual ---
    private static final float STRIPE = 40;      // tamanho de cada bloco da zebra / tracejado
    private static final float CURB_WIDTH = 10;  // largura da zebra
    private static final float DAY_CYCLE = 60f;  // segundos para um ciclo completo dia -> noite -> dia
    private static final Color CURB_RED = new Color(0.85f, 0.15f, 0.15f, 1);

    // Paletas: dia, entardecer, noite, amanhecer
    private static final Color[] GRASS = {
        new Color(0.20f, 0.60f, 0.20f, 1),
        new Color(0.55f, 0.45f, 0.15f, 1),
        new Color(0.03f, 0.10f, 0.08f, 1),
        new Color(0.35f, 0.40f, 0.30f, 1)
    };
    private static final Color[] ROAD = {
        new Color(0.35f, 0.35f, 0.38f, 1),
        new Color(0.35f, 0.28f, 0.28f, 1),
        new Color(0.08f, 0.08f, 0.12f, 1),
        new Color(0.28f, 0.28f, 0.33f, 1)
    };
    // Tom aplicado nos carros (multiplica a cor da textura)
    private static final Color[] AMBIENT = {
        new Color(1f, 1f, 1f, 1),
        new Color(1f, 0.80f, 0.60f, 1),
        new Color(0.35f, 0.40f, 0.65f, 1),
        new Color(0.80f, 0.80f, 0.90f, 1)
    };

    private float scroll; // distancia percorrida (anima zebra e tracejado)
    private float time;
    private final Color grass = new Color(), road = new Color(), ambient = new Color();


    public TrackRender(float initialLeftX, float initialRightX, float trackWidth, float screenWidth){
        this.leftX = initialLeftX;
        this.rightX = initialRightX;
        this.trackWidth = trackWidth;
        this.screenWidth = screenWidth;
        this.trackOffset = 0;
    }

    public void update(float deltaTime, float playerSpeed){
        trackOffset += trackMoveDirection * trackMoveSpeed * deltaTime;


        if(leftX + trackOffset < 0 ){
            trackOffset = -leftX;
            trackMoveDirection = 1;
        }

        if (rightX + trackOffset > screenWidth){
            trackOffset = screenWidth - rightX;
            trackMoveDirection = -1;
        }

        // periodo de 2 blocos: o padrao se repete, entao da pra usar modulo
        scroll = (scroll + playerSpeed * deltaTime) % (STRIPE * 2);
        time += deltaTime;
        updatePalette();
    }

    /** Interpola as cores entre as fases do dia. */
    private void updatePalette() {
        float phase = (time % DAY_CYCLE) / DAY_CYCLE * GRASS.length;
        int a = (int) phase;
        int b = (a + 1) % GRASS.length;
        float t = phase - a;
        grass.set(GRASS[a]).lerp(GRASS[b], t);
        road.set(ROAD[a]).lerp(ROAD[b], t);
        ambient.set(AMBIENT[a]).lerp(AMBIENT[b], t);
    }

    public void draw(ShapeRenderer shapeRenderer){
        float h = Gdx.graphics.getHeight();
        float l = getLeftBound();
        float r = getRightBound();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Grama e asfalto
        shapeRenderer.setColor(grass);
        shapeRenderer.rect(0, 0, screenWidth, h);
        shapeRenderer.setColor(road);
        shapeRenderer.rect(l, 0, r - l, h);

        // Zebras (descem conforme a velocidade -> sensacao de movimento)
        int i = 0;
        for (float y = -scroll; y < h; y += STRIPE, i++) {
            shapeRenderer.setColor(i % 2 == 0 ? CURB_RED : Color.WHITE);
            shapeRenderer.rect(l - CURB_WIDTH, y, CURB_WIDTH, STRIPE);
            shapeRenderer.rect(r, y, CURB_WIDTH, STRIPE);
        }

        // Tracejado entre as faixas
        shapeRenderer.setColor(Color.WHITE);
        float laneWidth = (r - l) / EnemyCar.LANES;
        for (int lane = 1; lane < EnemyCar.LANES; lane++) {
            float x = l + lane * laneWidth;
            for (float y = -scroll; y < h; y += STRIPE * 2) {
                shapeRenderer.rect(x - 2, y, 4, STRIPE);
            }
        }

        shapeRenderer.end();
    }

    /** Cor para multiplicar nos sprites (escurece os carros a noite). */
    public Color getAmbient() {
        return ambient;
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
