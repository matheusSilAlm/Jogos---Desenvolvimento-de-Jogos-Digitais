package io.github.airhockey.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Disposable;
import io.github.airhockey.model.Table;

public class ArcadeMenu implements Disposable {

    public enum MenuChoice {
        NONE,
        SINGLE_PLAYER,
        TWO_PLAYERS
    }

    private int selectedIndex = 0; // 0 = 1P vs IA, 1 = 2P 1v1
    private float blinkTimer = 0f;

    private final BitmapFont fontTitle;
    private final BitmapFont fontSubtitle;
    private final BitmapFont fontOption;
    private final BitmapFont fontFooter;
    private final GlyphLayout layout;

    public ArcadeMenu() {
        fontTitle = new BitmapFont();
        fontTitle.getData().setScale(3.8f);

        fontSubtitle = new BitmapFont();
        fontSubtitle.getData().setScale(1.8f);

        fontOption = new BitmapFont();
        fontOption.getData().setScale(2.2f);

        fontFooter = new BitmapFont();
        fontFooter.getData().setScale(1.3f);

        layout = new GlyphLayout();
    }

    public MenuChoice update(float dt) {
        blinkTimer += dt;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = 1;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            return MenuChoice.SINGLE_PLAYER;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            return MenuChoice.TWO_PLAYERS;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return selectedIndex == 0 ? MenuChoice.SINGLE_PLAYER : MenuChoice.TWO_PLAYERS;
        }

        return MenuChoice.NONE;
    }

    public void renderShapes(ShapeRenderer shapes) {
        // Painel central de fliperama
        float boxW = 840f;
        float boxH = 500f;
        float boxX = (Table.WORLD_WIDTH - boxW) / 2f;
        float boxY = (Table.WORLD_HEIGHT - boxH) / 2f;

        // Fundo escuro translúcido do menu
        shapes.begin(ShapeType.Filled);
        shapes.setColor(0.04f, 0.05f, 0.10f, 0.90f);
        shapes.rect(boxX, boxY, boxW, boxH);

        // Moldura neon roxa/ciano
        shapes.setColor(0.6f, 0.2f, 0.9f, 0.35f);
        shapes.rect(boxX - 6f, boxY - 6f, boxW + 12f, boxH + 12f);
        shapes.setColor(0.04f, 0.05f, 0.10f, 1f);
        shapes.rect(boxX, boxY, boxW, boxH);

        // Barra decorativa superior
        shapes.setColor(1.0f, 0.1f, 0.55f, 0.8f);
        shapes.rect(boxX, boxY + boxH - 6f, boxW / 2f, 6f);
        shapes.setColor(0f, 0.95f, 1.0f, 0.8f);
        shapes.rect(boxX + boxW / 2f, boxY + boxH - 6f, boxW / 2f, 6f);

        // Destaque da opção selecionada
        float optY = selectedIndex == 0 ? boxY + 245f : boxY + 185f;
        shapes.setColor(selectedIndex == 0 ? 1f : 0f, selectedIndex == 0 ? 0.1f : 0.95f, selectedIndex == 0 ? 0.55f : 1f, 0.20f);
        shapes.rect(boxX + 100f, optY - 8f, boxW - 200f, 48f);

        shapes.end();
    }

    public void renderText(SpriteBatch batch) {
        float centerX = Table.WORLD_WIDTH / 2f;
        float boxY = (Table.WORLD_HEIGHT - 500f) / 2f;

        // 1. Título Arcade Neon
        fontTitle.setColor(0.95f, 0.95f, 1f, 1f);
        String title = "AIR HOCKEY";
        layout.setText(fontTitle, title);
        fontTitle.draw(batch, title, centerX - layout.width / 2f, boxY + 450f);

        fontSubtitle.setColor(1.0f, 0.85f, 0.1f, 1f);
        String sub = ">> FLIPERAMA EDITION <<";
        layout.setText(fontSubtitle, sub);
        fontSubtitle.draw(batch, sub, centerX - layout.width / 2f, boxY + 390f);

        // 2. Opções do Menu
        String opt1 = (selectedIndex == 0 ? ">  " : "   ") + "[ 1 ]  1 JOGADOR  (VS IA)";
        fontOption.setColor(selectedIndex == 0 ? Color.valueOf("ff198c") : Color.valueOf("8a95aa"));
        layout.setText(fontOption, opt1);
        fontOption.draw(batch, opt1, centerX - layout.width / 2f, boxY + 275f);

        String opt2 = (selectedIndex == 1 ? ">  " : "   ") + "[ 2 ]  2 JOGADORES (VERSUS 1V1)";
        fontOption.setColor(selectedIndex == 1 ? Color.valueOf("00f0ff") : Color.valueOf("8a95aa"));
        layout.setText(fontOption, opt2);
        fontOption.draw(batch, opt2, centerX - layout.width / 2f, boxY + 215f);

        // 3. Texto intermitente (pisca estilo fliperama)
        if ((int)(blinkTimer * 3f) % 2 == 0) {
            fontFooter.setColor(0.95f, 0.95f, 1f, 1f);
            String pressPrompt = "PRESSIONE [1], [2] OU [ENTER] PARA JOGAR";
            layout.setText(fontFooter, pressPrompt);
            fontFooter.draw(batch, pressPrompt, centerX - layout.width / 2f, boxY + 130f);
        }

        // 4. Guia de Controles
        fontFooter.setColor(0.65f, 0.70f, 0.80f, 0.85f);
        String tip1 = "JOGADOR 1: [SETAS] Mover  |  [ESPACO] Super Bate (Boost)";
        layout.setText(fontFooter, tip1);
        fontFooter.draw(batch, tip1, centerX - layout.width / 2f, boxY + 75f);

        String tip2 = "JOGADOR 2: [W A S D] Mover  |  [SHIFT] Super Bate (Boost)";
        layout.setText(fontFooter, tip2);
        fontFooter.draw(batch, tip2, centerX - layout.width / 2f, boxY + 45f);
    }

    @Override
    public void dispose() {
        fontTitle.dispose();
        fontSubtitle.dispose();
        fontOption.dispose();
        fontFooter.dispose();
    }
}
