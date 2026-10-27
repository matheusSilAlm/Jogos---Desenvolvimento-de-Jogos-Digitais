package io.github.airhockey;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.airhockey.fx.Effects;
import io.github.airhockey.input.AIController;
import io.github.airhockey.input.KeyboardController;
import io.github.airhockey.input.PlayerController;
import io.github.airhockey.model.Paddle;
import io.github.airhockey.model.Player;
import io.github.airhockey.model.Puck;
import io.github.airhockey.model.Table;
import io.github.airhockey.physics.CollisionHandler;
import io.github.airhockey.powerup.PowerUpManager;
import io.github.airhockey.ui.ArcadeMenu;
import io.github.airhockey.ui.ScoreBoard;

public class AirHockeyGame extends ApplicationAdapter {

    public enum GameState {
        MENU,
        PLAYING,
        GAME_OVER
    }

    public enum GameMode {
        SINGLE_PLAYER,
        TWO_PLAYERS
    }

    private FitViewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private ScoreBoard scoreBoard;
    private ArcadeMenu arcadeMenu;

    private Table table;
    private Puck puck;
    private Player player1;
    private Player player2;
    private CollisionHandler collisionHandler;
    private Effects effects;
    private PowerUpManager powerUpManager;

    private GameState state = GameState.MENU;
    private GameMode currentMode = GameMode.SINGLE_PLAYER;

    @Override
    public void create() {
        table = new Table();
        viewport = new FitViewport(Table.WORLD_WIDTH, Table.WORLD_HEIGHT);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        scoreBoard = new ScoreBoard();
        arcadeMenu = new ArcadeMenu();
        collisionHandler = new CollisionHandler();
        effects = new Effects();
        powerUpManager = new PowerUpManager(effects);

        puck = new Puck(Table.WORLD_WIDTH / 2f, Table.WORLD_HEIGHT / 2f);

        // Configuração inicial padrão para visualização de fundo
        setupPlayers(GameMode.SINGLE_PLAYER);
        resetPositions();
    }

    private void setupPlayers(GameMode mode) {
        currentMode = mode;

        Color p1Color = new Color(1.0f, 0.10f, 0.55f, 1f); // Neon Magenta
        Paddle paddle1 = new Paddle(Table.FIELD_LEFT + Paddle.RADIUS + 80f, Table.WORLD_HEIGHT / 2f, true, p1Color);
        KeyboardController p1Input = new KeyboardController(
            Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.SPACE
        );
        player1 = new Player("J1", paddle1, p1Input, p1Color);

        Color p2Color = new Color(0.0f, 0.95f, 1.0f, 1f); // Neon Ciano
        Paddle paddle2 = new Paddle(Table.FIELD_RIGHT - Paddle.RADIUS - 80f, Table.WORLD_HEIGHT / 2f, false, p2Color);

        PlayerController p2Input;
        String p2Name;
        if (mode == GameMode.SINGLE_PLAYER) {
            p2Input = new AIController(puck, paddle2, table, powerUpManager);
            p2Name = "IA";
        } else {
            p2Input = new KeyboardController(
                Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT
            );
            p2Name = "J2";
        }

        player2 = new Player(p2Name, paddle2, p2Input, p2Color);
    }

    private void startMatch(GameMode mode) {
        setupPlayers(mode);
        player1.resetScore();
        player2.resetScore();
        effects.clear();
        powerUpManager.clear();
        resetPositions();
        state = GameState.PLAYING;
    }

    private void resetPositions() {
        puck.reset(Table.WORLD_WIDTH / 2f, Table.WORLD_HEIGHT / 2f);
        player1.getPaddle().reset(Table.FIELD_LEFT + Paddle.RADIUS + 80f, Table.WORLD_HEIGHT / 2f);
        player2.getPaddle().reset(Table.FIELD_RIGHT - Paddle.RADIUS - 80f, Table.WORLD_HEIGHT / 2f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);

        update(dt);
        draw();
    }

    private void update(float dt) {
        effects.update(dt);

        if (state == GameState.MENU) {
            ArcadeMenu.MenuChoice choice = arcadeMenu.update(dt);
            if (choice == ArcadeMenu.MenuChoice.SINGLE_PLAYER) {
                startMatch(GameMode.SINGLE_PLAYER);
            } else if (choice == ArcadeMenu.MenuChoice.TWO_PLAYERS) {
                startMatch(GameMode.TWO_PLAYERS);
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            effects.clear();
            powerUpManager.clear();
            state = GameState.MENU;
            return;
        }

        if (state == GameState.GAME_OVER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                startMatch(currentMode);
            }
            return;
        }

        // Estado PLAYING
        powerUpManager.update(dt, player1.getPaddle(), player2.getPaddle(), puck);

        player1.update(dt, table);
        player2.update(dt, table);

        puck.update(dt);
        if (puck.isGolden()) {
            effects.sparkle(puck.getPosition().x, puck.getPosition().y);
        }

        collisionHandler.checkAndResolve(puck, player1.getPaddle());
        collisionHandler.checkAndResolve(puck, player2.getPaddle());

        Table.GoalSide goal = table.handlePuckBounds(puck);
        if (goal == Table.GoalSide.LEFT) {
            int pts = puck.isGolden() ? 2 : 1;
            player2.addScore(pts);
            effects.goalScored(true, puck.getPosition().y, player2.getColor());
            resetPositions();
            if (player2.getScore() >= ScoreBoard.TARGET_SCORE) {
                state = GameState.GAME_OVER;
                effects.startVictory(false, player2.getColor());
            }
        } else if (goal == Table.GoalSide.RIGHT) {
            int pts = puck.isGolden() ? 2 : 1;
            player1.addScore(pts);
            effects.goalScored(false, puck.getPosition().y, player1.getColor());
            resetPositions();
            if (player1.getScore() >= ScoreBoard.TARGET_SCORE) {
                state = GameState.GAME_OVER;
                effects.startVictory(true, player1.getColor());
            }
        }
    }

    private void draw() {
        viewport.apply();
        ScreenUtils.clear(0.03f, 0.04f, 0.08f, 1f); // Fundo arcade preto azulado

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Tremor de tela
        viewport.getCamera().position.set(Table.WORLD_WIDTH / 2f + effects.getShakeX(),
            Table.WORLD_HEIGHT / 2f + effects.getShakeY(), 0f);
        viewport.getCamera().update();

        // 1. Renderiza a mesa e entidades
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeType.Filled);
        table.render(shapes);
        powerUpManager.render(shapes);
        player1.getPaddle().render(shapes);
        player2.getPaddle().render(shapes);
        puck.render(shapes);
        effects.render(shapes);
        shapes.end();

        // 2. Renderiza UI de acordo com o estado
        if (state == GameState.MENU) {
            arcadeMenu.renderShapes(shapes);
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            arcadeMenu.renderText(batch);
            batch.end();
        } else {
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            String modeStr = currentMode == GameMode.SINGLE_PLAYER ? "1P VS IA" : "1P VS 2P";
            scoreBoard.render(batch, player1, player2, modeStr, state == GameState.GAME_OVER, puck.isGolden());
            batch.end();
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        scoreBoard.dispose();
        arcadeMenu.dispose();
    }
}
