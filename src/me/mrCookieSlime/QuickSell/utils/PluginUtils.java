package me.mrCookieSlime.QuickSell.utils;

import io.github.thebusybiscuit.cscorelib2.config.Config;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class PluginUtils {

    private Plugin plugin;
    private int id;
    private Config cfg;
    private Localization local;

    /**
     * Creates a new PluginUtils Instance for
     * the specified Plugin
     *
     * @param  plugin The Plugin for which this PluginUtils Instance is made for
     */
    public PluginUtils(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new PluginUtils Instance for
     * the specified Plugin
     *
     * @param  plugin The Plugin for which this PluginUtils Instance is made for
     */
    public PluginUtils(Plugin plugin, int id) {
        this(plugin);
        this.id = id;
    }

    /**
     * Returns the specified ID from Curse
     *
     * @return      Plugin ID
     */
    public int getPluginID() {
        return this.id;
    }

    /**
     * Automatically sets up the messages.yml for you
     */
    public void setupLocalization() {
        local = new Localization(plugin);
    }

    /**
     * Automatically sets up the config.yml for you
     */
    public void setupConfig() {
        FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        config.options().header("\nName: " + plugin.getName() + "\nAuthor: " + plugin.getDescription().getAuthors().get(0) + "\n\nDo not modify the Config while the Server is running\notherwise bad things might happen!\n\nThis Plugin also requires CS-CoreLib to run!\nIf you don't have it installed already, its going to be\nautomatically installed for you\n\nThis Plugin utilises an Auto-Updater. If you want to turn that off,\nsimply set options -> auto-update to false");
        plugin.saveConfig();

        cfg = new Config(plugin);
    }

    /**
     * Returns the previously setup Config
     *
     * @return      Config of this Plugin
     */
    public Config getConfig() {
        return cfg;
    }

    /**
     * Returns the previously setup Localization
     *
     * @return      Localization for this Plugin
     */
    public Localization getLocalization() {
        return local;
    }
}
