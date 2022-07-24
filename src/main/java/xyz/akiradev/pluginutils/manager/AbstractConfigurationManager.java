/**
 * CREDIT https://github.com/Rosewood-Development/RoseGarden
 */

package xyz.akiradev.pluginutils.manager;

import xyz.akiradev.pluginutils.PluginUtils;
import xyz.akiradev.pluginutils.config.CommentedFileConfiguration;
import xyz.akiradev.pluginutils.config.PluginSetting;
import xyz.akiradev.pluginutils.config.SingularPluginSetting;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractConfigurationManager extends Manager {

    protected final PluginUtils plugin;

    private static final String[] FOOTER = new String[] {
            "That's everything! You reached the end of the configuration.",
            "Enjoy the plugin!"
    };

    private final Class<? extends PluginSetting> settingEnum;
    private CommentedFileConfiguration configuration;
    private Map<String, PluginSetting> cachedValues;

    public AbstractConfigurationManager(PluginUtils plugin, Class<? extends PluginSetting> settingEnum) {
        super(plugin);
        this.plugin = plugin;

        if (!settingEnum.isEnum())
            throw new IllegalArgumentException("settingEnum class must be of type Enum");

        this.settingEnum = settingEnum;
    }

    @Override
    public final void reload() {
        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        boolean setHeaderFooter = !configFile.exists();
        boolean changed = setHeaderFooter;

        this.configuration = CommentedFileConfiguration.loadConfiguration(configFile);

        if (setHeaderFooter)
            this.configuration.addComments(this.getHeader());

        for (PluginSetting setting : this.getSettings().values()) {
            setting.reset();
            changed |= setting.setIfNotExists(this.configuration);
        }

        if (setHeaderFooter)
            this.configuration.addComments(FOOTER);

        if (changed)
            this.configuration.save();
    }

    @Override
    public final void disable() {
        for (PluginSetting setting : this.getSettings().values())
            setting.reset();
    }

    /**
     * @return the header to place at the top of the configuration file
     */
    protected abstract String[] getHeader();

    /**
     * @return the config.yml as a CommentedFileConfiguration
     */
    public final CommentedFileConfiguration getConfig() {
        return this.configuration;
    }

    /**
     * @return the values of the setting enum
     */
    public Map<String, PluginSetting> getSettings() {
        if (this.cachedValues == null) {
            try {
                PluginSetting[] roseSettings = (PluginSetting[]) this.settingEnum.getDeclaredMethod("values").invoke(null);
                this.cachedValues = new LinkedHashMap<>();
                for (PluginSetting roseSetting : roseSettings)
                    this.cachedValues.put(roseSetting.getKey(), roseSetting);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
                this.cachedValues = Collections.emptyMap();
            }

            this.injectAdditionalSettings();
        }

        return this.cachedValues;
    }

    /**
     * Injects additional settings into the config
     */
    private void injectAdditionalSettings() {
        Map<String, PluginSetting> values = this.cachedValues;
        this.cachedValues = new LinkedHashMap<>();

        if (this.plugin.hasLocaleManager())
            this.cachedValues.put("locale", new SingularPluginSetting(this.plugin, "locale", "en_US", "The locale to use in the /locale folder"));

        this.cachedValues.putAll(values);
    }

}
