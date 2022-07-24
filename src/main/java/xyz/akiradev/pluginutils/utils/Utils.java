package xyz.akiradev.pluginutils.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import xyz.akiradev.pluginutils.config.CommentedConfigurationSection;
import xyz.akiradev.pluginutils.config.CommentedFileConfiguration;
import xyz.akiradev.pluginutils.config.PluginSettingSection;
import xyz.akiradev.pluginutils.config.PluginSettingValue;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
public class Utils {

    private static Logger logger;

    public static final String GRADIENT = "<g:#8A2387:#E94057:#F27121>";
    public static final String PREFIX = "&7[" + GRADIENT + "PluginUtils&7] ";

    /**
     * @return the Logger for RoseGarden
     */
    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger("RoseGarden", null) { };
            logger.setParent(Bukkit.getLogger());
            logger.setLevel(Level.ALL);
        }
        return logger;
    }

    /**
     * Checks if a String contains any values for a yaml value that need to be quoted
     *
     * @param string The string to check
     * @return true if any special characters need to be escaped, otherwise false
     */
    public static boolean containsConfigSpecialCharacters(String string) {
        for (char c : string.toCharArray()) {
            // Range taken from SnakeYAML's Emitter.java
            if (!(c == '\n' || (0x20 <= c && c <= 0x7E)) &&
                    (c == 0x85 || (c >= 0xA0 && c <= 0xD7FF)
                            || (c >= 0xE000 && c <= 0xFFFD)
                            || (c >= 0x10000 && c <= 0x10FFFF))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is an update available
     *
     * @param latest The latest version of the plugin from Spigot
     * @param current The currently installed version of the plugin
     * @return true if available, otherwise false
     */
    public static boolean isUpdateAvailable(String latest, String current) {
        if (latest == null || current == null)
            return false;

        // Break versions into individual numerical pieces separated by periods
        int[] latestSplit = Arrays.stream(latest.replaceAll("[^0-9.]", "").split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray();
        int[] currentSplit = Arrays.stream(current.replaceAll("[^0-9.]", "").split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray();

        // Make sure both arrays are the same length
        if (latestSplit.length > currentSplit.length) {
            currentSplit = Arrays.copyOf(currentSplit, latestSplit.length);
        } else if (currentSplit.length > latestSplit.length) {
            latestSplit = Arrays.copyOf(latestSplit, currentSplit.length);
        }

        // Compare pieces from most significant to least significant
        for (int i = 0; i < latestSplit.length; i++) {
            if (latestSplit[i] > currentSplit[i]) {
                return true;
            } else if (currentSplit[i] > latestSplit[i]) {
                break;
            }
        }

        return false;
    }

    /**
     * Gets an Object as a numerical value
     *
     * @param value The Object to cast
     * @return The Object as a numerical value
     * @throws ClassCastException when the value is not a numerical value
     */
    public static double getNumber(Object value) {
        if (value instanceof Integer) {
            return (int) value;
        } else if (value instanceof Short) {
            return (short) value;
        } else if (value instanceof Byte) {
            return (byte) value;
        } else if (value instanceof Float) {
            return (float) value;
        }

        return (double) value;
    }

    public static void recursivelyWriteRoseSettingValues(CommentedFileConfiguration fileConfiguration, PluginSettingValue settingValue) {
        recursivelyWriteRoseSettingValues(fileConfiguration, fileConfiguration, settingValue);
    }

    private static void recursivelyWriteRoseSettingValues(CommentedFileConfiguration baseConfiguration, CommentedConfigurationSection currentSection, PluginSettingValue settingValue) {
        String key = settingValue.getKey();
        Object defaultValue = settingValue.getDefaultValue();
        String[] comments = settingValue.getComments();

        String keyPath = currentSection.getCurrentPath() == null ? key : currentSection.getCurrentPath() + "." + key;

        if (defaultValue instanceof PluginSettingSection) {
            baseConfiguration.addPathedComments(keyPath, comments);
            currentSection = currentSection.createSection(key);

            PluginSettingSection settingSection = (PluginSettingSection) defaultValue;
            for (PluginSettingValue value : settingSection.getValues())
                recursivelyWriteRoseSettingValues(baseConfiguration, currentSection, value);
        } else {
            baseConfiguration.set(keyPath, defaultValue, comments);
        }
    }

    /**
     * Sends a RoseGarden message to a recipient
     */
    public static void sendMessage(CommandSender recipient, String message) {
        recipient.sendMessage(HexUtils.colorify(PREFIX + message));
    }

    /**
     * Sends a RoseGarden message to a recipient
     */
    public static void sendMessage(CommandSender recipient, String message, StringPlaceholders placeholders) {
        sendMessage(recipient, placeholders.apply(message));
    }

}