package net.KabOOm356.Service;

import net.KabOOm356.Locale.Entry.LocalePhrase;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import test.test.Answer.LocaleEntryAnswer;
import test.test.service.ServiceTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PlayerServiceTest extends ServiceTest {
	private PlayerService manager;

	@Mock
	private PermissionService permissionService;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();

		when(getModule().getPermissionService()).thenReturn(permissionService);
		when(permissionService.hasPermission(any(Player.class), anyString())).thenReturn(false);
		manager = spy(new PlayerService(getModule()));
	}

	@Test
	public void testGetModLevelOp() {
		final CommandSender commandSender = mock(CommandSender.class);
		when(commandSender.isOp()).thenReturn(true);
		assertEquals(ModLevel.HIGH, manager.getModLevel(commandSender));
	}

	@Test
	public void testGetModLevelConsole() {
		final ConsoleCommandSender commandSender = mock(ConsoleCommandSender.class);
		when(commandSender.isOp()).thenReturn(false);
		assertEquals(ModLevel.HIGH, manager.getModLevel(commandSender));
	}

	@Test
	public void testGetModLevelNotPlayer() {
		final CommandSender commandSender = mock(CommandSender.class);
		try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			assertEquals(ModLevel.NONE, manager.getModLevel(commandSender));
		}
	}

	@Test
	public void testGetModLevelNoModLevel() {
		final CommandSender commandSender =
				mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
		try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			assertEquals(ModLevel.NONE, manager.getModLevel(commandSender));
		}
	}

	@Test
	public void testGetModLevelHighModLevel() {
		final CommandSender commandSender =
				mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
		when(permissionService.hasPermission(
				(Player) commandSender, PlayerService.getHighModLevelPermission()))
				.thenReturn(true);
		try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			assertEquals(ModLevel.HIGH, manager.getModLevel(commandSender));
		}
	}

	@Test
	public void testGetModLevelNormalModLevel() {
		final CommandSender commandSender =
				mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
		when(permissionService.hasPermission(
				(Player) commandSender, PlayerService.getNormalModLevelPermission()))
				.thenReturn(true);
		try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			assertEquals(ModLevel.NORMAL, manager.getModLevel(commandSender));
		}
	}

	@Test
	public void testGetModLevelLowModLevel() {
		final CommandSender commandSender =
				mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
		when(permissionService.hasPermission(
				(Player) commandSender, PlayerService.getLowModLevelPermission()))
				.thenReturn(true);
		assertEquals(ModLevel.LOW, manager.getModLevel(commandSender));
	}

	@Test
	public void testRequireModLevelInBounds() {
		final String modLevelString = "modLevel";
		final CommandSender commandSender = mock(CommandSender.class);
		try (final MockedStatic<ModLevel> modLevel = mockStatic(ModLevel.class)) {
			modLevel.when(() -> ModLevel.modLevelInBounds(anyString())).thenReturn(true);
			assertTrue(manager.requireModLevelInBounds(commandSender, modLevelString));
		}
	}

	@Test
	public void testRequireModLevelNotInBounds() {
		final String modLevelString = "modLevel";
		final CommandSender commandSender = mock(CommandSender.class);
		try (final MockedStatic<ModLevel> modLevel = mockStatic(ModLevel.class)) {
			modLevel.when(() -> ModLevel.modLevelInBounds(anyString())).thenReturn(false);
			assertFalse(manager.requireModLevelInBounds(commandSender, modLevelString));
			verify(commandSender).sendMessage(anyString());
		}
	}

	@Test
	public void testDisplayModLevel() {
		when(getLocale().getString(any(LocalePhrase.class))).thenAnswer(LocaleEntryAnswer.instance);
		final CommandSender commandSender = mock(CommandSender.class);
		doReturn(ModLevel.NONE).when(manager).getModLevel(commandSender);
		manager.displayModLevel(commandSender);
		verify(commandSender).sendMessage(anyString());
	}

	@Test
	public void testDisplayModLevelCommandSenderPlayer() {
		when(getLocale().getString(any(LocalePhrase.class))).thenAnswer(LocaleEntryAnswer.instance);
		final CommandSender commandSender = mock(CommandSender.class);
		final CommandSender player = mock(CommandSender.class);
		when(BukkitUtil.formatPlayerName(player)).thenReturn("player");
		doReturn(ModLevel.NONE).when(manager).getModLevel(player);
		manager.displayModLevel(commandSender, player);
		verify(commandSender).sendMessage(anyString());
	}
}
