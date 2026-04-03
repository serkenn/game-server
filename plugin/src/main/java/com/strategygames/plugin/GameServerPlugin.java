package com.strategygames.plugin;

import com.strategygames.plugin.api.GameApiClient;
import com.strategygames.plugin.command.DecisionCommand;
import com.strategygames.plugin.command.IcbmCommand;
import com.strategygames.plugin.command.NationCommand;
import com.strategygames.plugin.command.ResearchCommand;
import com.strategygames.plugin.listener.PlayerEventListener;
import org.bukkit.plugin.java.JavaPlugin;

public class GameServerPlugin extends JavaPlugin {

    private GameApiClient apiClient;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String apiUrl = getConfig().getString("api.url", "http://game-logic-api:8080");
        apiClient = new GameApiClient(apiUrl, getLogger());

        registerCommands();
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        getLogger().info("GameServerPlugin enabled. API URL: " + apiUrl);
    }

    @Override
    public void onDisable() {
        getLogger().info("GameServerPlugin disabled.");
    }

    public GameApiClient getApiClient() {
        return apiClient;
    }

    private void registerCommands() {
        var research = getCommand("research");
        if (research != null) research.setExecutor(new ResearchCommand(this));

        var icbm = getCommand("icbm");
        if (icbm != null) icbm.setExecutor(new IcbmCommand(this));

        var decision = getCommand("decision");
        if (decision != null) decision.setExecutor(new DecisionCommand(this));

        var nation = getCommand("nation");
        if (nation != null) nation.setExecutor(new NationCommand(this));
    }
}
