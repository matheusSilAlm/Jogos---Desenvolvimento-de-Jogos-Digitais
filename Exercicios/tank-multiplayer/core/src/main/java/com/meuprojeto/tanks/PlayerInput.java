package com.meuprojeto.tanks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Comando que o cliente manda ao servidor a cada frame.
 * O cliente NÃO manda sua posição: só a intenção (direção, mira, atirar).
 * Assim o servidor decide colisões, recarga e dano — ninguém consegue "trapacear"
 * mandando uma posição falsa.
 *
 * Protocolo (Cliente -> Servidor):
 *   byte    direction   (-1 parado, 0 cima, 1 direita, 2 baixo, 3 esquerda)
 *   float   aimAngle    (radianos)
 *   boolean fire        (botão de tiro pressionado)
 */
public class PlayerInput {
    public int     direction = GameState.DIR_NONE;
    public float   aimAngle;
    public boolean fire;

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(direction);
        out.writeFloat(aimAngle);
        out.writeBoolean(fire);
    }

    public static PlayerInput read(DataInputStream in) throws IOException {
        PlayerInput p = new PlayerInput();
        p.direction = in.readByte();
        p.aimAngle  = in.readFloat();
        p.fire      = in.readBoolean();
        return p;
    }
}
