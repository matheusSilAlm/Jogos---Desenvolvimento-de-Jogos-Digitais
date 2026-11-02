package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * GameScreen Arcade Sovietico Completo:
 * SFX, Screen Shake, Hold Piece, Linhas com Flash e Particulas,
 * Textos Flutuantes, Combos, Modo Desafio e Nivel Inicial.
 */
public class GameScreen extends ScreenAdapter {

    private final TetrisGame game;

    // Graficos
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture blockTexture;
    private Texture fieldTexture;
    private Texture gameBg;
    private Texture gameOverBox;
    private Texture pauseBox;
    private Texture pixelTexture;

    // Estado do jogo
    private Board board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private TetrominoType holdPiece;
    private boolean canHold;

    private float dropTimer;
    private float dropInterval;
    private int score;
    private int lines;
    private int level;
    private int highScore;
    private int combo;
    private boolean gameOver;
    private boolean isPaused;

    // Visual Juice: Particulas, Flash de Linhas e Textos Flutuantes
    private final List<Particle> particles = new ArrayList<Particle>();
    private final List<FloatingText> floatingTexts = new ArrayList<FloatingText>();
    private List<Integer> clearingRows = new ArrayList<Integer>();
    private float clearingTimer = 0f;

    // Screen Shake
    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;

    private final Random random = new Random();

    // Posicionamento do tabuleiro
    private final int boardOffsetX = Constants.BOARD_OFFSET_X;
    private final int boardOffsetY = Constants.BOARD_OFFSET_Y;

    // DAS / ARR
    private static final float MOVE_DAS_DELAY = 0.20f;
    private static final float MOVE_ARR_DELAY = 0.08f;
    private static final float SOFT_DROP_INTERVAL = 0.05f;

    private boolean leftHeld = false;
    private float leftTimer = 0f;

    private boolean rightHeld = false;
    private float rightTimer = 0f;

    private boolean downHeld = false;
    private float downTimer = 0f;

    public GameScreen(TetrisGame game) {
        this.game = game;
    }

    public GameScreen() {
        this(null);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        try {
            font = new BitmapFont(Gdx.files.internal("retro_font.fnt"));
        } catch (Exception e) {
            font = new BitmapFont();
        }
        font.getData().setScale(1.0f);

        blockTexture = new Texture("blocoTetris.png");
        blockTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        fieldTexture = new Texture("campoTetris.png");
        fieldTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        gameBg = new Texture("game_bg.png");
        gameBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        gameOverBox = new Texture("game_over_box.png");
        pauseBox = new Texture("pause_box.png");

        // 1x1 textura branca para particulas e flash
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        pixelTexture = new Texture(pix);
        pix.dispose();

        if (game != null && game.getPreferences() != null) {
            highScore = game.getPreferences().getInteger("highScore", 0);
        }

        initGame();

        if (game != null && game.getAudioManager() != null) {
            game.getAudioManager().playGameMusic();
        }
    }

    private void initGame() {
        if (game != null && game.getPreferences() != null) {
            highScore = game.getPreferences().getInteger("highScore", 0);
        }
        board = new Board();
        particles.clear();
        floatingTexts.clear();
        clearingRows.clear();
        clearingTimer = 0f;
        shakeTimer = 0f;

        holdPiece = null;
        canHold = true;
        combo = -1;

        level = game != null ? game.getStartingLevel() : 1;
        dropInterval = Constants.BASE_DROP_INTERVAL *
            (float) Math.pow(Constants.DROP_SPEED_FACTOR, level - 1);

        // Modo Desafio Soviético (blocos de entulho na base)
        if (game != null && game.isChallengeMode()) {
            Color[] pal = new Color[] {
                Color.CYAN, Color.GOLD, Color.PURPLE, Color.GREEN, Color.RED, Color.BLUE, Color.ORANGE
            };
            board.generateGarbageRows(6, pal, random);
            floatingTexts.add(new FloatingText("DESAFIO SOVIETICO!", boardOffsetX + 150, boardOffsetY + 350, Color.RED, 2.0f));
        }

        nextPiece = new Tetromino(getRandomType());
        spawnNewPiece();
        gameOver = false;
        isPaused = false;
        score = 0;
        lines = 0;
        dropTimer = 0;

        leftHeld = false;  leftTimer = 0;
        rightHeld = false; rightTimer = 0;
        downHeld = false;  downTimer = 0;
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (blockTexture != null) blockTexture.dispose();
        if (fieldTexture != null) fieldTexture.dispose();
        if (gameBg != null) gameBg.dispose();
        if (gameOverBox != null) gameOverBox.dispose();
        if (pauseBox != null) pauseBox.dispose();
        if (pixelTexture != null) pixelTexture.dispose();
    }

    private void spawnNewPiece() {
        currentPiece = nextPiece;
        nextPiece = new Tetromino(getRandomType());
        canHold = true;

        if (!board.isValidPosition(currentPiece.getShape(),
            currentPiece.getX(), currentPiece.getY())) {
            triggerGameOver();
        }
    }

    private void triggerGameOver() {
        gameOver = true;
        checkHighScore();
        if (game != null && game.getAudioManager() != null) {
            game.getAudioManager().stopAll();
            game.getAudioManager().playGameOver();
        }
        shake(0.4f, 6.0f);
    }

    private void checkHighScore() {
        if (score > highScore) {
            highScore = score;
            if (game != null && game.getPreferences() != null) {
                game.getPreferences().putInteger("highScore", highScore);
                game.getPreferences().flush();
            }
        }
    }

    private void shake(float duration, float intensity) {
        this.shakeTimer = duration;
        this.shakeIntensity = intensity;
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

        if (!gameOver && !isPaused) {
            update(delta);
        }

        // Atualizar juice
        if (shakeTimer > 0) shakeTimer -= delta;

        Iterator<Particle> pIt = particles.iterator();
        while (pIt.hasNext()) {
            if (!pIt.next().update(delta)) pIt.remove();
        }

        Iterator<FloatingText> tIt = floatingTexts.iterator();
        while (tIt.hasNext()) {
            if (!tIt.next().update(delta)) tIt.remove();
        }

        // Calculo de Screen Shake
        float sx = 0, sy = 0;
        if (shakeTimer > 0) {
            sx = (random.nextFloat() * 2f - 1f) * shakeIntensity;
            sy = (random.nextFloat() * 2f - 1f) * shakeIntensity;
        }

        Gdx.gl.glClearColor(0.05f, 0.06f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // 1. Moldura Arcade
        batch.setColor(Color.WHITE);
        batch.draw(gameBg, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // 2. Tabuleiro com tremor (Screen Shake)
        float curBoardX = boardOffsetX + sx;
        float curBoardY = boardOffsetY + sy;

        batch.draw(fieldTexture, curBoardX, curBoardY,
            Constants.BOARD_COLS * Constants.BLOCK_SIZE,
            Constants.BOARD_ROWS * Constants.BLOCK_SIZE);

        // 3. Blocos fixos no tabuleiro
        for (int r = 0; r < board.getRows(); r++) {
            boolean isClearing = clearingRows.contains(r);
            for (int c = 0; c < board.getCols(); c++) {
                Color color = board.getCell(r, c);
                if (color == null) continue;
                float x = curBoardX + c * Constants.BLOCK_SIZE;
                float y = curBoardY + r * Constants.BLOCK_SIZE;

                if (isClearing) {
                    // Flash branco/dourado na eliminacao de linha
                    float flash = (clearingTimer * 20f) % 2f > 1f ? 1f : 0.4f;
                    batch.setColor(flash, flash, 0.5f + flash * 0.5f, 1f);
                } else {
                    batch.setColor(color);
                }
                batch.draw(blockTexture, x, y,
                    Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
            }
        }

        // 4. Ghost Piece
        if (!gameOver && !isPaused && currentPiece != null && clearingTimer <= 0) {
            int ghostY = currentPiece.getY();
            while (board.isValidPosition(currentPiece.getShape(), currentPiece.getX(), ghostY - 1)) {
                ghostY--;
            }
            if (ghostY != currentPiece.getY()) {
                int[][] shape = currentPiece.getShape();
                Color c = currentPiece.getColor();
                batch.setColor(c.r, c.g, c.b, 0.32f);
                for (int r = 0; r < shape.length; r++) {
                    for (int col = 0; col < shape[0].length; col++) {
                        if (shape[r][col] == 0) continue;
                        int boardRow = ghostY + (shape.length - 1 - r);
                        int boardCol = currentPiece.getX() + col;
                        float gx = curBoardX + boardCol * Constants.BLOCK_SIZE;
                        float gy = curBoardY + boardRow * Constants.BLOCK_SIZE;
                        batch.draw(blockTexture, gx, gy,
                            Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
                    }
                }
            }
        }

        // 5. Peca ativa em queda
        if (!gameOver && currentPiece != null && clearingTimer <= 0) {
            int[][] shape = currentPiece.getShape();
            int posX = currentPiece.getX();
            int posY = currentPiece.getY();
            batch.setColor(currentPiece.getColor());
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[0].length; c++) {
                    if (shape[r][c] == 0) continue;
                    int boardRow = posY + (shape.length - 1 - r);
                    int boardCol = posX + c;
                    float x = curBoardX + boardCol * Constants.BLOCK_SIZE;
                    float y = curBoardY + boardRow * Constants.BLOCK_SIZE;
                    batch.draw(blockTexture, x, y,
                        Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
                }
            }
        }

        // 6. Particulas de explosao de linha
        for (Particle p : particles) {
            p.draw(batch, pixelTexture);
        }

        // 7. Alerta de Perigo quando altura passa da linha 15
        if (board.getHighestOccupiedRow() >= 15 && !gameOver) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(Gdx.graphics.getFrameId() * 0.2f);
            batch.setColor(1f, 0.1f, 0.1f, pulse * 0.25f);
            batch.draw(pixelTexture, curBoardX, curBoardY + 15 * Constants.BLOCK_SIZE,
                Constants.BOARD_COLS * Constants.BLOCK_SIZE, 5 * Constants.BLOCK_SIZE);
        }

        // 8. HUD CRT
        font.setColor(new Color(1.0f, 0.90f, 0.40f, 1f));

        // PONTOS (y = 555)
        font.draw(batch, String.valueOf(score), 356, 555, 123, Align.center, false);

        // RECORDE (y = 503)
        font.draw(batch, String.valueOf(highScore), 356, 503, 123, Align.center, false);

        // NIVEL (x = 350 a 414, center = 382, y = 451)
        font.draw(batch, String.valueOf(level), 350, 451, 64, Align.center, false);

        // LINHAS (x = 419 a 485, center = 452, y = 451)
        font.draw(batch, String.valueOf(lines), 419, 451, 66, Align.center, false);

        // 9. Previa da proxima peca (PROXIMO: centro x = 417.5, y = 365)
        if (nextPiece != null) {
            drawPreviewShape(nextPiece.getShape(), nextPiece.getColor(), 417.5f, 365f, 1.0f);
        }

        // 10. Peca guardada (RESERVA [C]: centro x = 417.5, y = 253)
        if (holdPiece != null) {
            float holdAlpha = canHold ? 1.0f : 0.45f;
            drawPreviewShape(holdPiece.shape, holdPiece.color, 417.5f, 253f, holdAlpha);
        }

        // 11. Textos Flutuantes Arcade
        for (FloatingText ft : floatingTexts) {
            ft.draw(batch, font);
        }

        // 12. Overlay de Pausa
        if (isPaused && !gameOver) {
            batch.setColor(Color.WHITE);
            batch.draw(pauseBox, 120, 260);
        }

        // 13. Overlay de Fim de Jogo
        if (gameOver) {
            batch.setColor(Color.WHITE);
            batch.draw(gameOverBox, 80, 215);

            font.setColor(Color.WHITE);
            font.draw(batch, "PONTOS: " + score, 80, 320, 340, Align.center, false);

            if (score >= highScore && score > 0) {
                font.setColor(new Color(0.98f, 0.85f, 0.20f, 1f));
                font.draw(batch, "★ NOVO RECORDE SOVIETICO! ★", 80, 292, 340, Align.center, false);
            }
        }

        batch.end();
    }

    private void drawPreviewShape(int[][] shape, Color color, float centerX, float centerY, float alpha) {
        int sCols = shape[0].length;
        int sRows = shape.length;
        float pieceW = sCols * Constants.BLOCK_SIZE;
        float pieceH = sRows * Constants.BLOCK_SIZE;
        float startX = centerX - pieceW / 2f;
        float startY = centerY - pieceH / 2f;

        batch.setColor(color.r, color.g, color.b, alpha);
        for (int r = 0; r < sRows; r++) {
            for (int c = 0; c < sCols; c++) {
                if (shape[r][c] == 0) continue;
                float bx = startX + c * Constants.BLOCK_SIZE;
                float by = startY + (sRows - 1 - r) * Constants.BLOCK_SIZE;
                batch.draw(blockTexture, bx, by, Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
            }
        }
    }

    // =====================================================================
    // INPUT (com DAS/ARR e SFX)
    // =====================================================================

    private void handleInput(float delta) {
        // Pausa
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !gameOver) {
            isPaused = !isPaused;
            if (game != null && game.getAudioManager() != null) {
                if (isPaused) game.getAudioManager().pauseGameMusic();
                else game.getAudioManager().resumeGameMusic();
            }
            return;
        }

        // Mudo
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (game != null && game.getAudioManager() != null) {
                game.getAudioManager().toggleMute();
            }
        }

        // ESC: Volta ao Menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            checkHighScore();
            if (game != null) {
                game.showMenu();
            }
            return;
        }

        if (isPaused) return;

        // Fim de Jogo: R recomeca
        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                initGame();
                if (game != null && game.getAudioManager() != null) {
                    game.getAudioManager().playGameMusic();
                }
            }
            return;
        }

        // Se estiver executando animacao de linha, bloqueia input de peca
        if (clearingTimer > 0) return;

        // Hold Piece (C ou SHIFT)
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
            if (canHold) {
                canHold = false;
                if (game != null && game.getAudioManager() != null) {
                    game.getAudioManager().playHold();
                }
                if (holdPiece == null) {
                    holdPiece = currentPiece.getType();
                    spawnNewPiece();
                } else {
                    TetrominoType temp = holdPiece;
                    holdPiece = currentPiece.getType();
                    currentPiece = new Tetromino(temp);
                }
                return;
            }
        }

        // Rotacao
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (currentPiece.rotate(board)) {
                if (game != null && game.getAudioManager() != null) {
                    game.getAudioManager().playRotate();
                }
            }
        }

        // Hard drop (queda instantanea com pontuacao e juice)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            int dropDistance = 0;
            while (currentPiece.moveDown(board)) {
                dropDistance++;
            }
            score += dropDistance * 2;
            checkHighScore();
            if (game != null && game.getAudioManager() != null) {
                game.getAudioManager().playHardDrop();
            }
            shake(0.14f, 4.0f);
            lockPiece();
            return;
        }

        handleHorizontal(delta);
        handleSoftDrop(delta);
    }

    private void handleHorizontal(float delta) {
        boolean leftPressed  = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        if (leftPressed && rightPressed) {
            leftHeld = false;  leftTimer = 0;
            rightHeld = false; rightTimer = 0;
            return;
        }

        // ESQUERDA
        if (leftPressed) {
            if (!leftHeld) {
                if (currentPiece.moveLeft(board)) {
                    if (game != null && game.getAudioManager() != null) game.getAudioManager().playMove();
                }
                leftHeld = true;
                leftTimer = 0f;
            } else {
                leftTimer += delta;
                if (leftTimer >= MOVE_DAS_DELAY && leftTimer - MOVE_DAS_DELAY >= MOVE_ARR_DELAY) {
                    if (currentPiece.moveLeft(board)) {
                        if (game != null && game.getAudioManager() != null) game.getAudioManager().playMove();
                    }
                    leftTimer = MOVE_DAS_DELAY;
                }
            }
        } else {
            leftHeld = false;
            leftTimer = 0f;
        }

        // DIREITA
        if (rightPressed) {
            if (!rightHeld) {
                if (currentPiece.moveRight(board)) {
                    if (game != null && game.getAudioManager() != null) game.getAudioManager().playMove();
                }
                rightHeld = true;
                rightTimer = 0f;
            } else {
                rightTimer += delta;
                if (rightTimer >= MOVE_DAS_DELAY && rightTimer - MOVE_DAS_DELAY >= MOVE_ARR_DELAY) {
                    if (currentPiece.moveRight(board)) {
                        if (game != null && game.getAudioManager() != null) game.getAudioManager().playMove();
                    }
                    rightTimer = MOVE_DAS_DELAY;
                }
            }
        } else {
            rightHeld = false;
            rightTimer = 0f;
        }
    }

    private void handleSoftDrop(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (!downHeld) {
                downHeld = true;
                downTimer = 0f;
                if (currentPiece.moveDown(board)) {
                    dropTimer = 0f;
                    score += 1;
                    checkHighScore();
                    if (game != null && game.getAudioManager() != null) game.getAudioManager().playDrop();
                }
            } else {
                downTimer += delta;
                if (downTimer >= SOFT_DROP_INTERVAL) {
                    downTimer -= SOFT_DROP_INTERVAL;
                    if (currentPiece.moveDown(board)) {
                        dropTimer = 0f;
                        score += 1;
                        checkHighScore();
                        if (game != null && game.getAudioManager() != null) game.getAudioManager().playDrop();
                    }
                }
            }
        } else {
            downHeld = false;
            downTimer = 0f;
        }
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    private void update(float delta) {
        // Pausa a gravidade enquanto executa a animacao de explosao/flash de linha
        if (clearingTimer > 0) {
            clearingTimer -= delta;
            if (clearingTimer <= 0) {
                finishLineClear();
            }
            return;
        }

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
        if (game != null && game.getAudioManager() != null) {
            game.getAudioManager().playDrop();
        }

        List<Integer> full = board.getFullRows();
        if (!full.isEmpty()) {
            clearingRows = full;
            clearingTimer = 0.18f; // Flash de 180ms antes de eliminar

            // Spawn de particulas nas linhas cheias
            for (int r : full) {
                for (int c = 0; c < board.getCols(); c++) {
                    Color cellColor = board.getCell(r, c);
                    if (cellColor == null) cellColor = Color.WHITE;
                    float px = boardOffsetX + c * Constants.BLOCK_SIZE + Constants.BLOCK_SIZE / 2f;
                    float py = boardOffsetY + r * Constants.BLOCK_SIZE + Constants.BLOCK_SIZE / 2f;
                    for (int k = 0; k < 4; k++) {
                        float vx = (random.nextFloat() * 2f - 1f) * 160f;
                        float vy = 60f + random.nextFloat() * 180f;
                        particles.add(new Particle(px, py, vx, vy, 4f + random.nextFloat() * 4f, cellColor, 0.45f));
                    }
                }
            }
        } else {
            combo = -1;
            spawnNewPiece();
        }
    }

    private void finishLineClear() {
        int cleared = clearingRows.size();
        board.removeRows(clearingRows);
        clearingRows.clear();

        lines += cleared;
        combo++;

        int basePts = 0;
        String lineLabel = "";
        Color labelColor = Color.WHITE;

        switch (cleared) {
            case 1:
                basePts = 100 * level;
                lineLabel = "SINGLE! +" + basePts;
                labelColor = Color.CYAN;
                if (game != null && game.getAudioManager() != null) game.getAudioManager().playClear();
                shake(0.14f, 3.5f);
                break;
            case 2:
                basePts = 300 * level;
                lineLabel = "DOUBLE! +" + basePts;
                labelColor = Color.GREEN;
                if (game != null && game.getAudioManager() != null) game.getAudioManager().playClear();
                shake(0.20f, 5.0f);
                break;
            case 3:
                basePts = 500 * level;
                lineLabel = "TRIPLE! +" + basePts;
                labelColor = Color.ORANGE;
                if (game != null && game.getAudioManager() != null) game.getAudioManager().playClear();
                shake(0.25f, 6.5f);
                break;
            case 4:
                basePts = 800 * level;
                lineLabel = "★ TETRIS! ★ +" + basePts;
                labelColor = Color.GOLD;
                if (game != null && game.getAudioManager() != null) game.getAudioManager().playTetris();
                shake(0.38f, 9.0f);
                break;
        }

        score += basePts;

        // Bonus de combo
        if (combo > 0) {
            int comboBonus = 50 * combo * level;
            score += comboBonus;
            floatingTexts.add(new FloatingText("COMBO x" + (combo + 1) + "! +" + comboBonus,
                boardOffsetX + 150, boardOffsetY + 350, Color.YELLOW, 1.2f));
        }

        floatingTexts.add(new FloatingText(lineLabel, boardOffsetX + 150, boardOffsetY + 300, labelColor, 1.4f));

        checkHighScore();

        level = (game != null ? game.getStartingLevel() : 1) + lines / 10;
        dropInterval = Constants.BASE_DROP_INTERVAL *
            (float) Math.pow(Constants.DROP_SPEED_FACTOR, level - 1);

        spawnNewPiece();
    }
}
