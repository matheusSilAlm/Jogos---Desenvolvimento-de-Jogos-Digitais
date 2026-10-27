package io.github.airhockey.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import io.github.airhockey.model.Player;
import io.github.airhockey.model.Table;

public class ScoreBoard implements Disposable {
    public static final int TARGET_SCORE = 7;

    private final BitmapFont fontScore;
    private final BitmapFont fontSmall;
    private final BitmapFont fontBanner;
    private final GlyphLayout layout;

    public ScoreBoard() {
        fontScore = new BitmapFont();
        fontScore.getData().setScale(2.8f);

        fontSmall = new BitmapFont();
        fontSmall.getData().setScale(1.3f);

        fontBanner = new BitmapFont();
        fontBanner.getData().setScale(3.0f);

        layout = new GlyphLayout();
    }

    public void render(SpriteBatch batch, Player p1, Player p2, String modeLabel, boolean gameOver, boolean goldenPuck) {
        float topY = Table.WORLD_HEIGHT - 18f;

        // 1. Placar Jogador 1 (Rosa neon)
        fontScore.setColor(p1.getColor());
        fontScore.draw(batch, p1.getName() + "  " + p1.getScore(), 50f, topY);

        // 2. Placar Jogador 2 / IA (Ciano neon)
        fontScore.setColor(p2.getColor());
        String p2Text = p2.getScore() + "  " + p2.getName();
        layout.setText(fontScore, p2Text);
        fontScore.draw(batch, p2Text, Table.WORLD_WIDTH - 50f - layout.width, topY);

        // 3. Indicador central do modo e atalho ESC
        fontSmall.setColor(0.75f, 0.78f, 0.90f, 0.85f);
        String header = "[ " + modeLabel + " - META: " + TARGET_SCORE + " GOLS ]";
        layout.setText(fontSmall, header);
        fontSmall.draw(batch, header, (Table.WORLD_WIDTH - layout.width) / 2f, topY + 2f);

        fontSmall.setColor(0.50f, 0.55f, 0.65f, 0.70f);
        String escTip = "ESC: MENU";
        layout.setText(fontSmall, escTip);
        fontSmall.draw(batch, escTip, (Table.WORLD_WIDTH - layout.width) / 2f, topY - 26f);

        // Alerta de Disco Dourado ativo
        if (goldenPuck && !gameOver) {
            fontSmall.setColor(1.0f, 0.84f, 0.10f, 1f);
            String goldMsg = "! DISCO DOURADO ATIVO: VALE 2 GOLS !";
            layout.setText(fontSmall, goldMsg);
            fontSmall.draw(batch, goldMsg, (Table.WORLD_WIDTH - layout.width) / 2f, topY - 50f);
        }

        // 4. Banner de Fim de Partida
        if (gameOver) {
            String winner = p1.getScore() >= TARGET_SCORE ? p1.getName() : p2.getName();
            Color winColor = p1.getScore() >= TARGET_SCORE ? p1.getColor() : p2.getColor();

            fontBanner.setColor(winColor);
            String winMsg = ">>> VENCEDOR: " + winner + " <<<";
            layout.setText(fontBanner, winMsg);
            fontBanner.draw(batch, winMsg, (Table.WORLD_WIDTH - layout.width) / 2f, Table.WORLD_HEIGHT / 2f + 50f);

            fontSmall.setColor(1f, 1f, 1f, 0.9f);
            String prompt = "[ ESPACO ] JOGAR NOVAMENTE   |   [ ESC ] RETORNAR AO MENU";
            layout.setText(fontSmall, prompt);
            fontSmall.draw(batch, prompt, (Table.WORLD_WIDTH - layout.width) / 2f, Table.WORLD_HEIGHT / 2f - 20f);
        }
    }

    @Override
    public void dispose() {
        fontScore.dispose();
        fontSmall.dispose();
        fontBanner.dispose();
    }
}
