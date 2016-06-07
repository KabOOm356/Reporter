package net.KabOOm356.Service;

import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.service.ServiceTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({PermissionService.class, Reporter.class, Player.class, BukkitUtil.class, Bukkit.class})
public class PermissionServiceTest extends ServiceTest {
	private static final String permission = "permission";

	private PermissionService manager;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();

		mockStatic(Reporter.class);
		mockStatic(Player.class);
		mockStatic(Bukkit.class);
		mockStatic(BukkitUtil.class);

		manager = spy(new PermissionService(getModule()));
	}

	@Test
	public void hasPermissionCommandSender() {
		final CommandSender sender = mock(CommandSender.class);
		final Player player = mock(Player.class);
		when(BukkitUtil.isPlayer(sender)).thenReturn(true);
		when(Player.class.cast(sender)).thenReturn(player);
		doReturn(true).when(manager).hasPermission(player, permission);
		manager.hasPermission(sender, permission);
		verify(manager).hasPermission(player, permission);
	}

	@Test
	public void hasPermissionCommandSenderNotAPlayer() {
		final CommandSender sender = mock(CommandSender.class);
		when(BukkitUtil.isPlayer(sender)).thenReturn(false);
		assertTrue(manager.hasPermission(sender, permission));
	}

	@Test
	public void hasPermissionPlayer() {
		final Player player = mock(Player.class);
		assertFalse(manager.hasPermission(player, permission));
	}

	@Test
	public void hasPermissionPlayerIsOpNotConfigured() {
		final Player player = mock(Player.class);
		when(player.isOp()).thenReturn(true);
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
