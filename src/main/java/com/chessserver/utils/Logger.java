package com.chessserver.utils;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static final boolean LOG_TO_FILE = true; // Active ou désactive les logs dans un fichier
    private static final String LOG_FILE = "server.log"; // Chemin du fichier de logs
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Méthode pour loguer un message d'information.
     *
     * @param message Le message à loguer.
     */
    public static void info(String message) {
        log("INFO", message);
    }

    /**
     * Méthode pour loguer un message d'erreur.
     *
     * @param message Le message à loguer.
     */
    public static void error(String message) {
        log("ERROR", message);
    }

    /**
     * Méthode pour loguer un message d'avertissement.
     *
     * @param message Le message à loguer.
     */
    public static void warn(String message) {
        log("WARN", message);
    }

    /**
     * Méthode pour loguer un message de débogage.
     *
     * @param message Le message à loguer.
     */
    public static void debug(String message) {
        log("DEBUG", message);
    }

    /**
     * Méthode principale pour loguer un message avec un niveau spécifique.
     *
     * @param level   Le niveau du log (INFO, ERROR, WARN, DEBUG).
     * @param message Le message à loguer.
     */
    private static void log(String level, String message) {
        String timeStamp = dateFormat.format(new Date());
        String formattedMessage = String.format("[%s] [%s] %s", timeStamp, level, message);

        // Affiche dans la console
        System.out.println(formattedMessage);

        // Si activé, écrit dans un fichier de log
        if (LOG_TO_FILE) {
            writeToFile(formattedMessage);
        }
    }

    /**
     * Écrit un message dans un fichier de log.
     *
     * @param message Le message à écrire.
     */
    private static void writeToFile(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(message);
        } catch (IOException e) {
            System.err.println("Impossible d'écrire dans le fichier de log : " + e.getMessage());
        }
    }
}
