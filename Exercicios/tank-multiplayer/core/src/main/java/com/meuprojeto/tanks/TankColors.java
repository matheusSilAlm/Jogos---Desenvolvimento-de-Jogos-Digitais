package com.meuprojeto.tanks;

import com.badlogic.gdx.graphics.Color;

/** Paleta dos 10 tanques: a cor é definida pelo id do jogador (0..9). */
public final class TankColors {

    public static final Color[] BODY = {
        new Color(0.90f, 0.22f, 0.22f, 1f), // vermelho
        new Color(0.25f, 0.50f, 0.95f, 1f), // azul
        new Color(0.25f, 0.80f, 0.30f, 1f), // verde
        new Color(0.95f, 0.85f, 0.20f, 1f), // amarelo
        new Color(1.00f, 0.55f, 0.10f, 1f), // laranja
        new Color(0.65f, 0.35f, 0.92f, 1f), // roxo
        new Color(0.20f, 0.85f, 0.90f, 1f), // ciano
        new Color(1.00f, 0.45f, 0.75f, 1f), // rosa
        new Color(0.92f, 0.92f, 0.92f, 1f), // branco
        new Color(0.62f, 0.42f, 0.22f, 1f), // marrom
    };

    public static final String[] NAMES = {
        "Vermelho", "Azul", "Verde", "Amarelo", "Laranja",
        "Roxo", "Ciano", "Rosa", "Branco", "Marrom",
    };

    /** Versão escura (esteiras, torre). */
    public static final Color[] DARK = new Color[BODY.length];

    static {
        for (int i = 0; i < BODY.length; i++) {
            DARK[i] = new Color(BODY[i]).mul(0.55f, 0.55f, 0.55f, 1f);
        }
    }

    private TankColors() {}
}
