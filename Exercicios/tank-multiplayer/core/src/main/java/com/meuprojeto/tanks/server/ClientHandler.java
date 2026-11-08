package com.meuprojeto.tanks.server;

import com.meuprojeto.tanks.PlayerInput;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Atende UM jogador (uma thread por jogador conectado).
 *   - run(): lê continuamente o PlayerInput do cliente e guarda o mais recente.
 *   - send(): chamado pelo GameLoop para enviar o snapshot já serializado.
 *
 * Handshake: ao conectar, o servidor envia um int com o id do jogador (0..9),
 * ou -1 se o servidor estiver cheio.
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private final int playerId;

    private final DataInputStream in;
    private final DataOutputStream out;

    /** Último input recebido. volatile: escrito aqui, lido pelo GameLoop. */
    volatile PlayerInput input = new PlayerInput();

    public ClientHandler(Socket socket, int playerId) throws IOException {
        super("Client-" + (playerId + 1));
        setDaemon(true);
        this.socket = socket;
        this.playerId = playerId;
        this.in  = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        out.writeInt(playerId);
        out.flush();
    }

    @Override
    public void run() {
        try {
            String name = in.readUTF();
            if (name == null || name.trim().isEmpty()) {
                name = "Jogador " + (playerId + 1);
            } else {
                name = name.trim();
                if (name.length() > 16) name = name.substring(0, 16);
            }
            GameServer.setPlayerName(playerId, name);

            while (true) {
                input = PlayerInput.read(in);
            }
        } catch (IOException e) {
            // cliente fechou o jogo ou a rede caiu
        } finally {
            close();
            GameServer.removePlayer(playerId);
        }
    }

    /** Envia um snapshot. synchronized: só uma thread escreve no socket por vez. */
    public synchronized void send(byte[] snapshot) {
        try {
            out.write(snapshot);
            out.flush();
        } catch (IOException e) {
            close(); // derruba o socket; o run() percebe e remove o jogador
        }
    }

    private void close() {
        try { socket.close(); } catch (IOException ignored) {}
    }

    /** Avisa o cliente que não há vaga (id = -1) e fecha. */
    static void rejectFull(Socket socket) {
        try {
            DataOutputStream o = new DataOutputStream(socket.getOutputStream());
            o.writeInt(-1);
            o.flush();
        } catch (IOException ignored) {
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
