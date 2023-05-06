package me.whe.mcwordle.commands;

import me.whe.mcwordle.WordleController;
import me.whe.mcwordle.WordleGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WordleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            UUID uuid = player.getUniqueId();
            if (!WordleController.getGames().containsKey(uuid)) {
                WordleController.addGame(new WordleGame(player, WordleController.getNextZ()), uuid);
            } else {
                player.sendMessage(Component.text("You are already in a game!", NamedTextColor.RED));
            }
        }
        return true;
    }

}
