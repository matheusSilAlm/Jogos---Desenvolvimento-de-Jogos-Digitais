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

### Power-ups
A cada 5–9 s aparece um item no campo (máx. 3 ao mesmo tempo; some após 20 s, piscando no
final). Basta passar por cima com o tanque.

| Item | Chance | Efeito |
|---|---|---|
| 🔧 **Chave** (verde) | 45% | Recupera **2 de vida**. Não é consumida se a vida estiver cheia. |
| 🛡 **Escudo** (azul) | 40% | Ganha **+1 quadradinho azul** na barra de vida. O próximo tiro recebido é absorvido inteiro e o escudo some. Não acumula. |
| ⚡ **Raio** (laranja) | 15% (raro) | **Dano x2** por 10 s: o tiro fica maior e laranja e tira 2 de vida. Pegar outro renova o tempo. |

Escudo e dano x2 são perdidos ao morrer. Chances, tempos e quantidades ficam em
`GameState.java` (bloco `POWER-UPS`).

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

**Liberar a porta no firewall do Windows** (uma vez, no PowerShell **como Administrador**):
```powershell
New-NetFirewallRule -DisplayName "Tanques 12345" -Direction Inbound -Protocol TCP -LocalPort 12345 -Action Allow
```
(equivalente no cmd: `netsh advfirewall firewall add rule name="Tanques 12345" dir=in action=allow protocol=TCP localport=12345`)

**Erro "Address already in use"** (um servidor antigo ainda está rodando):
```powershell
netstat -ano | findstr :12345      # o último número da linha LISTENING é o PID
taskkill /PID <PID> /F
```

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
├── PowerUp.java        item no campo (reparo, escudo, dano)                  (sem LibGDX)
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
                           float escudo, byte armadura, float danoX2,
                           short abates, short mortes] x qtdTanques
                        short qtdProjeteis
                          [byte dono, float x, float y, byte dano] x qtdProjeteis
                        byte qtdPowerUps
                          [byte tipo, float x, float y, float tempoRestante] x qtdPowerUps
```
`Socket.setTcpNoDelay(true)` dos dois lados para os pacotes pequenos não atrasarem.

Explosões e o "piscar" ao levar dano não trafegam pela rede: o cliente deduz comparando um
snapshot com o anterior (vida diminuiu → pisca; estava vivo e morreu → explosão).
