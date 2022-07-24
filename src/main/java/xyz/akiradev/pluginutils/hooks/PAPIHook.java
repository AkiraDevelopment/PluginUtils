package xyz.akiradev.pluginutils.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PAPIHook {

    private static Boolean enabled;

    /**
     * @return true if PlaceholderAPI is enabled, otherwise false
     */
    public static boolean enabled() {
        if (enabled != null)
            return enabled;
        return enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Applies placeholders from PlaceholderAPI to strings
     *
     * @param player The OfflinePlayer to apply placeholders with
     * @param text The text to replace placeholders
     * @return A string with replaced placeholders
     */
    public static String applyPlaceholders(OfflinePlayer player, String text) {
        if (enabled())
            return PlaceholderAPI.setPlaceholders(player, text);
        return text;
    }

}
