package com.meuprojeto.tanks;

/** Projétil. Só x, y e owner viajam pela rede; o resto é usado pelo servidor. */
public class Bullet {
    public int owner;          // id do tanque que atirou
    public float x, y;
    public float vx, vy;       // velocidade (servidor)
    public float traveled;     // distância já percorrida (servidor)
}
