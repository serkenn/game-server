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

public class ResearchCommand implements CommandExecutor {

    private final GameServerPlugin plugin;

    public ResearchCommand(GameServerPlugin plugin) {
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

        if (args.length == 0 || args[0].equalsIgnoreCase("candidates")) {
            showCandidates(player, nationId);
            return true;
        }

        if (args[0].equalsIgnoreCase("start") && args.length >= 2) {
            startResearch(player, nationId, args[1]);
            return true;
        }

        player.sendMessage("§eUsage: /research <candidates|start <techId>>");
        return true;
    }

    private void showCandidates(Player player, String nationId) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject response = plugin.getApiClient().getResearchCandidates(nationId);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!response.get("success").getAsBoolean()) {
                    player.sendMessage("§cFailed to fetch research candidates: "
                            + response.get("message").getAsString());
                    return;
                }
                player.sendMessage("§6=== Research Candidates ===");
                JsonArray data = response.getAsJsonArray("data");
                if (data == null || data.isEmpty()) {
                    player.sendMessage("§7No candidates available.");
                    return;
                }
                for (JsonElement el : data) {
                    JsonObject tech = el.getAsJsonObject();
                    player.sendMessage(String.format("§e%s §7[%s] §f- %s §7(Cost: %d RP, Time: %ds)",
                            tech.get("name").getAsString(),
                            tech.get("id").getAsString(),
                            tech.get("category").getAsString(),
                            tech.get("researchCost").getAsInt(),
                            tech.get("researchTimeSeconds").getAsLong()));
                }
                player.sendMessage("§7Use /research start <techId> to begin.");
            });
        });
    }

    private void startResearch(Player player, String nationId, String techId) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject response = plugin.getApiClient().startResearch(
                    nationId, techId, player.getUniqueId().toString());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!response.get("success").getAsBoolean()) {
                    player.sendMessage("§cCannot start research: " + response.get("message").getAsString());
                } else {
                    player.sendMessage("§aResearch started: §e" + techId);
                }
            });
        });
    }
}
