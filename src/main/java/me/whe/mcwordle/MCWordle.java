package me.whe.mcwordle;

import me.whe.mcwordle.commands.WordleCommand;
import me.whe.mcwordle.listeners.MCWordleListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCWordle extends JavaPlugin {

    private static MCWordle plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic

        plugin = this;

        WordleController.init();

        getServer().getPluginManager().registerEvents(new MCWordleListener(), this);

        getServer().getPluginCommand("wordle").setExecutor(new WordleCommand());

        System.out.println("Started mcwordle");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        WordleController.removeAllGames();

        System.out.println("Stopped mcwordle");
    }

    public static MCWordle getPlugin() {
        return plugin;
    }
}
