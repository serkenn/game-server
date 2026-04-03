package com.strategygames.plugin.command;

import com.google.gson.JsonObject;
import com.strategygames.plugin.GameServerPlugin;
import com.strategygames.plugin.util.TownyUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IcbmCommand implements CommandExecutor {

    private final GameServerPlugin plugin;

    public IcbmCommand(GameServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("strategygames.icbm")) {
            player.sendMessage("§cInsufficient permissions.");
            return true;
        }

        String nationId = TownyUtil.getNationId(player);
        if (nationId == null) {
            player.sendMessage("§cYou are not part of a nation.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            showStatus(player, nationId);
            return true;
        }

        if (args[0].equalsIgnoreCase("build")) {
            startBuild(player, nationId);
            return true;
        }

        if (args[0].equalsIgnoreCase("launch") && args.length >= 2) {
            launch(player, nationId, args[1]);
            return true;
        }

        player.sendMessage("§eUsage: /icbm <status|build|launch <targetNation>>");
        return true;
    }

    private void showStatus(Player player, String nationId) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject response = plugin.getApiClient().getIcbmStatus(nationId);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!response.get("success").getAsBoolean()) {
                    player.sendMessage("§cError: " + response.get("message").getAsString());
                    return;
                }
                JsonObject silo = response.getAsJsonObject("data");
                player.sendMessage("§6=== ICBM Silo Status ===");
                player.sendMessage("§eState: §f" + silo.get("state").getAsString());
                if (!silo.get("readyAt").isJsonNull()) {
                    player.sendMessage("§eReady at: §f" + silo.get("readyAt").getAsString());
                }
            });
        });
    }

    private void startBuild(Player player, String nationId) {
        player.sendMessage("§eStarting ICBM silo construction... (requires T3_ICBM technology)");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject response = plugin.getApiClient().buildIcbm(nationId);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!response.get("success").getAsBoolean()) {
                    player.sendMessage("§cCannot build: " + response.get("message").getAsString());
                } else {
                    player.sendMessage("§aSilo construction started! Estimated completion: ~7 days.");
                }
            });
        });
    }

    private void launch(Player player, String nationId, String targetNation) {
        player.sendMessage("§c§lLaunching ICBM at " + targetNation + "...");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject response = plugin.getApiClient().launchIcbm(
                    nationId, targetNation, player.getUniqueId().toString());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!response.get("success").getAsBoolean()) {
                    player.sendMessage("§cLaunch failed: " + response.get("message").getAsString());
                } else {
                    JsonObject data = response.getAsJsonObject("data");
                    boolean intercepted = data.get("intercepted").getAsBoolean();
                    if (intercepted) {
                        player.sendMessage("§eICBM was partially intercepted by " + targetNation + ".");
                    } else {
                        player.sendMessage("§aICBM struck " + targetNation + ". Reload time: ~2 days.");
                    }
                }
            });
        });
    }
}
