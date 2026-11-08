# Batalha de Tanques Multiplayer (LibGDX + Sockets TCP)

Até **10 jogadores** simultâneos, cada um com seu tanque de uma cor, num campo aberto.
Construído sobre a base do *Pong Multiplayer* da disciplina: mesmo modelo de
**servidor autoritativo** com **sockets TCP puros** (`DataInputStream`/`DataOutputStream`),
generalizado de 2 para N jogadores.

## Regras

- O tanque anda só nas **4 direções cardinais** (WASD ou setas), devagar (100 px/s).
- A **torre mira com o mouse**; uma **linha tracejada mostra a trajetória** do tiro até o
  **alcance máximo** (380 px). Ela fica na cor do tanque quando o tiro está pronto e cinza
  enquanto recarrega.
- Tiro com **recarga de 1,2 s** (clique esquerdo ou Espaço).
- Cada tanque tem **3 de vida**. Ao ser destruído, renasce em 3 s no ponto mais longe dos
  inimigos, com 2 s de escudo (o escudo acaba se ele atirar).
- Placar com abates/mortes no canto superior direito.

Todas as constantes (velocidades, alcance, recarga, vida, tamanho do campo) ficam no topo de
`GameState.java`.

## Como rodar

**Requisitos:** JDK 17+, todos na mesma rede, porta TCP **12345** liberada no firewall da
máquina do servidor.

### 1. Servidor (uma máquina)
- IntelliJ: abra `core/src/main/java/com/meuprojeto/tanks/server/GameServer.java` e clique
  em **Run** no `main`; **ou**
- Terminal: `./gradlew :core:runServer`

Descubra o IP da máquina com `ipconfig` (Windows), ex.: `192.168.0.15`.

### 2. Clientes (até 10 máquinas)
- IntelliJ: rode `Lwjgl3Launcher.main`; **ou** terminal: `./gradlew :lwjgl3:run`
- Na tela inicial digite o IP do servidor e aperte **ENTER** (`localhost` para testar na
  mesma máquina; dá para abrir vários clientes no mesmo PC).
- O 11º jogador recebe "Servidor cheio". Quem sai libera a vaga e outro pode entrar no meio
  da partida.

## Arquitetura

```
core/src/main/java/com/meuprojeto/tanks/
├── GameState.java      estado + constantes + (de)serialização do snapshot   (sem LibGDX)
├── Tank.java           dados de um tanque                                    (sem LibGDX)
├── Bullet.java         dados de um projétil                                  (sem LibGDX)
├── PlayerInput.java    comando do cliente (direção, mira, tiro)              (sem LibGDX)
├── TankClient.java     Game do LibGDX
├── ConnectScreen.java  tela para digitar o IP
├── GameScreen.java     rede do cliente + input + desenho
├── TankColors.java     as 10 cores
└── server/
    ├── GameServer.java     accept() + game loop a 60 Hz + regras
    └── ClientHandler.java  uma thread por jogador
```

### Threads do servidor
| Thread | O que faz |
|---|---|
| `main` | `accept()` para sempre; põe cada jogador no primeiro slot livre (ou responde `-1`). |
| `Client-N` (uma por jogador) | lê o `PlayerInput` mais recente desse jogador. |
| `GameLoop` | 60x por segundo: aplica inputs, move tanques/projéteis, colisões, dano, respawn; serializa o estado **uma vez** e envia os mesmos bytes a todos. |

O estado é protegido com `synchronized (state)`; o envio para os sockets acontece **fora**
do lock, para um cliente lento não travar os outros.

### Diferença importante em relação ao Pong
No Pong o cliente mandava **a própria posição**. Aqui o cliente manda só a **intenção**
(direção, ângulo da mira, botão de tiro) e o servidor decide o resto: colisão entre tanques,
recarga, alcance e dano. Assim ninguém consegue trapacear mandando posição falsa ou atirando
sem recarregar.

### Protocolo
```
Ao conectar   (S -> C): int playerId        (0..9, ou -1 = servidor cheio)
Todo frame    (C -> S): byte direção (-1 parado, 0 cima, 1 dir., 2 baixo, 3 esq.)
                        float ânguloMira, boolean atirando
A 60 Hz       (S -> C): byte qtdTanques
                          [byte id, boolean vivo, float x, float y, byte facing,
                           float anguloTorre, byte hp, float recarga, float respawn,
                           float escudo, short abates, short mortes] x qtdTanques
                        short qtdProjeteis
                          [byte dono, float x, float y] x qtdProjeteis
```
`Socket.setTcpNoDelay(true)` dos dois lados para os pacotes pequenos não atrasarem.

Explosões e o "piscar" ao levar dano não trafegam pela rede: o cliente deduz comparando um
snapshot com o anterior (vida diminuiu → pisca; estava vivo e morreu → explosão).
