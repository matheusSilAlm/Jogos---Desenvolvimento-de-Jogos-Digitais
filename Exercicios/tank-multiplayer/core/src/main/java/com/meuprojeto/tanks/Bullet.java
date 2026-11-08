package com.meuprojeto.tanks;

/** Projétil. Só x, y e owner viajam pela rede; o resto é usado pelo servidor. */
public class Bullet {
    public int owner;          // id do tanque que atirou
    public float x, y;
    public int damage = 1;     // 2 quando o atirador está com o power-up de dano
    public float vx, vy;       // velocidade (servidor)
    public float traveled;     // distância já percorrida (servidor)
}
