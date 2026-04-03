package com.strategygames.plugin.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.strategygames.plugin.GameServerPlugin;
import com.strategygames.plugin.util.TownyUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationCommand implements CommandExecutor {

    private final GameServerPlugin plugin;

    public NationCommand(GameServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        String nationId = TownyUtil.getNationId(player);
        if (nationId == null) {
            player.sendMessage("§cYou are not part of a nation.");
            return true;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject response = plugin.getApiClient().getNationStats(nationId);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!response.get("success").getAsBoolean()) {
                    player.sendMessage("§cError: " + response.get("message").getAsString());
                    return;
                }
                JsonObject stats = response.getAsJsonObject("data");
                player.sendMessage("§6=== " + stats.get("nationName").getAsString() + " ===");
                player.sendMessage("§eTrait: §f"        + stats.get("trait").getAsString());
                player.sendMessage("§eResearch Points: §f" + stats.get("researchPoints").getAsInt());
                player.sendMessage("§ePrestige: §f"     + stats.get("prestige").getAsInt());
                player.sendMessage(String.format("§eProduction: §f%.0f%%",
                        stats.get("effectiveProductionModifier").getAsDouble() * 100));
                player.sendMessage(String.format("§eMorale: §f%.1f",
                        stats.get("effectiveMorale").getAsDouble()));

                JsonArray debuffs = stats.getAsJsonArray("activeDebuffs");
                if (debuffs != null && !debuffs.isEmpty()) {
                    player.sendMessage("§c--- Active Debuffs ---");
                    for (JsonElement el : debuffs) {
                        JsonObject d = el.getAsJsonObject();
                        player.sendMessage("§c  " + d.get("type").getAsString()
                                + " -" + d.get("magnitude").getAsDouble());
                    }
                }
            });
        });
        return true;
    }
}
