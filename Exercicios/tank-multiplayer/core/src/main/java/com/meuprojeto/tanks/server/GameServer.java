package com.meuprojeto.tanks.server;

import com.meuprojeto.tanks.Bullet;
import com.meuprojeto.tanks.GameState;
import com.meuprojeto.tanks.PlayerInput;
import com.meuprojeto.tanks.PowerUp;
import com.meuprojeto.tanks.Tank;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Random;

/**
 * Servidor autoritativo da Batalha de Tanques.
 *
 * Mesma ideia da base do Pong, generalizada para N jogadores:
 *   - Thread principal: fica no accept() esperando jogadores (até MAX_PLAYERS),
 *     a qualquer momento da partida (entra e sai livremente).
 *   - Uma thread por jogador (ClientHandler): lê o input desse jogador.
 *   - Thread GameLoop: a 60 Hz aplica os inputs, move tanques e projéteis,
 *     resolve colisões/dano e envia o snapshot para TODOS os clientes.
 *
 * Toda leitura/escrita do estado é feita dentro de synchronized(state), porque
 * três tipos de thread mexem nele (accept, handlers ao desconectar e game loop).
 */
public class GameServer {

    public static final int PORT = 12345;
    private static final float DT = 1f / 60f;

    private static final GameState state = new GameState();
    private static final ClientHandler[] clients = new ClientHandler[GameState.MAX_PLAYERS];

    private static final Random random = new Random();
    private static float powerUpTimer = GameState.POWERUP_SPAWN_MIN;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[Servidor] Batalha de Tanques na porta " + PORT
            + " (max. " + GameState.MAX_PLAYERS + " jogadores)");

        Thread gameLoop = new Thread(GameServer::loop, "GameLoop");
        gameLoop.setDaemon(true);
        gameLoop.start();

        // Aceita jogadores para sempre: quem chegar ocupa o primeiro slot livre.
        while (true) {
            Socket socket = serverSocket.accept();
            socket.setTcpNoDelay(true); // pacotes pequenos e frequentes: sem atraso de Nagle

            int slot = -1;
            synchronized (state) {
                for (int i = 0; i < clients.length; i++) {
                    if (clients[i] == null) { slot = i; break; }
                }
                if (slot >= 0) {
                    try {
                        clients[slot] = new ClientHandler(socket, slot);
                        addPlayer(slot);
                    } catch (IOException e) {
                        // conexão caiu durante o handshake: ignora este cliente
                        try { socket.close(); } catch (IOException ignored) {}
                        continue;
                    }
                }
            }

            if (slot < 0) {
                System.out.println("[Servidor] Recusado (servidor cheio): " + socket.getInetAddress());
                ClientHandler.rejectFull(socket);
                continue;
            }

            System.out.println("[Servidor] Jogador " + (slot + 1) + " conectado: " + socket.getInetAddress());
            clients[slot].start();
        }
    }

    /** Chamado pelo ClientHandler quando a conexão cai. */
    static void removePlayer(int id) {
        String name;
        synchronized (state) {
            name = state.tanks[id].name;
            state.tanks[id].active = false;
            clients[id] = null;
        }
        System.out.println("[Servidor] " + name + " (Jogador " + (id + 1) + ") desconectado.");
    }

    static void setPlayerName(int id, String name) {
        synchronized (state) {
            state.tanks[id].name = name;
        }
        System.out.println("[Servidor] Jogador " + (id + 1) + " definiu nome: " + name);
    }

    private static void addPlayer(int id) {
        Tank t = state.tanks[id];
        t.active = true;
        t.name = "Jogador " + (id + 1);
        t.kills = 0;
        t.deaths = 0;
        spawn(t);
    }

    // ---- LOOP ------------------------------------------------------------------

    private static void loop() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / 60.0;
        double delta = 0;

        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            boolean ticked = false;
            byte[] snapshot = null;
            ClientHandler[] targets;

            synchronized (state) {
                while (delta >= 1.0) {
                    updateGame(DT);
                    delta -= 1.0;
                    ticked = true;
                }
                if (ticked) snapshot = serialize();
                targets = clients.clone();
            }

            // Envia FORA do lock: um cliente lento não trava o jogo dos outros.
            if (snapshot != null) {
                for (ClientHandler c : targets) {
                    if (c != null) c.send(snapshot);
                }
            }

            try { Thread.sleep(1); } catch (InterruptedException e) { return; }
        }
    }

    /** Serializa o estado UMA vez por tick; os mesmos bytes vão para todos. */
    private static byte[] serialize() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
            state.write(new DataOutputStream(bytes));
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e); // não acontece com ByteArrayOutputStream
        }
    }

    // ---- REGRAS DO JOGO --------------------------------------------------------

    private static void updateGame(float dt) {
        for (Tank t : state.tanks) {
            if (!t.active) continue;

            if (!t.alive) {
                t.respawnTimer -= dt;
                if (t.respawnTimer <= 0f) spawn(t);
                continue;
            }

            PlayerInput in = clients[t.id] != null ? clients[t.id].input : null;
            if (in == null) continue;

            t.shield = Math.max(0f, t.shield - dt);
            t.reload = Math.max(0f, t.reload - dt);
            t.damageBoost = Math.max(0f, t.damageBoost - dt);
            t.turretAngle = in.aimAngle;

            // Movimento só nas 4 direções cardinais
            if (in.direction >= GameState.DIR_UP && in.direction <= GameState.DIR_LEFT) {
                t.facing = in.direction;
                float step = GameState.TANK_SPEED * dt;
                tryMove(t, GameState.DIR_X[in.direction] * step, GameState.DIR_Y[in.direction] * step);
            }

            collectPowerUps(t);

            // Tiro (respeitando o tempo de recarga)
            if (in.fire && t.reload <= 0f) {
                fire(t);
            }
        }

        updateBullets(dt);
        updatePowerUps(dt);
    }

    private static void tryMove(Tank t, float dx, float dy) {
        float nx = clamp(t.x + dx, GameState.TANK_HALF, GameState.WORLD_WIDTH - GameState.TANK_HALF);
        float ny = clamp(t.y + dy, GameState.TANK_HALF, GameState.WORLD_HEIGHT - GameState.TANK_HALF);

        // Tanques não atravessam uns aos outros (colisão AABB)
        for (Tank o : state.tanks) {
            if (o == t || !o.active || !o.alive) continue;
            if (Math.abs(nx - o.x) < GameState.TANK_SIZE && Math.abs(ny - o.y) < GameState.TANK_SIZE) {
                return; // bloqueado
            }
        }
        t.x = nx;
        t.y = ny;
    }

    private static void fire(Tank t) {
        Bullet b = new Bullet();
        b.owner = t.id;
        b.x = t.barrelTipX();
        b.y = t.barrelTipY();
        b.vx = (float) Math.cos(t.turretAngle) * GameState.BULLET_SPEED;
        b.vy = (float) Math.sin(t.turretAngle) * GameState.BULLET_SPEED;
        b.damage = t.damageBoost > 0f ? GameState.BOOSTED_DAMAGE : 1;
        state.bullets.add(b);

        t.reload = GameState.RELOAD_TIME;
        t.shield = 0f; // atirar cancela a proteção de nascimento
    }

    private static void updateBullets(float dt) {
        Iterator<Bullet> it = state.bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.x += b.vx * dt;
            b.y += b.vy * dt;
            b.traveled += GameState.BULLET_SPEED * dt;

            // Alcance máximo atingido ou saiu do campo
            if (b.traveled >= GameState.BULLET_RANGE
                || b.x < 0 || b.y < 0 || b.x > GameState.WORLD_WIDTH || b.y > GameState.WORLD_HEIGHT) {
                it.remove();
                continue;
            }

            Tank hit = findHit(b);
            if (hit != null) {
                it.remove();
                if (hit.shield <= 0f) damage(hit, b.owner, b.damage);
            }
        }
    }

    private static Tank findHit(Bullet b) {
        float reach = GameState.TANK_HALF + GameState.BULLET_RADIUS;
        for (Tank t : state.tanks) {
            if (!t.active || !t.alive || t.id == b.owner) continue;
            if (Math.abs(b.x - t.x) <= reach && Math.abs(b.y - t.y) <= reach) return t;
        }
        return null;
    }

    private static void damage(Tank victim, int shooterId, int amount) {
        // O escudo do power-up absorbe o tiro inteiro (mesmo um tiro de dano dobrado) e some.
        if (victim.armor > 0) {
            victim.armor = 0;
            return;
        }
        victim.hp -= amount;
        if (victim.hp > 0) return;
        victim.hp = 0;

        victim.alive = false;
        victim.deaths++;
        victim.respawnTimer = GameState.RESPAWN_TIME;

        Tank shooter = state.tanks[shooterId];
        if (shooter.active) shooter.kills++;

        System.out.println("[Servidor] " + (shooter.active ? shooter.name : ("Jogador " + (shooterId + 1)))
            + " destruiu " + victim.name);
    }

    /** Renasce no ponto de spawn mais longe dos tanques vivos. */
    private static void spawn(Tank t) {
        float[] best = GameState.SPAWN_POINTS[0];
        float bestDist = -1f;
        for (float[] p : GameState.SPAWN_POINTS) {
            float nearest = Float.MAX_VALUE;
            for (Tank o : state.tanks) {
                if (o == t || !o.active || !o.alive) continue;
                float dx = o.x - p[0], dy = o.y - p[1];
                nearest = Math.min(nearest, dx * dx + dy * dy);
            }
            if (nearest > bestDist) {
                bestDist = nearest;
                best = p;
            }
        }

        t.x = best[0];
        t.y = best[1];
        t.alive = true;
        t.hp = GameState.TANK_MAX_HP;
        t.reload = 0f;
        t.shield = GameState.SPAWN_SHIELD;
        t.respawnTimer = 0f;
        t.armor = 0;
        t.damageBoost = 0f;
        // Vira para o centro do campo
        t.facing = t.y < GameState.WORLD_HEIGHT / 2f ? GameState.DIR_UP : GameState.DIR_DOWN;
        t.turretAngle = (float) Math.atan2(GameState.WORLD_HEIGHT / 2f - t.y, GameState.WORLD_WIDTH / 2f - t.x);
    }

    // ---- POWER-UPS -------------------------------------------------------------

    /** Faz power-ups expirarem e, de tempos em tempos, sorteia um novo no campo. */
    private static void updatePowerUps(float dt) {
        Iterator<PowerUp> it = state.powerUps.iterator();
        while (it.hasNext()) {
            PowerUp p = it.next();
            p.ttl -= dt;
            if (p.ttl <= 0f) it.remove();
        }

        powerUpTimer -= dt;
        if (powerUpTimer > 0f) return;
        powerUpTimer = GameState.POWERUP_SPAWN_MIN
            + random.nextFloat() * (GameState.POWERUP_SPAWN_MAX - GameState.POWERUP_SPAWN_MIN);

        if (state.powerUps.size() >= GameState.POWERUP_MAX) return;
        PowerUp p = new PowerUp();
        if (!findFreeSpot(p)) return;
        p.type = rollType();
        p.ttl = GameState.POWERUP_LIFETIME;
        state.powerUps.add(p);
    }

    /** Sorteio ponderado: cada tipo tem um peso em POWERUP_WEIGHTS (o de dano é o menor). */
    private static int rollType() {
        int total = 0;
        for (int w : GameState.POWERUP_WEIGHTS) total += w;
        int r = random.nextInt(total);
        for (int type = 0; type < GameState.POWERUP_WEIGHTS.length; type++) {
            r -= GameState.POWERUP_WEIGHTS[type];
            if (r < 0) return type;
        }
        return PowerUp.REPAIR;
    }

    /** Procura uma posição aleatória longe de tanques vivos e de outros power-ups. */
    private static boolean findFreeSpot(PowerUp p) {
        float margin = 60f;
        for (int attempt = 0; attempt < 20; attempt++) {
            float x = margin + random.nextFloat() * (GameState.WORLD_WIDTH - 2 * margin);
            float y = margin + random.nextFloat() * (GameState.WORLD_HEIGHT - 2 * margin);
            if (isFarFromEverything(x, y)) {
                p.x = x;
                p.y = y;
                return true;
            }
        }
        return false;
    }

    private static boolean isFarFromEverything(float x, float y) {
        for (Tank t : state.tanks) {
            if (!t.active || !t.alive) continue;
            if (dist2(x, y, t.x, t.y) < 90f * 90f) return false;
        }
        for (PowerUp o : state.powerUps) {
            if (dist2(x, y, o.x, o.y) < 120f * 120f) return false;
        }
        return true;
    }

    /** Pega os power-ups que o tanque está tocando (se o efeito servir para ele agora). */
    private static void collectPowerUps(Tank t) {
        float reach = GameState.TANK_HALF + GameState.POWERUP_RADIUS;
        Iterator<PowerUp> it = state.powerUps.iterator();
        while (it.hasNext()) {
            PowerUp p = it.next();
            if (Math.abs(p.x - t.x) > reach || Math.abs(p.y - t.y) > reach) continue;
            if (applyPowerUp(t, p.type)) it.remove();
        }
    }

    /** Aplica o efeito. Retorna false se não fez nada (ex.: vida cheia) e o item fica no chão. */
    private static boolean applyPowerUp(Tank t, int type) {
        switch (type) {
            case PowerUp.REPAIR:
                if (t.hp >= GameState.TANK_MAX_HP) return false;
                t.hp = Math.min(GameState.TANK_MAX_HP, t.hp + GameState.REPAIR_AMOUNT);
                System.out.println("[Servidor] " + t.name + " pegou REPARO");
                return true;
            case PowerUp.ARMOR:
                if (t.armor > 0) return false;
                t.armor = 1;
                System.out.println("[Servidor] " + t.name + " pegou ESCUDO");
                return true;
            case PowerUp.DAMAGE:
                t.damageBoost = GameState.DAMAGE_BOOST_TIME; // pegar outro renova o tempo
                System.out.println("[Servidor] " + t.name + " pegou DANO x2");
                return true;
            default:
                return false;
        }
    }

    private static float dist2(float ax, float ay, float bx, float by) {
        float dx = ax - bx, dy = ay - by;
        return dx * dx + dy * dy;
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }
}
