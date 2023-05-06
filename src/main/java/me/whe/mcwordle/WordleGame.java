package me.whe.mcwordle;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public class WordleGame {

    private Player player;
    private final int z;

    private final String word;

    private static final World world = Bukkit.getWorlds().get(0);

    private static final double PLAYER_X = -35.0 + 0.5;
    private static final double PLAYER_Y = world.getMaxHeight() - 18.0;
    private static final float PLAYER_YAW = -90.0f;
    private static final float PLAYER_PITCH = 0.0f;

    private static final int TILE_WIDTH = 5;
    private static final int TILE_HEIGHT = 5;
    private static final int TILE_CEILING = world.getMaxHeight() - 3;

    private static final int NUM_ROWS = 6;
    private static final int NUM_TILES_PER_ROW = 5;

    private static final Title.Times TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(3500),
            Duration.ofMillis(500)
    );

    private enum WordleColor {
        GREEN,
        YELLOW,
        GRAY,
        DONE
    }

    private static final Map<WordleColor, Material> blockMap = new HashMap<>(Map.of(
            WordleColor.GREEN, Material.LIME_CONCRETE,
            WordleColor.YELLOW, Material.YELLOW_CONCRETE,
            WordleColor.GRAY, Material.LIGHT_GRAY_CONCRETE
    ));

    private static final Map<WordleColor, Sound> soundMap = new HashMap<>(Map.of(
            WordleColor.GREEN, Sound.BLOCK_NOTE_BLOCK_BELL,
            WordleColor.YELLOW, Sound.BLOCK_NOTE_BLOCK_HARP,
            WordleColor.GRAY, Sound.BLOCK_NOTE_BLOCK_BASS
    ));

    private static final List<String> winMessages = Arrays.asList(
            "Genius!",
            "Magnificent!",
            "Impressive!",
            "Splendid!",
            "Great!",
            "Phew..."
    );

    private Location previousLocation;

    private List<List<Tile>> tileList = new ArrayList<>();
    private boolean guessing = false;
    private boolean guessedWord = false;
    private int guessNum = 0;

    private List<WordleColor> colors = new ArrayList<>();
    private int colorIndex = 0;

    public WordleGame(Player player, int z) {
        this.player = player;
        this.z = z;

        Random rand = new Random();
        List<String> answers = WordleController.getAnswers();
        this.word = answers.get(rand.nextInt(answers.size()));
    }

    public void createGame() {
        for (int y = 0; y < NUM_ROWS; y++) {
            tileList.add(new ArrayList<>());
            for (int z = -NUM_TILES_PER_ROW / 2; z <= NUM_TILES_PER_ROW / 2; z++) {
                Tile tile = new Tile(
                        new Location(world, 0, TILE_CEILING - y * (TILE_HEIGHT + 1), z * (TILE_WIDTH + 1)),
                        TILE_WIDTH,
                        TILE_HEIGHT
                );
                tileList.get(y).add(tile);
                tile.createTile('\0');
            }
        }

        Bukkit.getScheduler().runTaskLater(MCWordle.getPlugin(), () -> {
            previousLocation = player.getLocation();

            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(new Location(world, PLAYER_X, PLAYER_Y, z + 0.5, PLAYER_YAW, PLAYER_PITCH));

            final Component mainTitle = Component.text("WORDLE", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true);
            final Component subtitle = Component.text("Guess the word in six tries or less!", NamedTextColor.YELLOW);
            final Title title = Title.title(mainTitle, subtitle, TIMES);
            player.showTitle(title);

            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);

            final Component introMessage = Component.text("Welcome to Wordle! ", NamedTextColor.GREEN)
                    .decoration(TextDecoration.BOLD, true)
                    .append(Component.text("Type a five letter word in chat to guess it. You have six tries. Good luck!", NamedTextColor.WHITE)
                            .decoration(TextDecoration.BOLD, false)
                    );

            player.sendMessage(introMessage);
        }, 10L);
    }

    public void destroyGame() {
        for (List<Tile> row : tileList) {
            for (Tile tile : row) {
                tile.destroyTile();
            }
        }

        player.setGameMode(player.getPreviousGameMode());
        player.teleport(previousLocation);
    }

    public void tryGuess(String guess) {
        if (guessNum >= 6) {return;}

        guessing = true;

        List<Tile> tileRow = tileList.get(guessNum);
        for (int i = 0; i < tileRow.size(); i++) {
            tileRow.get(i).setLetter(guess.charAt(i));
        }

        player.playSound(player, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f);

        processGuess(guess);

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                WordleColor color = nextColor();
                if (color == WordleColor.DONE) {
                    if (guessedWord) {
                        onGuessedWord();
                    } else if (guessNum >= 5) {
                        onLastGuess();
                    } else {
                        guessNum++;
                        guessing = false;
                    }

                    this.cancel();
                    return;
                }
                tileRow.get(colorIndex - 1).fillTile(blockMap.get(color));
            }
        };

        runnable.runTaskTimer(MCWordle.getPlugin(), 20L, 5L);
    }

    public boolean isGuessing() {
        return guessing;
    }

    private void processGuess(String guess) {
        this.colorIndex = 0;

        // If it's the word, we don't have to process anything
        if (word.compareTo(guess) == 0) {
            this.colors = Collections.nCopies(word.length(), WordleColor.GREEN);
            guessedWord = true;
            return;
        }

        List<WordleColor> colors = new ArrayList<>();

        List<Character> unmatchedGuess = new ArrayList<>();
        List<Character> unmatchedWord = new ArrayList<>();

        // Pass one: green tiles
        for (int i = 0; i < guess.length(); i++) {
            char guessChar = guess.charAt(i), wordChar = word.charAt(i);
            if (guessChar == wordChar) {
                colors.add(WordleColor.GREEN);
                unmatchedGuess.add('-');
                unmatchedWord.add('-');
            } else {
                colors.add(WordleColor.GRAY);
                unmatchedGuess.add(guessChar);
                unmatchedWord.add(wordChar);
            }
        }

        // Pass two: yellow tiles
        for (int i = 0; i < unmatchedGuess.size(); i++) {
            char unmatchedGuessChar = unmatchedGuess.get(i);
            if (unmatchedGuessChar != '-' && unmatchedWord.contains(unmatchedGuessChar)) {
                int index = unmatchedWord.indexOf(unmatchedGuessChar);
                colors.set(i, WordleColor.YELLOW);
                unmatchedWord.set(index, '-');
            }
        }

        this.colors = colors;
    }

    private WordleColor nextColor() {
        if (colorIndex >= colors.size()) {
            return WordleColor.DONE;
        }

        WordleColor color = colors.get(colorIndex++);

        player.playSound(player, soundMap.get(color), 1.0f, (float) colorIndex * 0.1f + 0.4f);

        return color;
    }

    private void onGuessedWord() {
        final Component mainTitle = Component.text(winMessages.get(guessNum), NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true);
        final Component subtitle = Component.text("You got the word in " + (guessNum + 1) + " out of 6 guesses.", NamedTextColor.YELLOW);
        final Title title = Title.title(mainTitle, subtitle, TIMES);
        player.showTitle(title);

        player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);

        Bukkit.getScheduler().runTaskLater(MCWordle.getPlugin(), () -> WordleController.removeGame(player.getUniqueId()), 90L);
    }

    private void onLastGuess() {
        final Component mainTitle = Component.text("Game Over!", NamedTextColor.RED).decoration(TextDecoration.BOLD, true);
        final Component subtitle = Component.text("The word was " + word.toUpperCase() + ".", NamedTextColor.YELLOW);
        final Title title = Title.title(mainTitle, subtitle, TIMES);
        player.showTitle(title);

        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

        Bukkit.getScheduler().runTaskLater(MCWordle.getPlugin(), () -> WordleController.removeGame(player.getUniqueId()), 90L);
    }


    public int getZ() {
        return z;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
