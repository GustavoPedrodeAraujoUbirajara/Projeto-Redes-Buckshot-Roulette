package Game;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente (Jogador)
 * - Apenas exibe mensagens do servidor e envia comandos digitados.
 */
public class Cliente {

    public static void main(String[] args) {

        String host = "localhost";
        int porta = 12345;

        // Permite rodar: java Game.Cliente 127.0.0.1 12345
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) porta = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(host, porta)) {

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            Scanner scanner = new Scanner(System.in);

            // =========================
            // Thread que recebe mensagens do servidor
            // =========================
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Conexão encerrada pelo servidor.");
                }
            }, "server-listener").start();

            // =========================
            // Envia entradas do usuário para o servidor
            // =========================
            while (!socket.isClosed()) {
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine();
                out.println(input);
            }

        } catch (IOException e) {
            System.out.println("Falha ao conectar no servidor: " + e.getMessage());
        }
    }
}
