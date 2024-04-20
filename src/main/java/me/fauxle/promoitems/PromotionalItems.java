package me.fauxle.promoitems;

import org.bukkit.plugin.java.JavaPlugin;

public class PromotionalItems extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(
                new RandomItemChunkPopulator(getLogger(), getConfig().getDouble("item-drop-chance", 0.3)),
                this
        );
    }

}
