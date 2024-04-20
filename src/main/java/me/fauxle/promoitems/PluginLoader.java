package me.fauxle.promoitems;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginLoader extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            PromotionalItems plugin = new PromotionalItems(this, getConfig());
            getServer().getPluginManager().registerEvents(plugin, this);
        } catch (InvalidConfigurationException e) {
            // throwing a runtime exception during the onEnable() method will cause the plugin to be disabled
            // by spigot. Since the config is invalid, decided that the plugin should be disabled instead of
            // secretly modifying/resetting the item drop chance percentage
            throw new RuntimeException(e);
        }
    }

}
