package net.KabOOm356.Service.Messager;

import net.KabOOm356.Service.Messager.Messages.Message;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A {@link HashMap} that uses a {@link Group} as a key that points to a {@link PendingMessages} object.
 */
public class GroupMessages extends HashMap<Group, PendingMessages> {
	/**
	 * Generated Serial ID.
	 */
	private static final long serialVersionUID = -1335117825209551678L;

	/**
	 * Constructor.
	 */
	public GroupMessages() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param group   Initial group.
	 * @param message Initial message.
	 */
	public GroupMessages(final Group group, final Message message) {
		super();

		put(group, message);
	}

	/**
	 * Constructor.
	 *
	 * @param group    Initial group.
	 * @param messages Initial messages.
	 */
	public GroupMessages(final Group group, final ArrayList<Message> messages) {
		super();

		put(group, messages);
	}

	/**
	 * Adds the given message to the given group.
	 *
	 * @param group   The group to add the message to.
	 * @param message The message to add to the group.
	 */
	public void put(final Group group, final Message message) {
		if (!containsKey(group)) {
			put(group, new PendingMessages(message));
		} else {
			get(group).add(message);
		}
	}

	/**
	 * Adds the given messages to the given group.
	 *
	 * @param group    The group to add the messages to.
	 * @param messages The messages to add to the group.
	 */
	public void put(final Group group, final ArrayList<Message> messages) {
		for (final Message message : messages) {
			put(group, message);
		}
	}

	/**
	 * Re-indexes all the messages.
	 *
	 * @param remainingIndexes The remaining indexes after a batch deletion.
	 */
	public void reindexMessages(final ArrayList<Integer> remainingIndexes) {
		for (final Entry<Group, PendingMessages> messages : entrySet()) {
			final PendingMessages pendingMessage = messages.getValue();

			pendingMessage.reindexMessages(remainingIndexes);
		}

		removeEmpty();
	}

	@Override
	public PendingMessages remove(final Object key) {
		final PendingMessages removed = super.remove(key);

		removeEmpty();

		return removed;
	}

	/**
	 * Removes the given message.
	 *
	 * @param message The message to remove.
	 */
	public void remove(final Message message) {
		for (final PendingMessages messages : this.values()) {
			messages.remove(message);
		}

		removeEmpty();
	}

	/**
	 * Removes the given index from all messages and re-indexes the remaining indexes.
	 *
	 * @param index The index to remove.
	 */
	public void removeIndex(final int index) {
		for (final PendingMessages messages : this.values()) {
			messages.removeIndex(index);
		}

		removeEmpty();
	}

	/**
	 * Removes the empty items from this structure.
	 */
	private void removeEmpty() {
		final ArrayList<Group> removalKeys = new ArrayList<>();

		for (final Entry<Group, PendingMessages> messages : entrySet()) {
			if (messages.getValue().isEmpty()) {
				removalKeys.add(messages.getKey());
			}
		}

		for (final Group key : removalKeys) {
			this.remove(key);
		}
	}

	@Override
	public boolean isEmpty() {
		if (!super.isEmpty()) {
			for (final PendingMessages message : values()) {
				if (!message.isEmpty()) {
					return false;
				}
			}
		}

		return true;
	}
}
