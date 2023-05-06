package me.whe.mcwordle.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.whe.mcwordle.MCWordle;
import me.whe.mcwordle.WordleController;
import me.whe.mcwordle.WordleGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class MCWordleListener implements Listener {

    @EventHandler
    public void onChatEvent(AsyncChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (WordleController.getGames().containsKey(uuid)) {
            e.setCancelled(true);
            WordleGame game = WordleController.getGames().get(uuid);
            if (!game.isGuessing()) {
                String guess = PlainTextComponentSerializer.plainText().serialize(e.message()).toLowerCase();
                if (guess.length() != 5) {
                    player.sendMessage(Component.text("Your message is not five letters long.", NamedTextColor.RED));
                } else if (!guess.chars().allMatch(Character::isLetter)) {
                    player.sendMessage(Component.text("Your message contains non-letters.", NamedTextColor.RED));
                } else if (!WordleController.getValidWords().contains(guess)) {
                    player.sendMessage(Component.text("Invalid word.", NamedTextColor.RED));
                } else {
                    Bukkit.getScheduler().runTask(MCWordle.getPlugin(), () -> game.tryGuess(guess));
                }
            } else {
                player.sendMessage(Component.text("Please wait until your previous guess is processed.", NamedTextColor.RED));
            }
        }
    }


    @EventHandler
    public void onMoveEvent(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (WordleController.getGames().containsKey(uuid)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (WordleController.getQuitters().contains(uuid)) {
            player.sendMessage(Component.text("Wordle game ended because of disconnect.", NamedTextColor.RED));
            WordleController.getQuitters().remove(uuid);
        }
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (WordleController.getGames().containsKey(uuid)) {
            WordleController.removeGame(uuid);
            WordleController.getQuitters().add(uuid);
        }
    }
}
