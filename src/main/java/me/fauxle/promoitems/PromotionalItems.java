package me.fauxle.promoitems;

import lombok.NonNull;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class PromotionalItems implements Listener {

    private final Logger log;
    private final double itemDropChance;

    public PromotionalItems(PluginLoader pluginLoader, FileConfiguration config) throws InvalidConfigurationException {
        this.log = pluginLoader.getLogger();
        // There should be a config.yml setting to allow adjustment of the 30% chance
        this.itemDropChance = config.getDouble("item-drop-chance", 0.3);
        if (itemDropChance < 0 || itemDropChance > 1)
            throw new InvalidConfigurationException("item-drop-chance must be within the bound [0, 1]");
    }

    // A populated chunk usually occurs when the chunk is loaded for the first time
    // But if it is for when it is strictly a new chunk being loaded, use
    // ChunkLoadEvent and check event.isNewChunk() condition
    @EventHandler
    public void onPopulateEvent(ChunkPopulateEvent event) {
        Random random = ThreadLocalRandom.current();
        // The item drop is a random chance (default 30% chance)
        if (random.nextDouble() > itemDropChance) return;

        ChunkSnapshot chunkSnapshot = event.getChunk().getChunkSnapshot(true, false, false);
        int offsetX = random.nextInt(15);
        int offsetZ = random.nextInt(15);
        int y = chunkSnapshot.getHighestBlockYAt(offsetX, offsetZ);

        // a random location within the chunk (but on the surface)
        Location itemSpawnLocation = new Location(
                event.getWorld(),
                (chunkSnapshot.getX() << 4) + offsetX + 0.5,
                y + 1,
                (chunkSnapshot.getZ() << 4) + offsetZ + 0.5
        );

        // Spawn an item drop of a diamond
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // The spawned item should have:
            // colorful custom display name
            meta.setDisplayName(colorfulText("Lucky Diamond"));

            // multi-line lore
            meta.setLore(Arrays.asList(
                    ChatColor.AQUA + "Lucky item drop! Yippee!!!",
                    ChatColor.GRAY.toString() + ChatColor.ITALIC + new Date()
            ));

            // glowing without an enchantment showing
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        } else {
            log.warning("Item meta not found for material type: " + item.getType());
        }

        // All items spawned should de-spawn after 60 seconds.
        Item entity = spawnItemDropWithExpiration(itemSpawnLocation, item, Duration.ofSeconds(60));

        // Using Log Level Info, log the world name, coordinates and item type spawned.
        log.info("Random item (" + entity.getItemStack().getType() + ") spawned at " + entity.getLocation());
    }

    /**
     * Spawns a dropped item entity with a custom time until expiration (de-spawns)
     *
     * @param location  The {@link Location} to spawn the item entity at
     * @param itemStack The {@link ItemStack} the dropped item entity will be
     * @param duration  The duration of how long the item entity should last before de-spawning naturally
     * @return The spawned item entity
     * @throws NullPointerException          If the world is null in the given location
     * @throws UnsupportedOperationException If the duration is longer than the world's item-despawn-rate defined in
     *                                       the <a href="https://www.spigotmc.org/wiki/spigot-configuration/">spigot config yml</a>
     */
    public @NonNull Item spawnItemDropWithExpiration(@NonNull Location location, @NonNull ItemStack itemStack, @NonNull Duration duration) {
        Objects.requireNonNull(location.getWorld(), "World is missing in location");
        Item entity = location.getWorld().dropItem(location, itemStack);
        int despawnRate = getItemDespawnRate(location.getWorld());
        int durationTicks = 20 * (int) duration.getSeconds();
        if (durationTicks > despawnRate) {
            // It is possible through the use of a scheduler or similar to have a longer duration than the
            // item de-spawn rate, but since requirements only need it to be 60 seconds this can be worked
            // on in the future if it is needed
            throw new UnsupportedOperationException("Not able to apply duration beyond " + despawnRate + " ticks");
        }
        entity.setTicksLived(despawnRate - durationTicks);
        return entity;
    }

    /**
     * Determines the world's configured item-despawn-rate
     *
     * @param world The world to check
     * @return The item-despawn-rate int value
     */
    public int getItemDespawnRate(World world) {
        // default item de-spawn rate is 5 minutes
        if (world == null) return 6000;
        // This is also possible with nms ((CraftWorld) world).spigotConfig.itemDespawnRate
        // however we will be sticking with using the spigot api for now
        // Not using caching/async here since the spigot yaml configuration
        // should already be loaded in memory when calling getConfig()
        YamlConfiguration spigotYmlConfig = Bukkit.spigot().getConfig();
        String key = "world-settings." + world.getName() + ".item-despawn-rate";
        // check if there is a custom rate for the world. If not, use the default
        if (!spigotYmlConfig.contains(key))
            key = "world-settings.default.item-despawn-rate";
        return spigotYmlConfig.getInt(key, 6000);
    }

    /**
     * Makes any string a colorful one by prepending a random {@link ChatColor} to each character
     *
     * @param str The input string
     * @return A colorful version of the input string. If the input was null, an empty string is returned instead.
     */
    public @NonNull String colorfulText(String str) {
        // handle null string inputs
        if (str == null) return "";
        List<ChatColor> chatColors = Arrays.stream(ChatColor.values()).filter(ChatColor::isColor).toList();
        Random random = ThreadLocalRandom.current();
        StringBuilder result = new StringBuilder();
        for (char c : str.toCharArray()) {
            result.append(chatColors.get(random.nextInt(chatColors.size())));
            result.append(c);
        }
        return result.toString();
    }

}
