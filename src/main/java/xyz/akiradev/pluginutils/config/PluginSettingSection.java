package xyz.akiradev.pluginutils.config;

import java.util.Arrays;
import java.util.List;

public class PluginSettingSection {

    private final List<PluginSettingValue> values;

    public PluginSettingSection(PluginSettingValue... values) {
        this.values = Arrays.asList(values);
    }

    public List<PluginSettingValue> getValues() {
        return this.values;
    }

}
