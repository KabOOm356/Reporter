package net.KabOOm356.Service;

import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.service.ServiceTest;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({PlayerService.class, BukkitUtil.class, Bukkit.class, Player.class, ModLevel.class})
public class PlayerServiceTest extends ServiceTest {
	private static final Field highModLevelPermission = field(PlayerService.class, "highModLevelPermission");
	private static final Field normalModLevelPermission = field(PlayerService.class, "normalModLevelPermission");
	private static final Field lowModLevelPermission = field(PlayerService.class, "lowModLevelPermission");

	private PlayerService manager;

	@Mock
	private PermissionService permissionService;

	private static String getPermissionString(final Field field) throws IllegalAccessException {
		Validate.notNull(field);
		return field.get(null).toString();
	}

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();
		mockStatic(Bukkit.class);
		mockStatic(BukkitUtil.class);
		mockStatic(Player.class);
		mockStatic(ModLevel.class);
		when(getModule().getPermissionService()).thenReturn(permissionService);
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
		when(BukkitUtil.isPlayer(commandSender)).thenReturn(false);
		assertEquals(ModLevel.NONE, manager.getModLevel(commandSender));
	}

	@Test
	public void testGetModLevelNoModLevel() {
		final CommandSender commandSender = mock(CommandSender.class);
		final Player player = mock(Player.class);
		when(Player.class.cast(commandSender)).thenReturn(player);
		when(BukkitUtil.isPlayer(commandSender)).thenReturn(true);
		assertEquals(ModLevel.NONE, manager.getModLevel(commandSender));
	}

	@Test
	public void testGetModLevelHighModLevel() throws IllegalAccessException {
		final CommandSender commandSender = mock(CommandSender.class);
		final Player player = mock(Player.class);
		when(Player.class.cast(commandSender)).thenReturn(player);
		when(BukkitUtil.isPlayer(commandSender)).thenReturn(true);
		when(permissionService.hasPermission(player, getPermissionString(highModLevelPermission))).thenReturn(true);
		assertEquals(ModLevel.HIGH, manager.getModLevel(commandSender));
	}

	@Test
	public void testGetModLevelNormalModLevel() throws IllegalAccessException {
		final CommandSender commandSender = mock(CommandSender.class);
		final Player player = mock(Player.class);
		when(Player.class.cast(commandSender)).thenReturn(player);
		when(BukkitUtil.isPlayer(commandSender)).thenReturn(true);
		when(permissionService.hasPermission(player, getPermissionString(normalModLevelPermission))).thenReturn(true);
		assertEquals(ModLevel.NORMAL, manager.getModLevel(commandSender));
	}

	@Test
	public void testGetModLevelLowModLevel() throws IllegalAccessException {
		final CommandSender commandSender = mock(CommandSender.class);
		final Player player = mock(Player.class);
		when(Player.class.cast(commandSender)).thenReturn(player);
		when(BukkitUtil.isPlayer(commandSender)).thenReturn(true);
		when(permissionService.hasPermission(player, getPermissionString(lowModLevelPermission))).thenReturn(true);
		assertEquals(ModLevel.LOW, manager.getModLevel(commandSender));
	}

	@Test
	public void testRequireModLevelInBounds() {
		final String modLevelString = "modLevel";
		final CommandSender commandSender = mock(CommandSender.class);
		when(ModLevel.modLevelInBounds(anyString())).thenReturn(true);
		assertTrue(manager.requireModLevelInBounds(commandSender, modLevelString));
	}

	@Test
	public void testRequireModLevelNotInBounds() {
		final String modLevelString = "modLevel";
		final CommandSender commandSender = mock(CommandSender.class);
		when(ModLevel.modLevelInBounds(anyString())).thenReturn(false);
		assertFalse(manager.requireModLevelInBounds(commandSender, modLevelString));
		verify(commandSender).sendMessage(anyString());
	}

	@Test
	public void testDisplayModLevel() {
		final CommandSender commandSender = mock(CommandSender.class);
		doReturn(ModLevel.NONE).when(manager).getModLevel(commandSender);
		manager.displayModLevel(commandSender);
		verify(commandSender).sendMessage(anyString());
	}

	@Test
	public void testDisplayModLevelCommandSenderPlayer() {
		final CommandSender commandSender = mock(CommandSender.class);
		final CommandSender player = mock(CommandSender.class);
		when(BukkitUtil.formatPlayerName(player)).thenReturn("player");
		doReturn(ModLevel.NONE).when(manager).getModLevel(player);
		manager.displayModLevel(commandSender, player);
		verify(commandSender).sendMessage(anyString());
	}
}
