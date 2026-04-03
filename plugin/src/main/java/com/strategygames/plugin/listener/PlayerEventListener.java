package com.strategygames.plugin.listener;

import com.strategygames.plugin.GameServerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for MC events and delegates to the game-logic API where needed.
 * Extend this class to hook into additional Bukkit events.
 */
public class PlayerEventListener implements Listener {

    private final GameServerPlugin plugin;

    public PlayerEventListener(GameServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Future: sync player join with nation stats, notify of completed research, etc.
        plugin.getLogger().fine("Player joined: " + event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Future: flush any pending state
        plugin.getLogger().fine("Player quit: " + event.getPlayer().getName());
    }
}
