package io.github.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

import java.util.Random;

/**
 * Tela principal de jogo no estilo Sovietico do Tetris X68000 (1987-1988).
 * Toca '01 - Hawker\'s song.mp3' e conta com moldura arcade, HUD CRT e ghost piece.
 */
public class GameScreen extends ScreenAdapter {

    private final TetrisGame game;

    // Recursos graficos
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture blockTexture;
    private Texture fieldTexture;
    private Texture gameBg;
    private Texture gameOverBox;
    private Texture pauseBox;

    // Estado do jogo
    private Board board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;

    private float dropTimer;
    private float dropInterval = Constants.BASE_DROP_INTERVAL;
    private int score;
    private int lines;
    private int level = 1;
    private int highScore = 0;
    private boolean gameOver;
    private boolean isPaused;

    private final Random random = new Random();

    // Posicionamento do tabuleiro
    private final int boardOffsetX = Constants.BOARD_OFFSET_X;
    private final int boardOffsetY = Constants.BOARD_OFFSET_Y;

    // DAS / ARR (Delayed Auto Shift)
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

    // =====================================================================
    // CICLO DE VIDA
    // =====================================================================

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

        if (game != null && game.getPreferences() != null) {
            highScore = game.getPreferences().getInteger("highScore", 0);
        }

        initGame();

        if (game != null && game.getAudioManager() != null) {
            game.getAudioManager().playGameMusic();
        }
    }

    private void initGame() {
        board = new Board();
        nextPiece = new Tetromino(getRandomType());
        spawnNewPiece();
        gameOver = false;
        isPaused = false;
        score = 0;
        lines = 0;
        level = 1;
        dropInterval = Constants.BASE_DROP_INTERVAL;
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
    }

    // =====================================================================
    // SPAWN DE PECAS
    // =====================================================================

    private void spawnNewPiece() {
        currentPiece = nextPiece;
        nextPiece = new Tetromino(getRandomType());

        if (!board.isValidPosition(currentPiece.getShape(),
            currentPiece.getX(), currentPiece.getY())) {
            triggerGameOver();
        }
    }

    private void triggerGameOver() {
        gameOver = true;
        if (score > highScore) {
            highScore = score;
            if (game != null && game.getPreferences() != null) {
                game.getPreferences().putInteger("highScore", highScore);
                game.getPreferences().flush();
            }
        }
        if (game != null && game.getAudioManager() != null) {
            game.getAudioManager().stopAll();
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

        if (!gameOver && !isPaused) {
            update(delta);
        }

        Gdx.gl.glClearColor(0.05f, 0.06f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // 1. Moldura Arcade Sovietica completa (Console X68000)
        batch.setColor(Color.WHITE);
        batch.draw(gameBg, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // 2. Fundo da Matriz de jogo com marca d'agua sovietica
        batch.draw(fieldTexture, boardOffsetX, boardOffsetY,
            Constants.BOARD_COLS * Constants.BLOCK_SIZE,
            Constants.BOARD_ROWS * Constants.BLOCK_SIZE);

        // 3. Blocos fixos no tabuleiro (com relevo chanfrado 3D)
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

        // 4. Ghost Piece (projecao translucida no fundo)
        if (!gameOver && !isPaused && currentPiece != null) {
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
                        float gx = boardOffsetX + boardCol * Constants.BLOCK_SIZE;
                        float gy = boardOffsetY + boardRow * Constants.BLOCK_SIZE;
                        batch.draw(blockTexture, gx, gy,
                            Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
                    }
                }
            }
        }

        // 5. Peca ativa em queda
        if (!gameOver && currentPiece != null) {
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

        // 6. HUD / Mostradores CRT no painel direito
        Color hudColor = new Color(1.0f, 0.90f, 0.40f, 1f);
        font.setColor(hudColor);

        // SCORE: centro y = 544
        font.draw(batch, String.valueOf(score), 356, 544, 123, Align.center, false);

        // TOP RECORD: centro y = 479
        font.draw(batch, String.valueOf(highScore), 356, 479, 123, Align.center, false);

        // LEVEL: centro y = 414
        font.draw(batch, String.valueOf(level), 356, 414, 123, Align.center, false);

        // LINES: centro y = 349
        font.draw(batch, String.valueOf(lines), 356, 349, 123, Align.center, false);

        // 7. Previa da proxima peca (centralizada na caixa NEXT)
        if (nextPiece != null) {
            int[][] nextShape = nextPiece.getShape();
            int sCols = nextShape[0].length;
            int sRows = nextShape.length;
            float pieceW = sCols * Constants.BLOCK_SIZE;
            float pieceH = sRows * Constants.BLOCK_SIZE;
            float prevStartX = 417.5f - pieceW / 2f;
            float prevStartY = 238.5f - pieceH / 2f;

            batch.setColor(nextPiece.getColor());
            for (int r = 0; r < sRows; r++) {
                for (int c = 0; c < sCols; c++) {
                    if (nextShape[r][c] == 0) continue;
                    float bx = prevStartX + c * Constants.BLOCK_SIZE;
                    float by = prevStartY + (sRows - 1 - r) * Constants.BLOCK_SIZE;
                    batch.draw(blockTexture, bx, by,
                        Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
                }
            }
        }

        // 8. Overlay de Pausa
        if (isPaused && !gameOver) {
            batch.setColor(Color.WHITE);
            batch.draw(pauseBox, 120, 260);
        }

        // 9. Overlay de Fim de Jogo
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

    // =====================================================================
    // INPUT (com DAS/ARR)
    // =====================================================================

    private void handleInput(float delta) {
        // Pausa / Retorno ao Menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !gameOver) {
            isPaused = !isPaused;
            if (game != null && game.getAudioManager() != null) {
                if (isPaused) {
                    game.getAudioManager().pauseGameMusic();
                } else {
                    game.getAudioManager().resumeGameMusic();
                }
            }
            return;
        }

        // Mudo / desmudo
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (game != null && game.getAudioManager() != null) {
                game.getAudioManager().toggleMute();
            }
        }

        // ESC: Volta ao Menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (game != null) {
                game.showMenu();
            }
            return;
        }

        // Se pausado, congela acoes de peca
        if (isPaused) return;

        // Se Fim de Jogo, tecla R recomeca
        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                initGame();
                if (game != null && game.getAudioManager() != null) {
                    game.getAudioManager().playGameMusic();
                }
            }
            return;
        }

        // Rotacao
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            currentPiece.rotate(board);
        }

        // Hard drop (queda instantanea)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            while (currentPiece.moveDown(board)) { /* desce ao fundo */ }
            lockPiece();
            return;
        }

        // Movimento horizontal com DAS/ARR
        handleHorizontal(delta);

        // Soft drop com repeticao controlada
        handleSoftDrop(delta);
    }

    /** Trata ← e → com DAS (delay inicial) e ARR (repeticao continua). */
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
                currentPiece.moveLeft(board);
                leftHeld = true;
                leftTimer = 0f;
            } else {
                leftTimer += delta;
                if (leftTimer >= MOVE_DAS_DELAY
                    && leftTimer - MOVE_DAS_DELAY >= MOVE_ARR_DELAY) {
                    currentPiece.moveLeft(board);
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

    /** Soft drop (seta ↓) com repeticao controlada em SOFT_DROP_INTERVAL. */
    private void handleSoftDrop(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (!downHeld) {
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
    // UPDATE (gravidade automatica)
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
                    score += 100 * level;
                    break;
                case 2:
                    score += 300 * level;
                    break;
                case 3:
                    score += 500 * level;
                    break;
                case 4:
                    score += 800 * level; // Tetris!
                    break;
            }
            if (score > highScore) {
                highScore = score;
            }
            level = 1 + lines / 10;
            dropInterval = Constants.BASE_DROP_INTERVAL *
                (float) Math.pow(Constants.DROP_SPEED_FACTOR, level - 1);
        }

        spawnNewPiece();
    }
}
