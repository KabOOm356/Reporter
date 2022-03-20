package net.KabOOm356.Service;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import test.test.service.ServiceTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PermissionServiceTest extends ServiceTest {
	private static final String permission = "permission";

	private PermissionService manager;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();

		manager = new PermissionService(getModule());
	}

	@Test
	public void hasPermissionCommandSenderIsPlayer() {
		final CommandSender sender =
				mock(CommandSender.class, withSettings().extraInterfaces(Player.class));
		when(getPermissionHandler().hasPermission((Player) sender, permission)).thenReturn(true);
		assertTrue(manager.hasPermission(sender, permission));
	}

	@Test
	public void hasPermissionCommandSenderNotAPlayer() {
		final CommandSender sender = mock(CommandSender.class);
		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			assertTrue(manager.hasPermission(sender, permission));
		}
	}

	@Test
	public void hasPermissionPlayer() {
		final Player player = mock(Player.class);
		assertFalse(manager.hasPermission(player, permission));
	}

	@Test
	public void hasPermissionPlayerIsOpNotConfigured() {
		final Player player = mock(Player.class);
		assertFalse(manager.hasPermission(player, permission));
	}

	@Test
	public void hasPermissionPlayerIsOpIsConfigured() {
		final Player player = mock(Player.class);
		when(player.isOp()).thenReturn(true);
		when(getConfiguration().getBoolean(anyString(), anyBoolean())).thenReturn(true);
		assertTrue(manager.hasPermission(player, permission));
	}
}
