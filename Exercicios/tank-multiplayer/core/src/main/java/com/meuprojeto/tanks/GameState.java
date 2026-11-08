package com.meuprojeto.tanks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Estado completo da partida + constantes de jogo.
 * NÃO depende de LibGDX: é usado tanto pelo servidor quanto pelo cliente.
 *
 * O servidor é o dono deste estado (autoritativo). A cada tick ele serializa
 * o estado com {@link #write(DataOutputStream)} e manda para todos os clientes,
 * que reconstroem com {@link #read(DataInputStream)}.
 */
public class GameState {

    // ---- MUNDO ---------------------------------------------------------------
    public static final float WORLD_WIDTH  = 1280f;
    public static final float WORLD_HEIGHT = 720f;
    public static final int   MAX_PLAYERS  = 10;

    // ---- TANQUE --------------------------------------------------------------
    public static final float TANK_SIZE      = 36f;           // lado do quadrado do tanque
    public static final float TANK_HALF      = TANK_SIZE / 2f;
    public static final float TANK_SPEED     = 100f;          // px/s ("não muito rápido")
    public static final float BARREL_LENGTH  = 26f;           // do centro até a ponta do cano
    public static final int   TANK_MAX_HP    = 3;
    public static final float RELOAD_TIME    = 1.2f;          // segundos entre tiros
    public static final float RESPAWN_TIME   = 3f;            // segundos morto
    public static final float SPAWN_SHIELD   = 2f;            // invulnerável ao nascer

    // ---- PROJÉTIL ------------------------------------------------------------
    public static final float BULLET_SPEED   = 420f;          // px/s
    public static final float BULLET_RANGE   = 380f;          // distância máxima (não cruza o mapa)
    public static final float BULLET_RADIUS  = 4f;

    // ---- DIREÇÕES CARDINAIS (facing do corpo do tanque) ----------------------
    public static final int DIR_NONE  = -1;
    public static final int DIR_UP    = 0;
    public static final int DIR_RIGHT = 1;
    public static final int DIR_DOWN  = 2;
    public static final int DIR_LEFT  = 3;

    public static final int[] DIR_X = { 0, 1, 0, -1 };
    public static final int[] DIR_Y = { 1, 0, -1, 0 };

    /** Pontos de nascimento espalhados pelo campo (um por jogador possível). */
    public static final float[][] SPAWN_POINTS = {
        {  100, 100 }, {  640, 100 }, { 1180, 100 },
        {  100, 620 }, {  640, 620 }, { 1180, 620 },
        {  100, 360 }, { 1180, 360 },
        {  380, 360 }, {  900, 360 },
    };

    // ---- DADOS ---------------------------------------------------------------
    /** Um slot por jogador possível; slot.active diz se tem alguém conectado. */
    public final Tank[] tanks = new Tank[MAX_PLAYERS];
    public final List<Bullet> bullets = new ArrayList<Bullet>();

    public GameState() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            tanks[i] = new Tank(i);
        }
    }

    // ---- PROTOCOLO -----------------------------------------------------------
    /*
     * Snapshot (Servidor -> Cliente), enviado a 60 Hz:
     *   byte  qtdTanquesAtivos
     *   repetido qtdTanquesAtivos vezes:
     *     byte id, boolean alive, float x, float y, byte facing, float turretAngle,
     *     byte hp, float reload, float respawnTimer, float shield,
     *     short kills, short deaths
     *   short qtdProjeteis
     *   repetido qtdProjeteis vezes:
     *     byte owner, float x, float y
     */
    public void write(DataOutputStream out) throws IOException {
        int active = 0;
        for (Tank t : tanks) if (t.active) active++;

        out.writeByte(active);
        for (Tank t : tanks) {
            if (!t.active) continue;
            out.writeByte(t.id);
            out.writeUTF(t.name != null && !t.name.isEmpty() ? t.name : ("Jogador " + (t.id + 1)));
            out.writeBoolean(t.alive);
            out.writeFloat(t.x);
            out.writeFloat(t.y);
            out.writeByte(t.facing);
            out.writeFloat(t.turretAngle);
            out.writeByte(t.hp);
            out.writeFloat(t.reload);
            out.writeFloat(t.respawnTimer);
            out.writeFloat(t.shield);
            out.writeShort(t.kills);
            out.writeShort(t.deaths);
        }

        out.writeShort(bullets.size());
        for (Bullet b : bullets) {
            out.writeByte(b.owner);
            out.writeFloat(b.x);
            out.writeFloat(b.y);
        }
    }

    public static GameState read(DataInputStream in) throws IOException {
        GameState s = new GameState();

        int active = in.readUnsignedByte();
        for (int i = 0; i < active; i++) {
            Tank t = s.tanks[in.readUnsignedByte()];
            t.name         = in.readUTF();
            t.active       = true;
            t.alive        = in.readBoolean();
            t.x            = in.readFloat();
            t.y            = in.readFloat();
            t.facing       = in.readByte();
            t.turretAngle  = in.readFloat();
            t.hp           = in.readByte();
            t.reload       = in.readFloat();
            t.respawnTimer = in.readFloat();
            t.shield       = in.readFloat();
            t.kills        = in.readShort();
            t.deaths       = in.readShort();
        }

        int bulletCount = in.readUnsignedShort();
        for (int i = 0; i < bulletCount; i++) {
            Bullet b = new Bullet();
            b.owner = in.readUnsignedByte();
            b.x     = in.readFloat();
            b.y     = in.readFloat();
            s.bullets.add(b);
        }
        return s;
    }
}
