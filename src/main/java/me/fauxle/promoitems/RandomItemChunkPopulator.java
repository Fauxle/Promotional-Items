package me.fauxle.promoitems;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.*;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class RandomItemChunkPopulator implements Listener {

    private final Logger log;
    private final double itemDropChance;

    @EventHandler
    public void onPopulate(ChunkPopulateEvent event) {
        Random random = ThreadLocalRandom.current();
        // The item drop is a random chance (default 30% chance)
        if (random.nextDouble() > itemDropChance) return;

        ChunkSnapshot chunkSnapshot = event.getChunk().getChunkSnapshot(true, false, false);
        int offsetX = random.nextInt(15);
        int offsetZ = random.nextInt(15);
        int y = chunkSnapshot.getHighestBlockYAt(offsetX, offsetZ);
        Location itemSpawnLocation = new Location(
                event.getWorld(),
                (chunkSnapshot.getX() << 4) + offsetX + 0.5,
                y + 1,
                (chunkSnapshot.getZ() << 4) + offsetZ + 0.5
        );

        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {

            // TODO Customize display name and lore

            // The spawned item should have:
            // colorful custom display name
            meta.setDisplayName(ChatColor.GOLD + "Lucky Diamond");

            // multi-line lore
            meta.setLore(Arrays.asList(
                    ChatColor.AQUA + "Lucky item drop!",
                    ChatColor.GREEN + "On " + new Date()
            ));

            // glowing without an enchantment showing
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        } else {
            log.warning("Item meta not found for material: " + item.getType());
        }

        // All items spawned should de-spawn after 60 seconds.
        Item entity = spawnItemDropWithExpiration(itemSpawnLocation, item, Duration.ofSeconds(60));

        // Using Log Level Info, log the world name, coordinates and item type spawned.
        log.info("Random item (" + entity.getItemStack().getType() + ") spawned at " + entity.getLocation());
    }

    private Item spawnItemDropWithExpiration(@NonNull Location location, @NonNull ItemStack itemStack, @NonNull Duration duration) {
        Objects.requireNonNull(location.getWorld(), "World is missing in location");
        Item entity = location.getWorld().dropItem(location, itemStack);
        Integer despawnRate = getItemDespawnRate(location.getWorld());
        if (despawnRate == null) {
            log.severe("Cannot determine item de-spawn rate from spigot yml config. Expiration duration will not be applied.");
            return entity;
        }
        int durationTicks = 20 * (int) duration.getSeconds();
        if (durationTicks > despawnRate) {
            log.warning("Unsupported operation, not able to apply duration beyond " + despawnRate + " ticks");
            return entity;
        }
        int ticksExpiresAt = despawnRate;
        entity.setTicksLived(ticksExpiresAt - durationTicks);
        return entity;
    }

    private Integer getItemDespawnRate(World world) {
        // TODO Caching or restructure needed
        YamlConfiguration spigotYmlConfig = Bukkit.spigot().getConfig();
        String key = "world-settings." + world.getName() + ".item-despawn-rate";
        if (!spigotYmlConfig.contains(key))
            key = "world-settings.default.item-despawn-rate";
        int rate = spigotYmlConfig.getInt(key, -1);
        return rate < 0 ? null : rate;
    }

}
