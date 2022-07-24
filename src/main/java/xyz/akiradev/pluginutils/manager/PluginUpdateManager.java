package xyz.akiradev.pluginutils.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.akiradev.pluginutils.PluginUtils;
import xyz.akiradev.pluginutils.config.CommentedFileConfiguration;
import xyz.akiradev.pluginutils.utils.StringPlaceholders;
import xyz.akiradev.pluginutils.utils.Utils;

public class PluginUpdateManager extends Manager implements Listener {

    protected final PluginUtils plugin;
    private String updateVersion;

    public PluginUpdateManager(PluginUtils plugin) {
        super(plugin);
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void reload() {
        File configFile = new File(this.plugin.getPluginUtilsDataFolder(), "config.yml");

        CommentedFileConfiguration configuration = CommentedFileConfiguration.loadConfiguration(configFile);
        if (!configuration.contains("check-updates")) {
            configuration.set("check-updates", true, "Should all plugins running RoseGarden check for updates?", "RoseGarden is a core library created by Rosewood Development");
            configuration.save();
        }

        if (!configuration.getBoolean("check-updates") || this.plugin.getSpigotId() == -1)
            return;

        // Check for updates
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                String latestVersion = this.getLatestVersion();
                String currentVersion = this.plugin.getDescription().getVersion();

                if (Utils.isUpdateAvailable(latestVersion, currentVersion)) {
                    this.updateVersion = latestVersion;
                    Utils.getLogger().info("An update for " + this.plugin.getName() + " (v" + this.updateVersion + ") is available! You are running v" + currentVersion + ".");
                }
            } catch (Exception e) {
                Utils.getLogger().warning("An error occurred checking for an update. There is either no established internet connection or the Spigot API is down.");
            }
        });
    }

    @Override
    public void disable() {

    }

    /**
     * Gets the latest version of the plugin from the Spigot Web API
     *
     * @return the latest version of the plugin from Spigot
     * @throws IOException if a network error occurs
     */
    private String getLatestVersion() throws IOException {
        URL spigot = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.plugin.getSpigotId());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(spigot.openStream()))) {
            return reader.readLine();
        }
    }

    /**
     * @return the version of the latest update of this plugin, or null if there is none
     */
    public String getUpdateVersion() {
        return this.updateVersion;
    }

    /**
     * Called when a player joins and notifies ops if an update is available
     *
     * @param event The join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.updateVersion == null || !player.isOp())
            return;

        String website = this.plugin.getDescription().getWebsite();
        String updateMessage = "&eAn update for <g:#8A2387:#E94057:#F27121>" +
                this.plugin.getName() + " &e(&bv%new%&e) is available! You are running &bv%current%&e." +
                (website != null ? " " + website : "");

        StringPlaceholders placeholders = StringPlaceholders.builder("new", this.updateVersion)
                .addPlaceholder("current", this.plugin.getDescription().getVersion())
                .build();

        Utils.sendMessage(player, updateMessage, placeholders);
    }


}
