package me.whe.mcwordle;

import org.bukkit.Bukkit;
import org.bukkit.structure.Structure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class WordleController {

    private static Map<UUID, WordleGame> games = new HashMap<>();
    private static List<UUID> quitters = new ArrayList<>();
    private static final int Z_INCREMENT = 500;
    private static List<Integer> ZList = new ArrayList<>();

    private static Map<Character, Structure> letterStructures = new HashMap<>();

    private static final String ANSWERS_PATH = "/words/answers.txt";
    private static final String VALID_PATH = "/words/valid.txt";
    private static List<String> answers = new ArrayList<>();
    private static List<String> validWords = new ArrayList<>();

    public static void init() {
        // Structures
        for (char c = 'a'; c <= 'z'; c++) {
            InputStream file = MCWordle.class.getResourceAsStream("/structures/wordle_" + c + ".nbt");

            try {
                letterStructures.put(c, Bukkit.getStructureManager().loadStructure(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Words
        try {
            // Possible answers
            BufferedReader answersReader = new BufferedReader(new InputStreamReader(MCWordle.class.getResourceAsStream(ANSWERS_PATH)));

            while (answersReader.ready()) {
                answers.add(answersReader.readLine());
            }

            answersReader.close();


            // Valid guesses
            BufferedReader validReader = new BufferedReader(new InputStreamReader(MCWordle.class.getResourceAsStream(VALID_PATH)));

            while (validReader.ready()) {
                validWords.add(validReader.readLine());
            }

            validReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addGame(WordleGame game, UUID uuid) {
        ZList.add(game.getZ());
        game.createGame();
        games.put(uuid, game);
    }

    public static void removeGame(UUID uuid) {
        if (games.containsKey(uuid)) {
            WordleGame game = games.get(uuid);
            game.destroyGame();
            ZList.remove((Integer) game.getZ());
            games.remove(uuid);
        }
    }

    public static void removeAllGames() {
        for (UUID key : games.keySet()) {
            removeGame(key);
        }
    }

    public static int getNextZ() {
        int nextZ = 0;

        while (ZList.contains(nextZ)) {
            nextZ += Z_INCREMENT;
        }

        return nextZ;
    }

    public static Map<UUID, WordleGame> getGames() {
        return games;
    }

    public static List<UUID> getQuitters() {
        return quitters;
    }

    public static Map<Character, Structure> getLetterStructures() {
        return letterStructures;
    }

    public static List<String> getAnswers() {
        return answers;
    }

    public static List<String> getValidWords() {
        return validWords;
    }
}
