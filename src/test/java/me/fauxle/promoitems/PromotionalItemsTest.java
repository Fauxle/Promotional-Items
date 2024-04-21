package me.fauxle.promoitems;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class PromotionalItemsTest {

    private Server mockServer;
    private PromotionalItems plugin;


    @BeforeEach
    void setup() throws Exception {
        PluginLoader loader = mock(PluginLoader.class);
        FileConfiguration configuration = new YamlConfiguration();
        plugin = new PromotionalItems(loader, configuration);
        mockServer = mock(Server.class);
        when(mockServer.getLogger()).thenReturn(Logger.getLogger("ServerMock"));
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, mockServer);
    }

    @Test
    void test_colorfulText() {
        assertEquals("", plugin.colorfulText(null), "Should return blank string on null input");
        char[] result = plugin.colorfulText("Test").toCharArray();
        Set<Character> validColorChars = Arrays.stream(ChatColor.values()).filter(ChatColor::isColor).map(ChatColor::getChar).collect(Collectors.toSet());
        for (int i = 0; i < result.length; i += 3) {
            assertEquals(ChatColor.COLOR_CHAR, result[i], "String is not correctly formatted");
            assertTrue(validColorChars.contains(result[i + 1]), "Not a valid color char");
            assertTrue(Character.isAlphabetic(result[i + 2]), "Expecting a string character");
        }
    }

    @Test
    void test_getItemDespawnRate() {
        assertEquals(6000, plugin.getItemDespawnRate(null));
        World worldMock = mock(World.class);
        when(worldMock.getName()).thenReturn("test");
        YamlConfiguration spigotConfig = new YamlConfiguration();
        spigotConfig.set("world-settings." + worldMock.getName() + ".item-despawn-rate", 4000);
        Server.Spigot mockSpigot = mock(Server.Spigot.class);
        when(mockSpigot.getConfig()).thenReturn(spigotConfig);
        when(mockServer.spigot()).thenReturn(mockSpigot);
        assertEquals(4000, plugin.getItemDespawnRate(worldMock));
    }

    @Test
    void test_spawnItemDropWithExpiration() {
        assertThrows(NullPointerException.class, () -> plugin.spawnItemDropWithExpiration(
                new Location(null, 0, 0, 0), new ItemStack(Material.DIAMOND),
                Duration.ofSeconds(60)), "Null world should throw NPE"
        );
        World worldMock = mock(World.class);
        when(worldMock.getName()).thenReturn("test");
        YamlConfiguration spigotConfig = new YamlConfiguration();
        spigotConfig.set("world-settings." + worldMock.getName() + ".item-despawn-rate", 5000);
        Server.Spigot mockSpigot = mock(Server.Spigot.class);
        when(mockSpigot.getConfig()).thenReturn(spigotConfig);
        when(mockServer.spigot()).thenReturn(mockSpigot);
        Item mockItem = mock(Item.class);
        AtomicInteger ticksLived = new AtomicInteger(0);
        doAnswer((Answer<Void>) invocationOnMock -> {
            ticksLived.set(invocationOnMock.getArgument(0));
            return null;
        }).when(mockItem).setTicksLived(anyInt());
        when(mockItem.getTicksLived()).thenAnswer((Answer<Integer>) invocationOnMock -> ticksLived.get());
        when(worldMock.dropItem(any(), any())).thenReturn(mockItem);
        plugin.spawnItemDropWithExpiration(new Location(worldMock, 0, 0, 0), new ItemStack(Material.DIAMOND), Duration.ofSeconds(60));
        assertEquals(3800, mockItem.getTicksLived(), "Ticks lived is not being calculated correctly");
    }

}
