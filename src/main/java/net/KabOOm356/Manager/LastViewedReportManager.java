package net.KabOOm356.Manager;

import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.Util;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LastViewedReportManager {
	public static final int noLastViewedIndex = -1;
	public static final String lastViewedIndex = "last";

	private final Map<CommandSender, Integer> lastViewed = new HashMap<CommandSender, Integer>();

	private static int getIndex(final String index) {
		return Util.parseInt(index);
	}

	/**
	 * Gets the last viewed report index for the given {@link CommandSender}.
	 *
	 * @param sender The {@link CommandSender} to get the last viewed report index for.
	 * @return The last viewed report index of the given {@link CommandSender}.
	 * @throws NoLastViewedReportException Thrown if the sender has not viewed a report.
	 */
	public int getLastViewed(final CommandSender sender) throws NoLastViewedReportException {
		Validate.notNull(sender);
		if (!hasLastViewed(sender)) {
			throw new NoLastViewedReportException(String.format("Sender [%s] does not have a last viewed report!", BukkitUtil.formatPlayerName(sender)));
		}
		return getLastViewed().get(sender);
	}

	/**
	 * Checks if the given {@link CommandSender} has a last viewed report.
	 *
	 * @param sender The {@link CommandSender}.
	 * @return True if the {@link CommandSender} has a last viewed report, otherwise false.
	 */
	public boolean hasLastViewed(final CommandSender sender) {
		return getLastViewed().containsKey(sender) && getLastViewed().get(sender) != noLastViewedIndex;
	}

	/**
	 * Gets the last viewed report viewed by the {@link CommandSender} or the parsed int value from the given index.
	 *
	 * @param sender The {@link CommandSender} that requested their last viewed report.
	 * @param index  The index the sender specified.
	 * @return An int that is either the last report the sender viewed or the parsed value from the given index.
	 * @throws NoLastViewedReportException Thrown if the index equals {@link #lastViewedIndex} but the {@link CommandSender} has not viewed a report yet.
	 */
	public int getIndexOrLastViewedReport(final CommandSender sender, final String index) throws NoLastViewedReportException {
		Validate.notNull(sender);
		Validate.notNull(index);
		Validate.notEmpty(index);
		if (lastViewedIndex.equalsIgnoreCase(index)) {
			return getLastViewed(sender);
		} else {
			return getIndex(index);
		}
	}

	/**
	 * Records that the given {@link CommandSender} has viewed the given index.
	 *
	 * @param sender The {@link CommandSender} that viewed the report.
	 * @param index  The index of the report that was viewed.
	 */
	public void playerViewed(final CommandSender sender, final int index) {
		getLastViewed().put(sender, index);
	}

	/**
	 * Removes the record of the {@link CommandSender}'s last report.  This should be called when the {@link CommandSender} quits.
	 *
	 * @param sender The {@link CommandSender} whose last viewed report record to remove.
	 */
	public void removeLastViewedReport(final CommandSender sender) {
		getLastViewed().remove(sender);
	}

	/**
	 * Removes the given report index from all players that have viewed it last.
	 *
	 * @param index The index of the report that should be removed.
	 */
	public void deleteIndex(final int index) {
		for (final Map.Entry<CommandSender, Integer> e : getLastViewed().entrySet()) {
			if (e.getValue() == index) {
				e.setValue(noLastViewedIndex);
			} else if (e.getValue() > index) {
				e.setValue(e.getValue() - 1);
			}
		}
	}

	/**
	 * Removes all report indexes that are not contained in the given {@link List} from all players that have viewed them.
	 *
	 * @param remainingIndexes The indexes that remain.
	 */
	public void deleteBatch(final List<Integer> remainingIndexes) {
		for (final Map.Entry<CommandSender, Integer> e : getLastViewed().entrySet()) {
			if (remainingIndexes.contains(e.getValue())) {
				e.setValue(remainingIndexes.indexOf(e.getValue()) + 1);
			} else {
				e.setValue(noLastViewedIndex);
			}
		}
	}

	private Map<CommandSender, Integer> getLastViewed() {
		return lastViewed;
	}
}
