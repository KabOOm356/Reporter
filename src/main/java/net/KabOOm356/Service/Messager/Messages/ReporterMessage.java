package net.KabOOm356.Service.Messager.Messages;

import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.FormattingUtil;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Message} that has indexes associated with it.
 */
public class ReporterMessage extends Message {
	/**
	 * The indexes associated with this message.
	 */
	private final List<Integer> indexes;

	/**
	 * Constructor.
	 *
	 * @param message The initial message.
	 */
	public ReporterMessage(final String message) {
		super(message);

		indexes = new ArrayList<>();
	}

	/**
	 * Constructor.
	 *
	 * @param message An initial message.
	 */
	public ReporterMessage(final Message message) {
		super(message.getMessage());

		indexes = new ArrayList<>();
	}

	/**
	 * Constructor.
	 *
	 * @param message The message.
	 * @param index   An index associated with this message
	 */
	public ReporterMessage(final String message, final int index) {
		super(message);

		indexes = new ArrayList<>();

		indexes.add(index);
	}

	/**
	 * Returns the raw message.
	 * <br />
	 * The raw message does not have the indexes appended to it.
	 *
	 * @return The raw message.
	 */
	public String getRawMessage() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		String message = super.getMessage();

		final String indexString = ArrayUtil.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE);

		message = message.replaceAll("%i", indexString);

		return message;
	}

	/**
	 * Returns the indexes currently associated with this message.
	 *
	 * @return The indexes currently associated with this message.
	 */
	public List<Integer> getIndexes() {
		return indexes;
	}

	/**
	 * Adds the given messages indexes to this message.
	 *
	 * @param message The message to add the indexes from.
	 */
	public void addIndexes(final ReporterMessage message) {
		if (messagesEqual(message)) {
			addIndexes(message.getIndexes());
		}
	}

	/**
	 * Adds the given index to this message.
	 *
	 * @param index The index to add.
	 */
	public void addIndex(final int index) {
		if (!indexes.contains(index)) {
			indexes.add(index);
		}
	}

	/**
	 * Adds the given indexes to this message.
	 *
	 * @param indexes The indexes to add.
	 */
	public void addIndexes(final List<Integer> indexes) {
		for (final int index : indexes) {
			addIndex(index);
		}
	}

	/**
	 * Removes the given index and re-indexes the remaining indexes.
	 *
	 * @param index The index to remove.
	 */
	public void removeIndex(final int index) {
		int LCV = 0;

		while (LCV < indexes.size()) {
			if (indexes.get(LCV) == index) {
				indexes.remove(LCV);
			} else if (indexes.get(LCV) > index) {
				indexes.set(LCV, indexes.get(LCV) - 1);
				LCV++;
			} else {
				LCV++;
			}
		}
	}

	/**
	 * Re-indexes all the messages.
	 *
	 * @param remainingIndexes The remaining indexes after a batch deletion.
	 */
	public void reindex(final List<Integer> remainingIndexes) {
		int LCV = 0;

		while (LCV < indexes.size()) {
			if (remainingIndexes.contains(indexes.get(LCV))) {
				indexes.set(LCV, remainingIndexes.indexOf(indexes.get(LCV)) + 1);
				LCV++;
			} else {
				indexes.remove(LCV);
			}
		}
	}

	/**
	 * Compares this message to the given message.  Messages are equal if their raw message string is equal.
	 *
	 * @param message The message to compare this to.
	 * @return True if this and the given message are equal, otherwise false.
	 */
	public boolean messagesEqual(final ReporterMessage message) {
		return getRawMessage().equalsIgnoreCase(message.getRawMessage());
	}

	@Override
	public boolean isEmpty() {
		return indexes.isEmpty();
	}

	@Override
	public String toString() {
		final String sb = super.toString() +
				"\nIndexes: " +
				ArrayUtil.indexesToString(indexes) + '\n' +
				"Full Message: " + this.getMessage();
		return FormattingUtil.addTabsToNewLines(sb, 1);
	}
}
