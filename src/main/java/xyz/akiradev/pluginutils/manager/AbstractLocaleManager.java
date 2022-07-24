package xyz.akiradev.pluginutils.manager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.akiradev.pluginutils.PluginUtils;
import xyz.akiradev.pluginutils.config.CommentedFileConfiguration;
import xyz.akiradev.pluginutils.hooks.PAPIHook;
import xyz.akiradev.pluginutils.locale.Locale;
import xyz.akiradev.pluginutils.utils.HexUtils;
import xyz.akiradev.pluginutils.utils.StringPlaceholders;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractLocaleManager extends Manager {

    protected final PluginUtils plugin;

    protected CommentedFileConfiguration locale;

    public AbstractLocaleManager(PluginUtils plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Creates a .lang file if one doesn't exist
     * Cross merges values between files into the .lang file, the .lang values take priority
     *
     * @param locale The Locale to register
     */
    private void registerLocale(Locale locale) {
        File file = new File(this.plugin.getDataFolder() + "/locale", locale.getLocaleName() + ".lang");
        boolean newFile = false;
        if (!file.exists()) {
            try {
                file.createNewFile();
                newFile = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean changed = false;
        CommentedFileConfiguration configuration = CommentedFileConfiguration.loadConfiguration(file);
        if (newFile) {
            configuration.addComments(locale.getLocaleName() + " translation by " + locale.getTranslatorName());
            Map<String, Object> defaultLocaleStrings = locale.getDefaultLocaleValues();
            for (String key : defaultLocaleStrings.keySet()) {
                Object value = defaultLocaleStrings.get(key);
                if (key.startsWith("#")) {
                    configuration.addComments((String) value);
                } else {
                    configuration.set(key, value);
                }
            }
            changed = true;
        } else {
            Map<String, Object> defaultLocaleStrings = locale.getDefaultLocaleValues();
            for (String key : defaultLocaleStrings.keySet()) {
                if (key.startsWith("#"))
                    continue;

                Object value = defaultLocaleStrings.get(key);
                if (!configuration.contains(key)) {
                    configuration.set(key, value);
                    changed = true;
                }
            }
        }

        if (changed)
            configuration.save();
    }

    @Override
    public final void reload() {
        File localeDirectory = new File(this.plugin.getDataFolder(), "locale");
        if (!localeDirectory.exists())
            localeDirectory.mkdirs();

        this.getLocales().forEach(this::registerLocale);

        String locale;
        if (this.plugin.hasConfigurationManager()) {
            locale = this.plugin.getManager(AbstractConfigurationManager.class).getSettings().get("locale").getString();
        } else {
            locale = "en_US";
        }

        File targetLocaleFile = new File(this.plugin.getDataFolder() + "/locale", locale + ".lang");
        if (!targetLocaleFile.exists()) {
            targetLocaleFile = new File(this.plugin.getDataFolder() + "/locale", "en_US.lang");
            this.plugin.getLogger().severe("File " + targetLocaleFile.getName() + " does not exist. Defaulting to en_US.lang");
        }

        this.locale = CommentedFileConfiguration.loadConfiguration(targetLocaleFile);
    }

    @Override
    public final void disable() {

    }

    public abstract List<Locale> getLocales();

    /**
     * Gets a locale message
     *
     * @param messageKey The key of the message to get
     * @return The locale message
     */
    public final String getLocaleMessage(String messageKey) {
        return this.getLocaleMessage(messageKey, StringPlaceholders.empty());
    }

    /**
     * Gets a locale message with the given placeholders applied
     *
     * @param messageKey The key of the message to get
     * @param stringPlaceholders The placeholders to apply
     * @return The locale message with the given placeholders applied
     */
    public final String getLocaleMessage(String messageKey, StringPlaceholders stringPlaceholders) {
        String message = this.locale.getString(messageKey);
        if (message == null)
            return ChatColor.RED + "Missing message in locale file: " + messageKey;
        return HexUtils.colorify(stringPlaceholders.apply(message));
    }

    /**
     * Sends a message to a CommandSender with the prefix with placeholders applied
     *
     * @param sender The CommandSender to send to
     * @param messageKey The message key of the Locale to send
     * @param stringPlaceholders The placeholders to apply
     */
    public final void sendMessage(CommandSender sender, String messageKey, StringPlaceholders stringPlaceholders) {
        String prefix = this.getLocaleMessage("prefix");
        String message = this.getLocaleMessage(messageKey, stringPlaceholders);
        if (message.isEmpty())
            return;
        this.sendParsedMessage(sender, prefix + message);
    }

    /**
     * Sends a message to a CommandSender with the prefix
     *
     * @param sender The CommandSender to send to
     * @param messageKey The message key of the Locale to send
     */
    public final void sendMessage(CommandSender sender, String messageKey) {
        this.sendMessage(sender, messageKey, StringPlaceholders.empty());
    }

    /**
     * Sends a message to a CommandSender with placeholders applied
     *
     * @param sender The CommandSender to send to
     * @param messageKey The message key of the Locale to send
     * @param stringPlaceholders The placeholders to apply
     */
    public final void sendSimpleMessage(CommandSender sender, String messageKey, StringPlaceholders stringPlaceholders) {
        this.sendParsedMessage(sender, this.getLocaleMessage(messageKey, stringPlaceholders));
    }

    /**
     * Sends a message to a CommandSender, falling back to the default command messages if none found
     *
     * @param sender The CommandSender to send to
     * @param messageKey The message key of the Locale to send
     */
    public final void sendSimpleMessage(CommandSender sender, String messageKey) {
        this.sendSimpleMessage(sender, messageKey, StringPlaceholders.empty());
    }

    /**
     * Sends a custom message to a CommandSender
     *
     * @param sender The CommandSender to send to
     * @param message The message to send
     */
    public final void sendCustomMessage(CommandSender sender, String message) {
        this.sendParsedMessage(sender, message);
    }

    /**
     * Replaces PlaceholderAPI placeholders if PlaceholderAPI is enabled
     *
     * @param sender The potential Player to replace with
     * @param message The message
     * @return A placeholder-replaced message
     */
    protected final String parsePlaceholders(CommandSender sender, String message) {
        if (sender instanceof Player)
            return PAPIHook.applyPlaceholders((Player) sender, message);
        return message;
    }

    /**
     * Sends a message with placeholders and colors parsed to a CommandSender
     *
     * @param sender The sender to send the message to
     * @param message The message
     */
    protected final void sendParsedMessage(CommandSender sender, String message) {
        if (!message.isEmpty())
            HexUtils.sendMessage(sender, this.parsePlaceholders(sender, message));
    }

}
