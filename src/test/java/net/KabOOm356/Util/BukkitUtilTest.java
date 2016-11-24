package net.KabOOm356.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@SuppressWarnings("deprecation")
@PrepareForTest({BukkitUtil.class, OfflinePlayer.class, Bukkit.class})
public class BukkitUtilTest extends PowerMockitoTest {
	@BeforeClass
	public static void mockBukkit() {
		mockStatic(Bukkit.class);
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
		final OfflinePlayer player = mock(OfflinePlayer.class);

		String returned = BukkitUtil.formatPlayerName(sender);
		assertEquals(playerName, returned);

		mockStatic(BukkitUtil.class);
		mockStatic(OfflinePlayer.class);
		when(BukkitUtil.isOfflinePlayer(sender)).thenReturn(true);
		when(OfflinePlayer.class.cast(sender)).thenReturn(player);
		when(BukkitUtil.formatPlayerName(player)).thenReturn(playerName);
		when(BukkitUtil.formatPlayerName(sender)).thenCallRealMethod();

		returned = BukkitUtil.formatPlayerName(sender);
		assertEquals(playerName, returned);

		verifyStatic();
	}

	@Test
	public void testFormatPlayerNameCommandSenderDisplayRealName() {
		final String playerName = "TestName";
		final CommandSender sender = mock(CommandSender.class);
		when(sender.getName()).thenReturn(playerName);
		final OfflinePlayer player = mock(OfflinePlayer.class);

		String returned = BukkitUtil.formatPlayerName(sender, true);
		assertEquals(playerName, returned);

		mockStatic(BukkitUtil.class);
		mockStatic(OfflinePlayer.class);
		when(BukkitUtil.isOfflinePlayer(sender)).thenReturn(true);
		when(OfflinePlayer.class.cast(sender)).thenReturn(player);
		when(BukkitUtil.formatPlayerName(player, true)).thenReturn(playerName);
		when(BukkitUtil.formatPlayerName(sender, true)).thenCallRealMethod();

		returned = BukkitUtil.formatPlayerName(sender, true);
		assertEquals(playerName, returned);
	}

	@Test
	public void testFormatPlayerNameOfflinePlayer() {
		final String playerName = "TestName";
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.getName()).thenReturn(playerName);

		String returned = BukkitUtil.formatPlayerName(offlinePlayer);
		assertEquals(playerName, returned);

		final Player player = mock(Player.class);
		when(offlinePlayer.isOnline()).thenReturn(true);
		when(offlinePlayer.getPlayer()).thenReturn(player);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.formatPlayerName(player)).thenReturn(playerName);
		when(BukkitUtil.formatPlayerName(offlinePlayer)).thenCallRealMethod();

		returned = BukkitUtil.formatPlayerName(offlinePlayer);
		assertEquals(playerName, returned);
	}

	@Test
	public void testFormatPlayerNameOfflinePlayerDisplayRealName() {
		final String playerName = "TestName";
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.getName()).thenReturn(playerName);

		String returned = BukkitUtil.formatPlayerName(offlinePlayer, true);
		assertEquals(playerName, returned);

		final Player player = mock(Player.class);
		when(offlinePlayer.isOnline()).thenReturn(true);
		when(offlinePlayer.getPlayer()).thenReturn(player);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.formatPlayerName(player, true)).thenReturn(playerName);
		when(BukkitUtil.formatPlayerName(offlinePlayer, true)).thenCallRealMethod();

		returned = BukkitUtil.formatPlayerName(offlinePlayer, true);
		assertEquals(playerName, returned);
	}

	@Test
	public void testFormatPlayerNamePlayer() {
		final String playerName = "TestName";
		final Player player = mock(Player.class);
		when(player.getDisplayName()).thenReturn(playerName);
		when(player.getName()).thenReturn(playerName);

		mockStatic(BukkitUtil.class);
		when(BukkitUtil.formatPlayerName(playerName, playerName)).thenReturn(playerName);
		when(BukkitUtil.formatPlayerName(player)).thenCallRealMethod();

		final String returned = BukkitUtil.formatPlayerName(player);
		assertEquals(playerName, returned);
	}

	@Test
	public void testFormatPlayerNamePlayerDisplayRealName() {
		final String playerName = "TestName";
		final Player player = mock(Player.class);
		when(player.getDisplayName()).thenReturn(playerName);
		when(player.getName()).thenReturn(playerName);

		mockStatic(BukkitUtil.class);
		when(BukkitUtil.formatPlayerName(playerName, playerName, true)).thenReturn(playerName);
		when(BukkitUtil.formatPlayerName(player, true)).thenCallRealMethod();

		final String returned = BukkitUtil.formatPlayerName(player, true);
		assertEquals(playerName, returned);
	}

	@Test
	public void testFormatPlayerName() {
		String displayName = "TestName";
		String realName = "TestName";

		String returned = BukkitUtil.formatPlayerName(displayName, realName);
		assertEquals(displayName, returned);

		displayName = "TestName";
		realName = "Name";
		returned = BukkitUtil.formatPlayerName(displayName, realName);
		assertEquals(displayName, returned);

		displayName = "DisplayName";
		realName = "RealName";
		returned = BukkitUtil.formatPlayerName(displayName, realName);
		assertEquals("DisplayName " + ChatColor.GOLD + "(RealName)", returned);
	}

	@Test
	public void testFormatPlayerNameDisplayRealName() {
		String displayName = "TestName";
		String realName = "TestName";

		String returned = BukkitUtil.formatPlayerName(displayName, realName, true);
		assertEquals("TestName " + ChatColor.GOLD + "(TestName)", returned);

		displayName = "TestName";
		realName = "Name";
		returned = BukkitUtil.formatPlayerName(displayName, realName, true);
		assertEquals("TestName " + ChatColor.GOLD + "(Name)", returned);

		displayName = "DisplayName";
		realName = "RealName";
		returned = BukkitUtil.formatPlayerName(displayName, realName, true);
		assertEquals("DisplayName " + ChatColor.GOLD + "(RealName)", returned);

		displayName = "DisplayName";
		realName = "RealName";
		returned = BukkitUtil.formatPlayerName(displayName, realName, false);
		assertEquals("DisplayName " + ChatColor.GOLD + "(RealName)", returned);
	}

	@Test
	public void testIsPlayerValidOfflinePlayer() {
		final UUID uuid = UUID.randomUUID();
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.getUniqueId()).thenReturn(uuid, BukkitUtil.invalidUserUUID);

		assertTrue(BukkitUtil.isPlayerValid(offlinePlayer));
		assertFalse(BukkitUtil.isPlayerValid(offlinePlayer));
	}

	@Test
	public void testIsPlayerValid() {
		assertTrue(BukkitUtil.isPlayerValid(UUID.randomUUID()));
		assertFalse(BukkitUtil.isPlayerValid(BukkitUtil.invalidUserUUID));
	}

	@Test
	public void testIsPlayer() {
		assertTrue(BukkitUtil.isPlayer(mock(Player.class)));
		assertFalse(BukkitUtil.isPlayer(mock(CommandSender.class)));
	}

	@Test
	public void testIsOfflinePlayer() {
		assertTrue(BukkitUtil.isOfflinePlayer(mock(Player.class)));
		assertFalse(BukkitUtil.isOfflinePlayer(mock(CommandSender.class)));
	}

	@Test
	public void testPlayersEqual() {
		final String matchingName = "MatchingTestName";
		final CommandSender commandSender = mock(CommandSender.class);
		final CommandSender commandSender1 = mock(CommandSender.class);

		assertFalse(BukkitUtil.playersEqual((CommandSender) null, null));
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
		final CommandSender commandSender = mock(CommandSender.class);
		final CommandSender commandSender1 = mock(CommandSender.class);
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		final OfflinePlayer offlinePlayer1 = mock(OfflinePlayer.class);

		mockStatic(OfflinePlayer.class);
		when(OfflinePlayer.class.cast(commandSender)).thenReturn(offlinePlayer);
		when(OfflinePlayer.class.cast(commandSender1)).thenReturn(offlinePlayer1);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.isOfflinePlayer(commandSender)).thenReturn(true);
		when(BukkitUtil.isOfflinePlayer(commandSender1)).thenReturn(true);
		when(BukkitUtil.playersEqual(commandSender, commandSender1)).thenCallRealMethod();

		when(offlinePlayer.getUniqueId()).thenReturn(matchingUUID, UUID.randomUUID());
		when(offlinePlayer1.getUniqueId()).thenReturn(matchingUUID, UUID.randomUUID());
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
	public void testPlayerEqualPlayerUUID() {
		final UUID uuid = UUID.randomUUID();
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.getUniqueId()).thenReturn(uuid, UUID.randomUUID());
		assertTrue(BukkitUtil.playersEqual(offlinePlayer, uuid));
		assertFalse(BukkitUtil.playersEqual(offlinePlayer, uuid));
	}

	@Test
	public void testColorCodeReplaceAll() {
		assertEquals("", BukkitUtil.colorCodeReplaceAll(""));
		assertEquals("This is a string", BukkitUtil.colorCodeReplaceAll("This is a string"));
		assertEquals("§0This §1is §2a §3string", BukkitUtil.colorCodeReplaceAll("&0This &1is &2a &3string"));
		assertEquals("§a", BukkitUtil.colorCodeReplaceAll("&a"));
	}

	@Test
	public void testGetUUIDStringCommandSender() {
		final CommandSender commandSender = mock(CommandSender.class);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.isOfflinePlayer(commandSender)).thenReturn(false);
		when(BukkitUtil.getUUIDString(commandSender)).thenCallRealMethod();

		assertEquals("", BukkitUtil.getUUIDString(commandSender));

		final String testUUIDString = "testUUIDString";
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(BukkitUtil.isOfflinePlayer(commandSender)).thenReturn(true);
		mockStatic(OfflinePlayer.class);
		when(OfflinePlayer.class.cast(commandSender)).thenReturn(offlinePlayer);
		when(BukkitUtil.getUUIDString(offlinePlayer)).thenReturn(testUUIDString);

		assertEquals(testUUIDString, BukkitUtil.getUUIDString(commandSender));
		verifyStatic();
	}

	@Test
	public void testGetUUIDStringPlayer() {
		final UUID playerUUID = UUID.randomUUID();
		final String playerName = "TestPlayerName";
		final OfflinePlayer player = mock(OfflinePlayer.class);
		when(player.getName()).thenReturn(playerName);
		when(player.getUniqueId()).thenReturn(playerUUID);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.getUUIDString(player)).thenCallRealMethod();
		when(BukkitUtil.isUsernameValid(playerName)).thenReturn(false);
		when(BukkitUtil.isPlayerValid(player)).thenReturn(false);

		assertEquals("", BukkitUtil.getUUIDString(player));

		when(BukkitUtil.isUsernameValid(playerName)).thenReturn(true);
		when(BukkitUtil.isPlayerValid(player)).thenReturn(true);

		assertEquals(playerUUID.toString(), BukkitUtil.getUUIDString(player));
	}

	@Test
	public void testGetUUID() {
		final CommandSender commandSender = mock(CommandSender.class);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.getUUID(commandSender)).thenCallRealMethod();
		when(BukkitUtil.isOfflinePlayer(commandSender)).thenReturn(false);

		assertNull(BukkitUtil.getUUID(commandSender));

		final UUID playerUUID = UUID.randomUUID();
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.getUniqueId()).thenReturn(playerUUID);
		mockStatic(OfflinePlayer.class);
		when(OfflinePlayer.class.cast(commandSender)).thenReturn(offlinePlayer);
		when(BukkitUtil.isOfflinePlayer(commandSender)).thenReturn(true);

		assertEquals(playerUUID, BukkitUtil.getUUID(commandSender));
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
		mockStatic(Bukkit.class);
		when(Bukkit.getOfflinePlayer(testUUID)).thenReturn(offlinePlayer);
		assertEquals(offlinePlayer, BukkitUtil.getOfflinePlayer(testUUID, null));
		verifyStatic();
	}

	@Test
	public void testGetOfflinePlayerValidName() {
		final String name = "TestName";
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		mockStatic(Bukkit.class);
		when(Bukkit.getOfflinePlayer(name)).thenReturn(offlinePlayer);
		assertEquals(offlinePlayer, BukkitUtil.getOfflinePlayer(null, name));
		verifyStatic();
	}

	@Test
	public void testGetPlayerOnline() {
		final Player player = mock(Player.class);
		mockStatic(Bukkit.class);
		when(Bukkit.getPlayer(anyString())).thenReturn(player);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.getPlayer(anyString(), anyBoolean())).thenCallRealMethod();
		when(BukkitUtil.isUsernameValid(anyString())).thenReturn(true);

		assertEquals(player, BukkitUtil.getPlayer("player", false));
		assertEquals(player, BukkitUtil.getPlayer("player", true));
	}

	@Test
	public void testGetPlayerNotOnlineHasPlayedBefore() {
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.hasPlayedBefore()).thenReturn(true);
		mockStatic(Bukkit.class);
		when(Bukkit.getPlayer(anyString())).thenReturn(null);
		when(Bukkit.getOfflinePlayer(anyString())).thenReturn(offlinePlayer);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.getPlayer(anyString(), anyBoolean())).thenCallRealMethod();
		when(BukkitUtil.isUsernameValid(anyString())).thenReturn(true);

		assertEquals(offlinePlayer, BukkitUtil.getPlayer("player", false));
		assertEquals(offlinePlayer, BukkitUtil.getPlayer("player", true));
	}

	@Test
	public void testGetPlayerNotOnlineHasNotPlayedBeforeNoMatchingAllowed() {
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.hasPlayedBefore()).thenReturn(false);
		mockStatic(Bukkit.class);
		when(Bukkit.getPlayer(anyString())).thenReturn(null);
		when(Bukkit.getOfflinePlayer(anyString())).thenReturn(offlinePlayer);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.getPlayer(anyString(), anyBoolean())).thenCallRealMethod();
		when(BukkitUtil.isUsernameValid(anyString())).thenReturn(true);

		assertEquals(null, BukkitUtil.getPlayer("player", false));
		verifyStatic();
	}

	@Test
	public void testGetPlayerNotOnlineHasNotPlayedBeforeMatchingAllowed() {
		final OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		final OfflinePlayer matchedOfflinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.hasPlayedBefore()).thenReturn(false);
		mockStatic(Bukkit.class);
		when(Bukkit.getPlayer(anyString())).thenReturn(null);
		when(Bukkit.getOfflinePlayer(anyString())).thenReturn(offlinePlayer);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.getPlayer(anyString(), anyBoolean())).thenCallRealMethod();
		when(BukkitUtil.matchOfflinePlayer(anyString())).thenReturn(matchedOfflinePlayer);
		when(BukkitUtil.isUsernameValid(anyString())).thenReturn(true);

		assertEquals(matchedOfflinePlayer, BukkitUtil.getPlayer("player", true));
		verifyStatic();
	}

	@Test
	public void testGetPlayerAnonymous() {
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.getPlayer(anyString(), anyBoolean())).thenCallRealMethod();
		when(BukkitUtil.isUsernameValid(anyString())).thenReturn(false);

		assertEquals(BukkitUtil.anonymousPlayer, BukkitUtil.getPlayer("!", false));
		assertEquals(BukkitUtil.anonymousPlayer, BukkitUtil.getPlayer("!", true));
		assertEquals(BukkitUtil.anonymousPlayer, BukkitUtil.getPlayer("*", false));
		assertEquals(BukkitUtil.anonymousPlayer, BukkitUtil.getPlayer("*", true));
	}

	@Test
	public void testGetPlayerInvalidUsername() {
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.getPlayer(anyString(), anyBoolean())).thenCallRealMethod();
		when(BukkitUtil.isUsernameValid(anyString())).thenReturn(false);

		assertNull(BukkitUtil.getPlayer(anyString(), anyBoolean()));
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
		final OfflinePlayer[] offlinePlayers = {offlinePlayer, offlinePlayer2, offlinePlayer3, offlinePlayer4};
		mockStatic(Bukkit.class);
		when(Bukkit.getOfflinePlayers()).thenReturn(offlinePlayers);
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
		final OfflinePlayer[] offlinePlayers = {offlinePlayer, offlinePlayer2, offlinePlayer3, offlinePlayer4};
		mockStatic(Bukkit.class);
		when(Bukkit.getOfflinePlayers()).thenReturn(offlinePlayers);
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
		mockStatic(Bukkit.class);
		when(Bukkit.getPluginManager()).thenReturn(pluginManager);

		assertEquals(testPlugin, BukkitUtil.getPlugin(pluginName));
		verify(pluginManager).isPluginEnabled(pluginName);
		verify(pluginManager).getPlugin(pluginName);
		verifyStatic();
	}

	@Test(expected = IllegalPluginAccessException.class)
	public void testGetPluginNotFound() {
		final String pluginName = "Test plugin";
		final PluginManager pluginManager = mock(PluginManager.class);
		when(pluginManager.isPluginEnabled(pluginName)).thenReturn(false);
		mockStatic(Bukkit.class);
		when(Bukkit.getPluginManager()).thenReturn(pluginManager);

		try {
			BukkitUtil.getPlugin(pluginName);
		} finally {
			verify(pluginManager).isPluginEnabled(pluginName);
			verifyStatic();
		}
	}

	@Test
	public void testConvertSecondsToServerTicks() {
		assertTrue(BukkitUtil.convertSecondsToServerTicks(1) > 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertSecondsToServerTicksNegative() {
		BukkitUtil.convertSecondsToServerTicks(-1);
	}
}
