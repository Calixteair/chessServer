# chessServer

Serveur Java multi-thread du jeu d'échecs. Deux points d'entrée branchés sur le même salon :

- **TCP** (port 8090) pour le client JavaFX ;
- **WebSocket** (port 8091) pour le client web.

Un joueur JavaFX peut donc affronter un joueur web. Le serveur fait autorité : il valide chaque coup avec le moteur `chess-game` et pousse l'état complet de la partie aux deux joueurs.

## Lancer

```bash
cd ../chessGame && mvn install          # moteur de règles
cd ../chessServer && mvn package
java -jar target/chess-server-1.0-SNAPSHOT.jar
# ou : mvn exec:java
```

Ports : variables `CHESS_TCP_PORT` et `CHESS_WS_PORT`.

## Protocole

Texte, une ligne par message, champs séparés par `;`. Identique sur TCP et WebSocket.

| Client → serveur | |
| --- | --- |
| `CONNECT;pseudo` | entrer dans le salon |
| `LIST_GAMES` | demander la liste des parties en attente |
| `CREATE_GAME;nom` | créer une partie (le créateur a les blancs) |
| `JOIN_GAME;id` | rejoindre une partie |
| `MOVE;e2e4` | jouer un coup UCI (promotion : `e7e8q`) |
| `RESIGN` / `LEAVE_GAME` | abandonner / quitter la partie |
| `PING` / `DISCONNECT` | maintien de connexion / déconnexion |

| Serveur → client | |
| --- | --- |
| `WELCOME;pseudo` | connexion acceptée |
| `GAMES;id,nom,créateur\|…` | parties en attente, poussées à chaque changement |
| `GAME_CREATED;id;nom` | partie créée, en attente d'adversaire |
| `GAME_START;id;nom;blancs;noirs;w\|b` | début de partie, avec la couleur du destinataire |
| `STATE;fen;statut;vainqueur;échec;dernierCoup;historique;coupsLégaux` | état complet après chaque changement |
| `OPPONENT_LEFT;pseudo` | l'adversaire a quitté la table |
| `ERROR;code;message` | requête refusée |

Statuts : `ONGOING`, `CHECKMATE`, `STALEMATE`, `DRAW_FIFTY_MOVES`, `DRAW_REPETITION`, `DRAW_INSUFFICIENT_MATERIAL`, `RESIGNATION`, `ABANDONED`. Quitter une partie en cours la fait perdre par abandon.
