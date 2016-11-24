package net.KabOOm356.Service.Store.type;

import net.KabOOm356.Runnable.Timer.ReportTimer;
import net.KabOOm356.Util.BukkitUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerReport extends HashMap<UUID, PlayerReportQueue> {
	private static final long serialVersionUID = 8004603524749240197L;

	/**
	 * Returns all the players the given {@link OfflinePlayer} has reported.
	 *
	 * @param sender The {@link OfflinePlayer}.
	 * @return A {@link PlayerReportQueue} containing all the players the {@link OfflinePlayer} has reported.
	 */
	public PlayerReportQueue get(final OfflinePlayer sender) {
		Validate.notNull(sender);
		return super.get(sender.getUniqueId());
	}

	/**
	 * Returns all the players the given {@link CommandSender} has reported.
	 *
	 * @param sender The {@link CommandSender}.
	 * @return A {@link PlayerReportQueue} containing all the players the {@link CommandSender} has reported.
	 */
	public PlayerReportQueue get(final CommandSender sender) {
		if (BukkitUtil.isOfflinePlayer(sender)) {
			final OfflinePlayer player = OfflinePlayer.class.cast(sender);
			return get(player);
		}
		return null;
	}

	/**
	 * Adds the {@link ReportTimer} to the sender's queue.
	 *
	 * @param sender   The {@link CommandSender} that reported.
	 * @param reported The {@link OfflinePlayer} that the sender reported.
	 * @param timer    The {@link ReportTimer}.
	 */
	public void put(final CommandSender sender, final OfflinePlayer reported, final ReportTimer timer) {
		if (BukkitUtil.isPlayer(sender)) {
			final Player player = Player.class.cast(sender);
			final UUID key = player.getUniqueId();
			add(key, reported, timer);
		}
	}

	/**
	 * Adds the {@link ReportTimer} to the sender's queue.
	 *
	 * @param sender   The {@link OfflinePlayer} that reported
	 * @param reported The {@link OfflinePlayer} that the sender reported.
	 * @param timer    The {@link ReportTimer}.
	 */
	public void put(final OfflinePlayer sender, final OfflinePlayer reported, final ReportTimer timer) {
		final UUID key = sender.getUniqueId();
		add(key, reported, timer);
	}

	/**
	 * Removes the {@link ReportTimer} from the sender's queue.
	 *
	 * @param sender   The {@link OfflinePlayer} that reported.
	 * @param reported The {@link OfflinePlayer} that the sender reported.
	 * @param timer    The {@link ReportTimer} to remove.
	 */
	public void remove(final OfflinePlayer sender, final OfflinePlayer reported, final ReportTimer timer) {
		final PlayerReportQueue playerReport = get(sender);
		if (playerReport != null) {
			playerReport.remove(reported, timer);
		}
	}

	private void add(final UUID key, final OfflinePlayer player, final ReportTimer timer) {
		final PlayerReportQueue queue;
		if (containsKey(key)) {
			queue = get(key);
		} else {
			queue = new PlayerReportQueue();
			put(key, queue);
		}
		queue.put(player, timer);
	}
}
