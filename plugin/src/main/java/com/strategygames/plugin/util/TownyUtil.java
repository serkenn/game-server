package com.strategygames.plugin.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Soft-dependency wrapper for TownyAPI.
 *
 * Uses reflection so the plugin compiles and loads even when Towny is absent.
 * At runtime, if Towny is present, nation membership is resolved normally.
 * If Towny is absent, all lookups return null (graceful degradation).
 *
 * Towny plugin.yml soft-depend entry: soft-depend: [Towny]
 */
public final class TownyUtil {

    private static Boolean townyPresent = null;

    private TownyUtil() {}

    /**
     * Returns the Towny nation name for the player, or null if not in a nation
     * or if Towny is not installed.
     * The nation name is used as nationId throughout the game-logic API.
     */
    public static String getNationId(Player player) {
        if (!isTownyPresent()) return null;
        try {
            // TownyAPI.getInstance()
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Method getInstance = townyApiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);
            if (api == null) return null;

            // api.getResident(uuid)
            Method getResident = townyApiClass.getMethod("getResident", java.util.UUID.class);
            Object resident = getResident.invoke(api, player.getUniqueId());
            if (resident == null) return null;

            // resident.hasTown()
            Class<?> residentClass = resident.getClass();
            Method hasTown = residentClass.getMethod("hasTown");
            if (!(Boolean) hasTown.invoke(resident)) return null;

            // resident.getTown()
            Method getTown = residentClass.getMethod("getTown");
            Object town = getTown.invoke(resident);
            if (town == null) return null;

            // town.hasNation()
            Class<?> townClass = town.getClass();
            Method hasNation = townClass.getMethod("hasNation");
            if (!(Boolean) hasNation.invoke(town)) return null;

            // town.getNation().getName()
            Method getNation = townClass.getMethod("getNation");
            Object nation = getNation.invoke(town);
            Method getName = nation.getClass().getMethod("getName");
            return (String) getName.invoke(nation);

        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isTownyPresent() {
        if (townyPresent == null) {
            townyPresent = Bukkit.getPluginManager().getPlugin("Towny") != null;
        }
        return townyPresent;
    }
}
