package xyz.akiradev.pluginutils.config;

import org.bukkit.plugin.Plugin;
import xyz.akiradev.pluginutils.PluginUtils;
import xyz.akiradev.pluginutils.manager.AbstractConfigurationManager;

public class SingularPluginSetting implements PluginSetting{

    private final PluginUtils plugin;
    private final String key;
    private final Object defaultValue;
    private final String[] comments;
    private Object value = null;

    public SingularPluginSetting(PluginUtils plugin, String key, Object defaultValue, String... comments) {
        this.plugin = plugin;
        this.key = key;
        this.defaultValue = defaultValue;
        this.comments = comments != null ? comments : new String[0];
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public String[] getComments() {
        return this.comments;
    }

    @Override
    public Object getCachedValue() {
        return this.value;
    }

    @Override
    public void setCachedValue(Object value) {
        this.value = value;
    }

    @Override
    public CommentedFileConfiguration getBaseConfig() {
        return this.plugin.getManager(AbstractConfigurationManager.class).getConfig();
    }


}
