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

public class DecisionCommand implements CommandExecutor {

    private final GameServerPlugin plugin;

    public DecisionCommand(GameServerPlugin plugin) {
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

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            showAvailable(player, nationId);
            return true;
        }

        if (args[0].equalsIgnoreCase("execute") && args.length >= 2) {
            String target = args.length >= 3 ? args[2] : null;
            executeDecision(player, nationId, args[1], target);
            return true;
        }

        player.sendMessage("§eUsage: /decision <list|execute <decisionId> [targetNation]>");
        return true;
    }

    private void showAvailable(Player player, String nationId) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject response = plugin.getApiClient().getAvailableDecisions(nationId);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!response.get("success").getAsBoolean()) {
                    player.sendMessage("§cError: " + response.get("message").getAsString());
                    return;
                }
                player.sendMessage("§6=== Available Decisions ===");
                JsonArray data = response.getAsJsonArray("data");
                if (data == null || data.isEmpty()) {
                    player.sendMessage("§7No decisions available (all on cooldown).");
                    return;
                }
                for (JsonElement el : data) {
                    player.sendMessage("§e- " + el.getAsString());
                }
                player.sendMessage("§7Use /decision execute <id> to activate.");
            });
        });
    }

    private void executeDecision(Player player, String nationId, String decisionId, String targetNation) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject response = plugin.getApiClient().executeDecision(
                    nationId, decisionId, player.getUniqueId().toString(), targetNation);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!response.get("success").getAsBoolean()) {
                    player.sendMessage("§cDecision failed: " + response.get("message").getAsString());
                } else {
                    player.sendMessage("§aDecision executed: §e" + decisionId);
                }
            });
        });
    }
}
