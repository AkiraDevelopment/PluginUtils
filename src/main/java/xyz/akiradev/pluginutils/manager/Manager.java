package xyz.akiradev.pluginutils.manager;

import org.bukkit.plugin.Plugin;

public abstract class Manager {

    protected final Plugin plugin;

    public Manager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads the Manager's settings
     */
    public abstract void reload();

    /**
     * Cleans up the Manager's resources
     */
    public abstract void disable();

}
