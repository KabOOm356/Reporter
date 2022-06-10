package net.KabOOm356.Util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import test.test.MockitoTest;

public class BukkitUtilTest extends MockitoTest {
  private static MockedStatic<Bukkit> bukkit;

  @BeforeClass
  public static void setupMocks() {
    bukkit = mockStatic(Bukkit.class);
  }

  @AfterClass
  public static void cleanupMocks() {
    bukkit.close();
  }

  @After
  public void resetMocks() {
    bukkit.reset();
  }

  @Test
  public void testIsUsernameValid() {
    String username = "*";
    assertFalse(BukkitUtil.isUsernameValid(username));
    username = "**";
    assertFalse(BukkitUtil.isUsernameValid(username));

    username = "!";
    assertFalse(BukkitUtil.isUsernameValid(username));
    username = "!!";
    assertFalse(BukkitUtil.isUsernameValid(username));

    username = "a";
    assertFalse(BukkitUtil.isUsernameValid(username));
    username = "A";
    assertFalse(BukkitUtil.isUsernameValid(username));
    username = "_";
    assertFalse(BukkitUtil.isUsernameValid(username));
    username = "0";
    assertFalse(BukkitUtil.isUsernameValid(username));

    username = "ab";
    assertTrue(BukkitUtil.isUsernameValid(username));
    username = "AB";
    assertTrue(BukkitUtil.isUsernameValid(username));
    username = "Ab";
    assertTrue(BukkitUtil.isUsernameValid(username));
    username = "ab_";
    assertTrue(BukkitUtil.isUsernameValid(username));
    username = "ab0";
    assertTrue(BukkitUtil.isUsernameValid(username));
    username = "aB_";
    assertTrue(BukkitUtil.isUsernameValid(username));
    username = "aB0";
    assertTrue(BukkitUtil.isUsernameValid(username));
    username = "abcdefghijklmnopqrst0123456789_";
    assertTrue(BukkitUtil.isUsernameValid(username));

    // Username is too long.
    username = "abcdefghijklmnopqrstuvwxyz0123456789_";
    assertFalse(BukkitUtil.isUsernameValid(username));
  }

  @Test
  public void testFormatPlayerNameCommandSender() {
    final String playerName = "TestName";
    final CommandSender sender = mock(CommandSender.class);
    when(sender.getName()).thenReturn(playerName);

    assertEquals(playerName, BukkitUtil.formatPlayerName(sender));
  }

  @Test
  public void testFormatPlayerNameCommandSenderPlayer() {
    final String playerName = "TestName";
    final OfflinePlayer player =
        mock(OfflinePlayer.class, withSettings().extraInterfaces(CommandSender.class));
    when(player.getName()).thenReturn(playerName);

    assertEquals(playerName, BukkitUtil.formatPlayerName((CommandSender) player));
  }

  @Test
  public void testFormatPlayerNameCommandSenderDisplayRealName() {
    final String playerName = "TestName";
    final CommandSender sender = mock(CommandSender.class);
    when(sender.getName()).thenReturn(playerName);

    assertEquals(playerName, BukkitUtil.formatPlayerName(sender, true));
  }

  @Test
  public void testFormatPlayerNameCommandSenderPlayerDisplayRealName() {
    final String playerName = "TestName";
    final OfflinePlayer player =
        mock(OfflinePlayer.class, withSettings().extraInterfaces(CommandSender.class));
    when(player.getName()).thenReturn(playerName);

    assertEquals(playerName, BukkitUtil.formatPlayerName((CommandSender) player, true));
  }

  @Test
  public void testFormatPlayerNameOfflinePlayer() {
    final String playerName = "TestName";
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.getName()).thenReturn(playerName);

    assertEquals(playerName, BukkitUtil.formatPlayerName(offlinePlayer));
  }

  @Test
  public void testFormatPlayerNameOfflinePlayerOnline() {
    final String playerName = "TestName";

    final Player player = mock(Player.class);
    when(player.getDisplayName()).thenReturn(playerName);
    when(player.getName()).thenReturn(playerName);

    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.isOnline()).thenReturn(true);
    when(offlinePlayer.getPlayer()).thenReturn(player);

    assertEquals(playerName, BukkitUtil.formatPlayerName(offlinePlayer));
  }

  @Test
  public void testFormatPlayerNameOfflinePlayerDisplayRealName() {
    final String playerName = "TestName";
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.getName()).thenReturn(playerName);

    assertEquals(playerName, BukkitUtil.formatPlayerName(offlinePlayer, true));
  }

  @Test
  public void testFormatPlayerNameOfflinePlayerOnlineDisplayRealName() {
    final Player player = mock(Player.class);
    when(player.getDisplayName()).thenReturn("displayName");
    when(player.getName()).thenReturn("playerName");

    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.isOnline()).thenReturn(true);
    when(offlinePlayer.getPlayer()).thenReturn(player);

    assertEquals(
        "displayName " + ChatColor.GOLD + "(playerName)",
        BukkitUtil.formatPlayerName(offlinePlayer, true));
  }

  @Test
  public void testFormatPlayerNamePlayer() {
    final String playerName = "TestName";
    final Player player = mock(Player.class);
    when(player.getDisplayName()).thenReturn(playerName);
    when(player.getName()).thenReturn(playerName);

    assertEquals(playerName, BukkitUtil.formatPlayerName(player));
  }

  @Test
  public void testFormatPlayerNamePlayerDisplayRealName() {
    final Player player = mock(Player.class);
    when(player.getDisplayName()).thenReturn("displayName");
    when(player.getName()).thenReturn("playerName");

    assertEquals(
        "displayName " + ChatColor.GOLD + "(playerName)",
        BukkitUtil.formatPlayerName(player, true));
  }

  @Test
  public void testFormatPlayerNameDisplayNameEqualRealName() {
    final String displayName = "TestName";
    final String realName = "TestName";

    assertEquals(displayName, BukkitUtil.formatPlayerName(displayName, realName));
  }

  @Test
  public void testFormatPlayerNameDisplayNameContainsRealName() {
    final String displayName = "TestName";
    final String realName = "Name";

    assertEquals(displayName, BukkitUtil.formatPlayerName(displayName, realName));
  }

  @Test
  public void testFormatPlayerName() {
    final String displayName = "DisplayName";
    final String realName = "RealName";

    assertEquals(
        "DisplayName " + ChatColor.GOLD + "(RealName)",
        BukkitUtil.formatPlayerName(displayName, realName));
  }

  @Test
  public void testFormatPlayerNameDisplayNameEqualsRealNameForceDisplayName() {
    final String displayName = "TestName";
    final String realName = "TestName";

    assertEquals(
        "TestName " + ChatColor.GOLD + "(TestName)",
        BukkitUtil.formatPlayerName(displayName, realName, true));
  }

  @Test
  public void testFormatPlayerNameDisplayNameContainsRealNameForceDisplayName() {
    final String displayName = "TestName";
    final String realName = "Name";

    assertEquals(
        "TestName " + ChatColor.GOLD + "(Name)",
        BukkitUtil.formatPlayerName(displayName, realName, true));
  }

  @Test
  public void testFormatPlayerNameDisplayNameNoEqualRealNameForceDisplayName() {
    final String displayName = "DisplayName";
    final String realName = "RealName";

    assertEquals(
        "DisplayName " + ChatColor.GOLD + "(RealName)",
        BukkitUtil.formatPlayerName(displayName, realName, true));
  }

  @Test
  public void testFormatPlayerNameDisplayNameEqualRealNameNoForceDisplayName() {
    final String displayName = "DisplayName";
    final String realName = "RealName";

    assertEquals(
        "DisplayName " + ChatColor.GOLD + "(RealName)",
        BukkitUtil.formatPlayerName(displayName, realName, false));
  }

  @Test
  public void testIsPlayerValidOfflinePlayerValid() {
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.getUniqueId()).thenReturn(UUID.randomUUID());

    assertTrue(BukkitUtil.isPlayerValid(offlinePlayer));
  }

  @Test
  public void testIsPlayerValidOfflinePlayerInvalid() {
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.getUniqueId()).thenReturn(BukkitUtil.invalidUserUUID);

    assertFalse(BukkitUtil.isPlayerValid(offlinePlayer));
  }

  @Test
  public void testIsPlayerIdValid() {
    assertTrue(BukkitUtil.isPlayerIdValid(UUID.randomUUID()));
  }

  @Test
  public void testIsPlayerIdValidInvalid() {
    assertFalse(BukkitUtil.isPlayerIdValid(BukkitUtil.invalidUserUUID));
  }

  @Test
  public void testIsPlayer() {
    assertTrue(BukkitUtil.isPlayer(mock(Player.class)));
  }

  @Test
  public void testIsPlayerCommandSender() {
    assertFalse(BukkitUtil.isPlayer(mock(CommandSender.class)));
  }

  @Test
  public void testIsOfflinePlayer() {
    assertTrue(BukkitUtil.isOfflinePlayer(mock(Player.class)));
  }

  @Test
  public void testIsOfflinePlayerCommandSender() {
    assertFalse(BukkitUtil.isOfflinePlayer(mock(CommandSender.class)));
  }

  @Test
  public void testPlayersEqual() {
    final String matchingName = "MatchingTestName";
    final CommandSender commandSender = mock(CommandSender.class);
    final CommandSender commandSender1 = mock(CommandSender.class);

    assertFalse(BukkitUtil.playersEqual(null, null));
    assertFalse(BukkitUtil.playersEqual(commandSender, null));
    assertFalse(BukkitUtil.playersEqual(null, commandSender));

    when(commandSender.getName()).thenReturn(matchingName, "NonMatchingName");
    when(commandSender1.getName()).thenReturn(matchingName, "AnotherName");
    assertTrue(BukkitUtil.playersEqual(commandSender, commandSender1));
    assertFalse(BukkitUtil.playersEqual(commandSender, commandSender1));
  }

  @Test
  public void testPlayersEqualOfflinePlayer() {
    final UUID matchingUUID = UUID.randomUUID();
    final String matchingName = "MatchingTestName";
    final CommandSender commandSender =
        mock(CommandSender.class, withSettings().extraInterfaces(OfflinePlayer.class));
    final CommandSender commandSender1 =
        mock(CommandSender.class, withSettings().extraInterfaces(OfflinePlayer.class));

    when(((OfflinePlayer) commandSender).getUniqueId()).thenReturn(matchingUUID, UUID.randomUUID());
    when(((OfflinePlayer) commandSender1).getUniqueId())
        .thenReturn(matchingUUID, UUID.randomUUID());
    when(commandSender.getName()).thenReturn(matchingName, "NonMatchingName");
    when(commandSender1.getName()).thenReturn(matchingName, "AnotherName");
    // UUID check => true
    assertTrue(BukkitUtil.playersEqual(commandSender, commandSender1));
    // UUID check => false => name check => true
    assertTrue(BukkitUtil.playersEqual(commandSender, commandSender1));
    // UUID check => false => name check => false
    assertFalse(BukkitUtil.playersEqual(commandSender, commandSender1));
  }

  @Test
  public void testColorCodeReplaceAll() {
    assertEquals("", BukkitUtil.colorCodeReplaceAll(""));
    assertEquals("This is a string", BukkitUtil.colorCodeReplaceAll("This is a string"));
    assertEquals(
        "§0This §1is §2a §3string", BukkitUtil.colorCodeReplaceAll("&0This &1is &2a &3string"));
    assertEquals("§a", BukkitUtil.colorCodeReplaceAll("&a"));
  }

  @Test
  public void testGetUUIDString() {
    final UUID uuid = UUID.randomUUID();
    final OfflinePlayer offlinePlayer =
        mock(OfflinePlayer.class, withSettings().extraInterfaces(CommandSender.class));
    when(offlinePlayer.getUniqueId()).thenReturn(uuid);
    when(offlinePlayer.getName()).thenReturn("testName");

    assertEquals(uuid.toString(), BukkitUtil.getUUIDString((CommandSender) offlinePlayer));
  }

  @Test
  public void testGetUUIDStringCommandSender() {
    final CommandSender commandSender = mock(CommandSender.class);

    assertEquals("", BukkitUtil.getUUIDString(commandSender));
  }

  @Test
  public void testGetUUIDStringPlayer() {
    final UUID uuid = UUID.randomUUID();
    final OfflinePlayer player = mock(OfflinePlayer.class);
    when(player.getName()).thenReturn("TestPlayerName");
    when(player.getUniqueId()).thenReturn(uuid);

    assertEquals(uuid.toString(), BukkitUtil.getUUIDString(player));
  }

  @Test
  public void testGetUUIDStringPlayerInvalidUsername() {
    final OfflinePlayer player = mock(OfflinePlayer.class);
    when(player.getName()).thenReturn("_");

    assertEquals("", BukkitUtil.getUUIDString(player));
  }

  @Test
  public void testGetUUIDStringPlayerInvalidUUID() {
    final OfflinePlayer player = mock(OfflinePlayer.class);
    when(player.getName()).thenReturn("TestPlayerName");
    when(player.getUniqueId()).thenReturn(BukkitUtil.invalidUserUUID);

    assertEquals("", BukkitUtil.getUUIDString(player));
  }

  @Test
  public void testGetOfflinePlayerInvalidUUIDAndName() {
    assertNull(BukkitUtil.getOfflinePlayer(null, null));
    assertNull(BukkitUtil.getOfflinePlayer(null, ""));
    assertNull(BukkitUtil.getOfflinePlayer(BukkitUtil.invalidUserUUID, null));
  }

  @Test
  public void testGetOfflinePlayerValidUUID() {
    final UUID testUUID = UUID.randomUUID();
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    bukkit.when(() -> Bukkit.getOfflinePlayer(testUUID)).thenReturn(offlinePlayer);
    assertEquals(offlinePlayer, BukkitUtil.getOfflinePlayer(testUUID, null));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetOfflinePlayerValidName() {
    final String name = "TestName";
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    bukkit.when(() -> Bukkit.getOfflinePlayer(name)).thenReturn(offlinePlayer);
    assertEquals(offlinePlayer, BukkitUtil.getOfflinePlayer(null, name));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerOnline() {
    final String playerName = "testPlayer";
    final Player player = mock(Player.class);
    bukkit.when(() -> Bukkit.getPlayer(playerName)).thenReturn(player);

    assertEquals(player, BukkitUtil.getPlayer(playerName, false));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerOnlineMatch() {
    final String playerName = "testPlayer";
    final Player player = mock(Player.class);
    bukkit.when(() -> Bukkit.getPlayer(playerName)).thenReturn(player);

    assertEquals(player, BukkitUtil.getPlayer(playerName, true));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerOffline() {
    final String playerName = "testPlayer";
    final OfflinePlayer player = mock(OfflinePlayer.class);
    when(player.hasPlayedBefore()).thenReturn(true);
    bukkit.when(() -> Bukkit.getOfflinePlayer(playerName)).thenReturn(player);

    assertEquals(player, BukkitUtil.getPlayer(playerName, false));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerOfflineMatch() {
    final String playerName = "testPlayer";
    final OfflinePlayer player = mock(OfflinePlayer.class);
    when(player.hasPlayedBefore()).thenReturn(true);
    bukkit.when(() -> Bukkit.getOfflinePlayer(playerName)).thenReturn(player);

    assertEquals(player, BukkitUtil.getPlayer(playerName, true));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerNotOnlineHasPlayedBefore() {
    final String playerName = "testPlayer";
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.hasPlayedBefore()).thenReturn(true);
    bukkit.when(() -> Bukkit.getOfflinePlayer(playerName)).thenReturn(offlinePlayer);

    assertEquals(offlinePlayer, BukkitUtil.getPlayer(playerName, false));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerNotOnlineHasPlayedBeforeMatch() {
    final String playerName = "testPlayer";
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.hasPlayedBefore()).thenReturn(true);
    bukkit.when(() -> Bukkit.getOfflinePlayer(playerName)).thenReturn(offlinePlayer);

    assertEquals(offlinePlayer, BukkitUtil.getPlayer(playerName, true));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerNotOnlineHasNotPlayedBefore() {
    final String playerName = "testPlayer";
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.hasPlayedBefore()).thenReturn(false);
    bukkit.when(() -> Bukkit.getOfflinePlayer(playerName)).thenReturn(offlinePlayer);

    assertNull(BukkitUtil.getPlayer(playerName, false));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerNotOnlineHasNotPlayedBeforeMatch() {
    final String playerName = "player";
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.hasPlayedBefore()).thenReturn(false);
    final OfflinePlayer matchedOfflinePlayer = mock(OfflinePlayer.class);
    when(matchedOfflinePlayer.getName()).thenReturn(playerName);
    final OfflinePlayer otherOfflinePlayer = mock(OfflinePlayer.class);
    when(otherOfflinePlayer.getName()).thenReturn("otherOfflinePlayer");

    bukkit.when(() -> Bukkit.getOfflinePlayer(playerName)).thenReturn(offlinePlayer);
    bukkit
        .when(Bukkit::getOfflinePlayers)
        .thenReturn(new OfflinePlayer[] {otherOfflinePlayer, matchedOfflinePlayer});

    assertEquals(matchedOfflinePlayer, BukkitUtil.getPlayer(playerName, true));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerAnonymous() {
    assertEquals(BukkitUtil.anonymousPlayer, BukkitUtil.getPlayer("!", false));
    assertEquals(BukkitUtil.anonymousPlayer, BukkitUtil.getPlayer("!", true));
    assertEquals(BukkitUtil.anonymousPlayer, BukkitUtil.getPlayer("*", false));
    assertEquals(BukkitUtil.anonymousPlayer, BukkitUtil.getPlayer("*", true));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetPlayerInvalidUsername() {
    assertNull(BukkitUtil.getPlayer("_", false));
    assertNull(BukkitUtil.getPlayer("_", true));
  }

  @Test
  public void testMatchOfflinePlayer() {
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.getName()).thenReturn("Test");
    final OfflinePlayer offlinePlayer2 = mock(OfflinePlayer.class);
    when(offlinePlayer2.getName()).thenReturn("another");
    final OfflinePlayer offlinePlayer3 = mock(OfflinePlayer.class);
    when(offlinePlayer3.getName()).thenReturn("Player");
    final OfflinePlayer offlinePlayer4 = mock(OfflinePlayer.class);
    when(offlinePlayer4.getName()).thenReturn("another player");
    final OfflinePlayer[] offlinePlayers = {
      offlinePlayer, offlinePlayer2, offlinePlayer3, offlinePlayer4
    };
    bukkit.when(Bukkit::getOfflinePlayers).thenReturn(offlinePlayers);
    assertEquals(offlinePlayer, BukkitUtil.matchOfflinePlayer("Test"));
    assertEquals(offlinePlayer, BukkitUtil.matchOfflinePlayer("te"));
    assertEquals(offlinePlayer2, BukkitUtil.matchOfflinePlayer("ANOTHER"));
    assertEquals(offlinePlayer2, BukkitUtil.matchOfflinePlayer("a"));
    assertEquals(offlinePlayer3, BukkitUtil.matchOfflinePlayer("player"));
    assertEquals(offlinePlayer4, BukkitUtil.matchOfflinePlayer("another player"));
    assertEquals(offlinePlayer4, BukkitUtil.matchOfflinePlayer("another pl"));
  }

  @Test
  public void testMatchOfflinePlayerNoMatch() {
    final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
    when(offlinePlayer.getName()).thenReturn("Test");
    final OfflinePlayer offlinePlayer2 = mock(OfflinePlayer.class);
    when(offlinePlayer2.getName()).thenReturn("another");
    final OfflinePlayer offlinePlayer3 = mock(OfflinePlayer.class);
    when(offlinePlayer3.getName()).thenReturn("Player");
    final OfflinePlayer offlinePlayer4 = mock(OfflinePlayer.class);
    when(offlinePlayer4.getName()).thenReturn("another player");
    final OfflinePlayer[] offlinePlayers = {
      offlinePlayer, offlinePlayer2, offlinePlayer3, offlinePlayer4
    };
    bukkit.when(Bukkit::getOfflinePlayers).thenReturn(offlinePlayers);
    assertNull(BukkitUtil.matchOfflinePlayer("Does not exist"));
    assertNull(BukkitUtil.matchOfflinePlayer("no player"));
    assertNull(BukkitUtil.matchOfflinePlayer("test player"));
    assertNull(BukkitUtil.matchOfflinePlayer("player test"));
  }

  @Test
  public void testGetPlugin() {
    final String pluginName = "Test plugin";
    final PluginManager pluginManager = mock(PluginManager.class);
    final Plugin testPlugin = mock(Plugin.class);
    when(pluginManager.isPluginEnabled(pluginName)).thenReturn(true);
    when(pluginManager.getPlugin(pluginName)).thenReturn(testPlugin);

    bukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);

    assertEquals(testPlugin, BukkitUtil.getPlugin(pluginName));
    verify(pluginManager).isPluginEnabled(pluginName);
    verify(pluginManager).getPlugin(pluginName);
  }

  @Test(expected = IllegalPluginAccessException.class)
  public void testGetPluginNotFound() {
    final String pluginName = "Test plugin";
    final PluginManager pluginManager = mock(PluginManager.class);
    when(pluginManager.isPluginEnabled(pluginName)).thenReturn(false);
    bukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);

    try {
      BukkitUtil.getPlugin(pluginName);
    } finally {
      verify(pluginManager).isPluginEnabled(pluginName);
    }
  }

  @Test
  public void testConvertSecondsToServerTicks() {
    assertEquals((Long) 20L, BukkitUtil.convertSecondsToServerTicks(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertSecondsToServerTicksNegative() {
    BukkitUtil.convertSecondsToServerTicks(-1);
  }
}
