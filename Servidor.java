package Game;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Servidor {

    private static final int PORTA = 12345;
    private static final int MAX_JOGADORES = 3;

    // =========================
    // Estrutura para fila de mensagens concorrentes
    // =========================
    static class IncomingMessage {
        final int playerIndex;
        final String text;
        final long timestampNanos;

        IncomingMessage(int playerIndex, String text) {
            this.playerIndex = playerIndex;
            this.text = text;
            this.timestampNanos = System.nanoTime();
        }
    }

    static class PlayerConnection {
        final Socket socket;
        final BufferedReader in;
        final PrintWriter out;
        String nome;

        PlayerConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        }

        void send(String msg) {
            out.println(msg);
        }

        void closeQuiet() {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(PORTA);
        System.out.println("Servidor iniciado na porta " + PORTA);

        List<PlayerConnection> jogadores = new ArrayList<>();

        while (jogadores.size() < MAX_JOGADORES) {
            Socket socket = serverSocket.accept();
            PlayerConnection pc = new PlayerConnection(socket);
            jogadores.add(pc);

            pc.send("CONECTADO AO SERVIDOR ✅");
            pc.send("DIGITE SEU NOME:");
            System.out.println("Cliente conectado: " + socket.getRemoteSocketAddress());
        }

        Game game = new Game();
        for (PlayerConnection pc : jogadores) {
            String nome = safeReadLine(pc.in);
            if (nome == null || nome.trim().isEmpty()) nome = "Jogador" + (game.getPlayers().size() + 1);
            pc.nome = nome.trim();
            game.addPlayer(new Player(pc.nome));
        }

        broadcast(jogadores, "\nTODOS OS JOGADORES CONECTADOS!");
        broadcast(jogadores, "BEM-VINDO AO BUCKSHOT ROULETTE");
        broadcast(jogadores, "REGRAS: Servidor valida e controla turnos.");

        BlockingQueue<IncomingMessage> inbox = new LinkedBlockingQueue<>();

        for (int i = 0; i < jogadores.size(); i++) {
            final int idx = i;
            PlayerConnection pc = jogadores.get(i);

            new Thread(() -> {
                try {
                    String line;
                    while ((line = pc.in.readLine()) != null) {
                        inbox.offer(new IncomingMessage(idx, line.trim()));
                    }
                } catch (IOException ignored) {}
            }).start();
        }

        boolean continuarServidor = true;

        while (continuarServidor) {

            broadcast(jogadores, "\nDigite START para iniciar.");
            waitAllPlayersCommand(jogadores, inbox, "START");

            game.resetMatch();
            broadcast(jogadores, "\nPARTIDA INICIADA!");

            String ammo = game.consumePendingAmmoInfo();
            if (ammo != null) broadcast(jogadores, ammo);

            while (!game.isGameOver()) {

                int current = game.getCurrentPlayerIndex();
                Player currentPlayer = game.getCurrentPlayer();

                broadcast(jogadores, game.getStateString());
                broadcast(jogadores, "É a vez de: " + currentPlayer.getName());
                jogadores.get(current).send("SUA VEZ ➜ Digite o número do ALVO (1-" + MAX_JOGADORES + ")");

                IncomingMessage msg = waitForTurnMessage(jogadores, inbox, current);
                Integer targetIndex = parseTarget(msg.text);

                if (targetIndex == null) {
                    jogadores.get(current).send("Digite um número válido.");
                    continue;
                }

                Game.ShotResult result = game.processShot(current, targetIndex);

                broadcast(jogadores, result.message);

                // =========================
                // BLOCO: Artes conforme resultado do tiro
                // =========================
                if (result.type == Game.ShotType.HIT) {
                    broadcast(jogadores, Game.HIT_ART);
                } 
                else if (result.type == Game.ShotType.MISS) {
                    broadcast(jogadores, Game.MISS_ART);
                } 
                else if (result.type == Game.ShotType.RELOAD) {
                    String reloadInfo = game.consumePendingAmmoInfo();
                    if (reloadInfo != null) broadcast(jogadores, reloadInfo);
                }

                if (!result.extraTurn) {
                    game.advanceTurn();
                }
            }

            int winnerIdx = game.getWinnerIndexOrMinus1();
            String winnerName = (winnerIdx >= 0) ? game.getPlayers().get(winnerIdx).getName() : "Ninguém";

            broadcast(jogadores, "\n🏆 VENCEDOR: " + winnerName);
            broadcast(jogadores, "Fim da partida.");

            broadcast(jogadores, "Querem jogar novamente? (S/N)");
            Map<Integer, Boolean> respostas = waitAllYesNo(jogadores, inbox);

            boolean todosSim = true;
            for (boolean v : respostas.values()) if (!v) todosSim = false;

            if (!todosSim) {
                continuarServidor = false;
                broadcast(jogadores, "Servidor encerrando.");
            } else {
                broadcast(jogadores, "Nova partida iniciando...");
            }
        }

        for (PlayerConnection pc : jogadores) pc.closeQuiet();
        serverSocket.close();
    }

    // =========================
    // Helpers
    // =========================

    static void broadcast(List<PlayerConnection> jogadores, String msg) {
        for (PlayerConnection pc : jogadores) {
            if (!pc.socket.isClosed()) pc.send(msg);
        }
    }

    static String safeReadLine(BufferedReader in) {
        try { return in.readLine(); } catch (IOException e) { return null; }
    }

    static void waitAllPlayersCommand(List<PlayerConnection> jogadores,
                                      BlockingQueue<IncomingMessage> inbox,
                                      String command) throws InterruptedException {
        Set<Integer> ok = new HashSet<>();
        while (ok.size() < jogadores.size()) {
            IncomingMessage m = inbox.take();
            if (m.text.equalsIgnoreCase(command)) {
                ok.add(m.playerIndex);
                jogadores.get(m.playerIndex).send("Confirmado (" + ok.size() + "/" + jogadores.size() + ")");
            } else {
                jogadores.get(m.playerIndex).send("Digite: " + command);
            }
        }
    }

    static IncomingMessage waitForTurnMessage(List<PlayerConnection> jogadores,
                                              BlockingQueue<IncomingMessage> inbox,
                                              int currentPlayerIndex) throws InterruptedException {
        while (true) {
            IncomingMessage m = inbox.take();
            if (m.playerIndex == currentPlayerIndex) return m;
            jogadores.get(m.playerIndex).send("⏳ Não é sua vez.");
        }
    }

    static Integer parseTarget(String text) {
        try { return Integer.parseInt(text.trim()) - 1; } catch (Exception e) { return null; }
    }

    static Map<Integer, Boolean> waitAllYesNo(List<PlayerConnection> jogadores,
                                              BlockingQueue<IncomingMessage> inbox) throws InterruptedException {
        Map<Integer, Boolean> ans = new HashMap<>();
        while (ans.size() < jogadores.size()) {
            IncomingMessage m = inbox.take();
            String t = m.text.toUpperCase();
            if (t.equals("S") || t.equals("SIM")) ans.put(m.playerIndex, true);
            else if (t.equals("N") || t.equals("NAO") || t.equals("NÃO")) ans.put(m.playerIndex, false);
            else jogadores.get(m.playerIndex).send("Responda S ou N.");
        }
        return ans;
    }
}
