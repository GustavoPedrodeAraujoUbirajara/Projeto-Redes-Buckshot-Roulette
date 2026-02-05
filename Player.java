package Game;

/**
 * Representa um jogador no servidor.
 * Toda a lógica de vida/morte fica aqui para o servidor validar as jogadas.
 */
public class Player {
    private final String name;
    private int life;
    private boolean alive;

    public Player(String name) {
        this.name = name;
        reset();
    }

    public String getName() {
        return name;
    }

    public int getLife() {
        return life;
    }

    public boolean isAlive() {
        return alive;
    }

    // =========================
    // Aplica dano e valida morte
    // =========================
    // Testa se o jogador ainda tem vida e atualiza o estado (vivo/morto).
    public void loseLife() {
        if (!alive) return;
        life--;
        if (life <= 0) {
            alive = false;
            life = 0;
        }
    }

    // =========================
    // Reseta estado do jogador para nova partida
    // =========================
    // Reutiliza a mesma conexão/socket, mas reinicia as variáveis do jogador.
    public void reset() {
        this.life = 3;
        this.alive = true;
    }
}
