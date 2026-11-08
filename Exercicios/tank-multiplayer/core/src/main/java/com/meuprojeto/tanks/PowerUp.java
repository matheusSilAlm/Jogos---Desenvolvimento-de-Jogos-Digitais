package com.meuprojeto.tanks;

/**
 * Power-up parado no campo, esperando um tanque passar por cima. Sem LibGDX.
 *
 * Tipos:
 *   REPAIR - chave inglesa: recupera {@link GameState#REPAIR_AMOUNT} de vida.
 *   ARMOR  - escudo: +1 quadradinho extra que absorve o próximo tiro inteiro.
 *   DAMAGE - dano dobrado por {@link GameState#DAMAGE_BOOST_TIME} segundos (raro).
 */
public class PowerUp {
    public static final int REPAIR = 0;
    public static final int ARMOR  = 1;
    public static final int DAMAGE = 2;

    public int   type;
    public float x, y;
    public float ttl;   // segundos até sumir do campo (o cliente pisca no final)
}
