package me.fauxle.promoitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PromotionalItemsTest {

    private Server mockServer;
    private PromotionalItems plugin;

    @BeforeEach
    void setup() throws InvalidConfigurationException {
        PluginLoader loader = mock(PluginLoader.class);
        FileConfiguration configuration = new YamlConfiguration();
        plugin = new PromotionalItems(loader, configuration);
        mockServer = mock(Server.class);
        when(mockServer.getLogger()).thenReturn(Logger.getLogger("ServerMock"));
        Bukkit.setServer(mockServer);
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

    }

    @Test
    void test_onPopulateEvent() {

    }

    @Test
    void test_onEnable() {

    }

}
