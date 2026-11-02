package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Random;

public class GameScreen extends ScreenAdapter {

    // ---------- Recursos gráficos ----------
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture blockTexture;
    private Texture backgroundTexture;
    private Texture gameOverOverlay;

    // ---------- Estado do jogo ----------
    private Board board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;

    private float dropTimer;
    private float dropInterval = Constants.BASE_DROP_INTERVAL;
    private int score;
    private int lines;
    private int level = 1;
    private boolean gameOver;

    private final Random random = new Random();

    // ---------- Posicionamento na tela ----------
    private final int boardOffsetX = 50;
    private final int boardOffsetY = 30;

    // ---------- DAS / ARR (Delayed Auto Shift) ----------
    // Tempo que a seta precisa ficar pressionada antes de começar a repetir
    private static final float MOVE_DAS_DELAY = 0.20f;
    // Intervalo entre cada movimento repetido após o DAS
    private static final float MOVE_ARR_DELAY = 0.08f;
    // Intervalo entre cada queda no soft-drop (seta para baixo segurada)
    private static final float SOFT_DROP_INTERVAL = 0.05f;

    private boolean leftHeld = false;
    private float leftTimer = 0f;

    private boolean rightHeld = false;
    private float rightTimer = 0f;

    private boolean downHeld = false;
    private float downTimer = 0f;

    // =====================================================================
    // CICLO DE VIDA
    // =====================================================================

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        blockTexture = new Texture("blocoTetris.png");
        blockTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        backgroundTexture = new Texture("campoTetris.png");
//        gameOverOverlay = new Texture("game_over_overlay.png");

        board = new Board();
        nextPiece = new Tetromino(getRandomType());
        spawnNewPiece();
        gameOver = false;
        score = 0;
        lines = 0;
        level = 1;
        dropInterval = Constants.BASE_DROP_INTERVAL;
        dropTimer = 0;

        // reset de estado do DAS/ARR (importante para o restart via R)
        leftHeld = false;  leftTimer = 0;
        rightHeld = false; rightTimer = 0;
        downHeld = false;  downTimer = 0;
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (blockTexture != null) blockTexture.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (gameOverOverlay != null) gameOverOverlay.dispose();
    }

    // =====================================================================
    // SPAWN DE PEÇAS
    // =====================================================================

    private void spawnNewPiece() {
        currentPiece = nextPiece;
        nextPiece = new Tetromino(getRandomType());

        if (!board.isValidPosition(currentPiece.getShape(),
            currentPiece.getX(), currentPiece.getY())) {
            gameOver = true;
        }
    }

    private TetrominoType getRandomType() {
        TetrominoType[] types = TetrominoType.values();
        return types[random.nextInt(types.length)];
    }

    // =====================================================================
    // RENDER
    // =====================================================================

    @Override
    public void render(float delta) {
        handleInput(delta);

        if (!gameOver) {
            update(delta);
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Fundo do tabuleiro
        batch.setColor(Color.WHITE);
        batch.draw(backgroundTexture, boardOffsetX, boardOffsetY);

        // Blocos fixos no tabuleiro
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Color color = board.getCell(r, c);
                if (color == null) continue;
                float x = boardOffsetX + c * Constants.BLOCK_SIZE;
                float y = boardOffsetY + r * Constants.BLOCK_SIZE;
                batch.setColor(color);
                batch.draw(blockTexture, x, y,
                    Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
            }
        }

        // Peça atual
        if (!gameOver) {
            int[][] shape = currentPiece.getShape();
            int posX = currentPiece.getX();
            int posY = currentPiece.getY();
            batch.setColor(currentPiece.getColor());
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[0].length; c++) {
                    if (shape[r][c] == 0) continue;
                    int boardRow = posY + (shape.length - 1 - r);
                    int boardCol = posX + c;
                    float x = boardOffsetX + boardCol * Constants.BLOCK_SIZE;
                    float y = boardOffsetY + boardRow * Constants.BLOCK_SIZE;
                    batch.draw(blockTexture, x, y,
                        Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
                }
            }
        }

        // HUD
        batch.setColor(Color.WHITE);
        int hudX = boardOffsetX + Constants.BOARD_COLS * Constants.BLOCK_SIZE + 30;
        font.setColor(Color.WHITE);
        font.draw(batch, "Pontos: " + score, hudX, Constants.SCREEN_HEIGHT - 30);
        font.draw(batch, "Linhas: " + lines, hudX, Constants.SCREEN_HEIGHT - 60);
        font.draw(batch, "Nivel: " + level, hudX, Constants.SCREEN_HEIGHT - 90);
        font.draw(batch, "Proximo:", hudX, Constants.SCREEN_HEIGHT - 140);

        // Prévia da próxima peça
        int[][] nextShape = nextPiece.getShape();
        batch.setColor(nextPiece.getColor());
        int previewX = hudX;
        int previewY = Constants.SCREEN_HEIGHT - 180;
        for (int r = 0; r < nextShape.length; r++) {
            for (int c = 0; c < nextShape[0].length; c++) {
                if (nextShape[r][c] == 0) continue;
                float x = previewX + c * Constants.BLOCK_SIZE;
                float y = previewY - r * Constants.BLOCK_SIZE;
                batch.draw(blockTexture, x, y,
                    Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
            }
        }

        // Overlay de game over
//        if (gameOver) {
//            batch.setColor(Color.WHITE);
//            batch.draw(gameOverOverlay, 0, 0);
//            font.setColor(Color.RED);
//            font.draw(batch, "FIM DO JOGO",
//                boardOffsetX + 50, Constants.SCREEN_HEIGHT / 2 + 20);
//            font.setColor(Color.WHITE);
//            font.draw(batch, "Pressione R para iniciar",
//                boardOffsetX + 30, Constants.SCREEN_HEIGHT / 2 - 20);
//        }

        batch.end();
    }

    // =====================================================================
    // INPUT (com DAS/ARR)
    // =====================================================================

    private void handleInput(float delta) {
        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                dispose();
                show();
            }
            return;
        }

        // Rotação (instantânea — a cada toque)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            currentPiece.rotate(board);
        }

        // Hard drop (queda instantânea — a cada toque)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            while (currentPiece.moveDown(board)) { /* desce ao fundo */ }
            lockPiece();
        }

        // Movimento horizontal com DAS/ARR
        handleHorizontal(delta);

        // Soft drop com repetição controlada
        handleSoftDrop(delta);
    }

    /** Trata ← e → com DAS (delay inicial) e ARR (repetição contínua). */
    private void handleHorizontal(float delta) {
        boolean leftPressed  = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        // Se ambas as setas estão pressionadas ao mesmo tempo, ignora as duas
        // (evita "vibração" da peça).
        if (leftPressed && rightPressed) {
            leftHeld = false;  leftTimer = 0;
            rightHeld = false; rightTimer = 0;
            return;
        }

        // ---------- ESQUERDA ----------
        if (leftPressed) {
            if (!leftHeld) {
                // Primeiro frame: move 1 célula imediatamente
                currentPiece.moveLeft(board);
                leftHeld = true;
                leftTimer = 0f;
            } else {
                leftTimer += delta;
                if (leftTimer >= MOVE_DAS_DELAY
                    && leftTimer - MOVE_DAS_DELAY >= MOVE_ARR_DELAY) {
                    currentPiece.moveLeft(board);
                    // Reancora o timer para o próximo repeat vir após ARR
                    leftTimer = MOVE_DAS_DELAY;
                }
            }
        } else {
            leftHeld = false;
            leftTimer = 0f;
        }

        // ---------- DIREITA ----------
        if (rightPressed) {
            if (!rightHeld) {
                currentPiece.moveRight(board);
                rightHeld = true;
                rightTimer = 0f;
            } else {
                rightTimer += delta;
                if (rightTimer >= MOVE_DAS_DELAY
                    && rightTimer - MOVE_DAS_DELAY >= MOVE_ARR_DELAY) {
                    currentPiece.moveRight(board);
                    rightTimer = MOVE_DAS_DELAY;
                }
            }
        } else {
            rightHeld = false;
            rightTimer = 0f;
        }
    }

    /** Soft drop (seta ↓) com repetição controlada em SOFT_DROP_INTERVAL. */
    private void handleSoftDrop(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (!downHeld) {
                // Primeiro frame: desce 1 célula
                downHeld = true;
                downTimer = 0f;
                if (currentPiece.moveDown(board)) dropTimer = 0f;
            } else {
                downTimer += delta;
                if (downTimer >= SOFT_DROP_INTERVAL) {
                    downTimer -= SOFT_DROP_INTERVAL;
                    if (currentPiece.moveDown(board)) dropTimer = 0f;
                }
            }
        } else {
            downHeld = false;
            downTimer = 0f;
        }
    }

    // =====================================================================
    // UPDATE (gravidade automática)
    // =====================================================================

    private void update(float delta) {
        dropTimer += delta;
        if (dropTimer >= dropInterval) {
            dropTimer = 0;
            if (!currentPiece.moveDown(board)) {
                lockPiece();
            }
        }
    }

    private void lockPiece() {
        board.placePiece(currentPiece);

        int cleared = board.clearLines();
        if (cleared > 0) {
            lines += cleared;
            switch (cleared) {
                case 1:
                    score += 100;
                    break;
                case 2:
                    score += 300;
                    break;
                case 3:
                    score += 500;
                    break;
                case 4:
                    score += 800;
                    break;
            }
            level = 1 + lines / 10;
            dropInterval = Constants.BASE_DROP_INTERVAL *
                (float) Math.pow(Constants.DROP_SPEED_FACTOR, level - 1);
        }

        spawnNewPiece();
    }
}
