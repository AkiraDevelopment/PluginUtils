package xyz.akiradev.pluginutils;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.akiradev.pluginutils.manager.AbstractConfigurationManager;
import xyz.akiradev.pluginutils.manager.AbstractLocaleManager;
import xyz.akiradev.pluginutils.manager.Manager;
import xyz.akiradev.pluginutils.manager.PluginUpdateManager;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class PluginUtils extends JavaPlugin {

    /**
     * The plugin ID on Spigot
     */
    private final int spigotId;

    /**
     * The plugin ID on bStats
     */
    private final int bStatsId;

    /**
     * The classes that extend the abstract managers
     */
    private final Class<? extends AbstractConfigurationManager> configurationManagerClass;
    private final Class<? extends AbstractLocaleManager> localeManagerClass;

    /**
     * The plugin managers
     */
    private final Map<Class<? extends Manager>, Manager> managers;

    public PluginUtils(int spigotId,
                       int bStatsId,
                       Class<? extends AbstractConfigurationManager> configurationManagerClass,
                       Class<? extends AbstractLocaleManager> localeManagerClass) {

        if (configurationManagerClass != null && Modifier.isAbstract(configurationManagerClass.getModifiers()))
            throw new IllegalArgumentException("configurationManagerClass cannot be abstract");
        if (localeManagerClass != null && Modifier.isAbstract(localeManagerClass.getModifiers()))
            throw new IllegalArgumentException("localeManagerClass cannot be abstract");

        this.spigotId = spigotId;
        this.bStatsId = bStatsId;
        this.configurationManagerClass = configurationManagerClass;
        this.localeManagerClass = localeManagerClass;
        this.managers = new LinkedHashMap<>();
    }

    @Override
    public void onEnable() {
        // bStats Metrics
        if (this.bStatsId != -1) {
            Metrics metrics = new Metrics(this, this.bStatsId);
            this.addCustomMetricsCharts(metrics);
        }

        // Load managers
        this.reload();

        // Run the plugin's enable code
        this.enable();

    }

    @Override
    public void onDisable() {
        this.disable();
    }

    /**
     * Called during {@link JavaPlugin#onEnable}
     */
    protected abstract void enable();

    /**
     * Called during {@link JavaPlugin#onDisable}
     */
    protected abstract void disable();

    /**
     * Registers any custom bStats Metrics charts for the plugin
     *
     * @param metrics The Metrics instance
     */
    protected void addCustomMetricsCharts(Metrics metrics) {
        // Must be overridden for any functionality.
    }

    /**
     * @return the order in which Managers should be loaded
     */
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return null;
    }

    /**
     * Reloads the plugin's managers
     */
    public void reload() {
        this.disableManagers();
        this.managers.values().forEach(Manager::reload);

        List<Class<? extends Manager>> managerLoadPriority = new ArrayList<>();

        if (this.hasConfigurationManager())
            managerLoadPriority.add(this.configurationManagerClass);

        if (this.hasLocaleManager())
            managerLoadPriority.add(this.localeManagerClass);

        managerLoadPriority.addAll(this.getManagerLoadPriority());

        if (this.spigotId != -1)
            managerLoadPriority.add(PluginUpdateManager.class);

        managerLoadPriority.forEach(this::getManager);
    }

    /**
     * Runs {@link Manager#disable} on all managers in the reverse order that they were loaded
     */
    private void disableManagers() {
        List<Manager> managers = new ArrayList<>(this.managers.values());
        Collections.reverse(managers);
        managers.forEach(Manager::disable);
    }

    /**
     * Gets a manager instance
     *
     * @param managerClass The class of the manager to get
     * @param <T> extends Manager
     * @return A new or existing instance of the given manager class
     */
    @SuppressWarnings("unchecked")
    public final <T extends Manager> T getManager(Class<T> managerClass) {
        if (this.managers.containsKey(managerClass))
            return (T) this.managers.get(managerClass);

        // Get the actual class if the abstract one is requested
        if (this.hasConfigurationManager() && managerClass == AbstractConfigurationManager.class) {
            return this.getManager((Class<T>) this.configurationManagerClass);
        } else if (this.hasLocaleManager() && managerClass == AbstractLocaleManager.class) {
            return this.getManager((Class<T>) this.localeManagerClass);
        }

        try {
            T manager = managerClass.getConstructor(PluginUtils.class).newInstance(this);
            this.managers.put(managerClass, manager);
            manager.reload();
            return manager;
        } catch (Exception ex) {
            throw new ManagerNotFoundException(managerClass, ex);
        }
    }

    /**
     * @return the ID of the plugin on Spigot, or -1 if not tracked
     */
    public int getSpigotId() {
        return this.spigotId;
    }

    /**
     * @return the ID of this plugin on bStats, or -1 if not tracked
     */
    public int getBStatsId() {
        return this.bStatsId;
    }

    /**
     * @return the data folder for RoseGarden
     */
    public File getPluginUtilsDataFolder() {
        File configDir = new File(this.getDataFolder().getParentFile(), "RoseGarden");
        if (!configDir.exists())
            configDir.mkdirs();
        return configDir;
    }

    public boolean hasConfigurationManager() {
        return this.configurationManagerClass != null;
    }

    public boolean hasLocaleManager() {
        return this.localeManagerClass != null;
    }

    /**
     * An exception thrown when a Manager fails to load
     */
    private static class ManagerNotFoundException extends RuntimeException {

        public ManagerNotFoundException(Class<? extends Manager> managerClass, Throwable cause) {
            super("Failed to load " + managerClass.getSimpleName(), cause);
        }

    }

}
