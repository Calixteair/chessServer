package com.chessserver.server;

import com.chessserver.exceptions.InvalidPseudoException;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private static final int MIN_PSEUDO_LENGTH = 1;
    private static final int MAX_PSEUDO_LENGTH = 20;
    private static final Pattern VALID_PSEUDO_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    private final Map<String, ClientHandler> connectedPlayers;

    public PlayerManager() {
        this.connectedPlayers = new ConcurrentHashMap<>();
    }

    /**
     * Ajoute un joueur avec un pseudo, après validation.
     *
     * @param pseudo Le pseudo du joueur.
     * @param handler Le gestionnaire de ce joueur.
     * @throws InvalidPseudoException Si le pseudo est invalide.
     */
    public void addPlayer(String pseudo, ClientHandler handler) throws InvalidPseudoException {
        validatePseudo(pseudo);
        if (connectedPlayers.containsKey(pseudo)) {
            throw new InvalidPseudoException("Le pseudo '" + pseudo + "' est déjà pris.");
        }
        connectedPlayers.put(pseudo, handler);
    }

    /**
     * Retire un joueur de la liste des joueurs connectés.
     *
     * @param pseudo Le pseudo du joueur à retirer.
     */
    public void removePlayer(String pseudo) {
        connectedPlayers.remove(pseudo);
    }

    /**
     * Valide le pseudo selon les règles définies.
     *
     * @param pseudo Le pseudo à valider.
     * @throws InvalidPseudoException Si le pseudo ne respecte pas les contraintes.
     */
    private void validatePseudo(String pseudo) throws InvalidPseudoException {
        if (pseudo == null || pseudo.length() < MIN_PSEUDO_LENGTH || pseudo.length() > MAX_PSEUDO_LENGTH) {
            throw new InvalidPseudoException("Le pseudo doit contenir entre " + MIN_PSEUDO_LENGTH + " et " + MAX_PSEUDO_LENGTH + " caractères.");
        }

        if (!VALID_PSEUDO_PATTERN.matcher(pseudo).matches()) {
            throw new InvalidPseudoException("Le pseudo ne doit contenir que des caractères alphanumériques (pas de caractères spéciaux).");
        }
    }
}

