package net.KabOOm356.Manager.Messager;

import net.KabOOm356.Manager.Messager.Messages.Message;

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
	public GroupMessages(Group group, Message message) {
		super();

		put(group, message);
	}

	/**
	 * Constructor.
	 *
	 * @param group    Initial group.
	 * @param messages Initial messages.
	 */
	public GroupMessages(Group group, ArrayList<Message> messages) {
		super();

		put(group, messages);
	}

	/**
	 * Adds the given message to the given group.
	 *
	 * @param group   The group to add the message to.
	 * @param message The message to add to the group.
	 */
	public void put(Group group, Message message) {
		if (!containsKey(group))
			put(group, new PendingMessages(message));
		else
			get(group).add(message);
	}

	/**
	 * Adds the given messages to the given group.
	 *
	 * @param group    The group to add the messages to.
	 * @param messages The messages to add to the group.
	 */
	public void put(Group group, ArrayList<Message> messages) {
		for (Message message : messages)
			put(group, message);
	}

	/**
	 * Re-indexes all the messages.
	 *
	 * @param remainingIndexes The remaining indexes after a batch deletion.
	 */
	public void reindexMessages(ArrayList<Integer> remainingIndexes) {
		for (Entry<Group, PendingMessages> messages : entrySet()) {
			PendingMessages pendingMessage = messages.getValue();

			pendingMessage.reindexMessages(remainingIndexes);
		}

		removeEmpty();
	}

	@Override
	public PendingMessages remove(Object key) {
		PendingMessages removed = super.remove(key);

		removeEmpty();

		return removed;
	}

	/**
	 * Removes the given message.
	 *
	 * @param message The message to remove.
	 */
	public void remove(Message message) {
		for (PendingMessages messages : this.values())
			messages.remove(message);

		removeEmpty();
	}

	/**
	 * Removes the given index from all messages and re-indexes the remaining indexes.
	 *
	 * @param index The index to remove.
	 */
	public void removeIndex(int index) {
		for (PendingMessages messages : this.values())
			messages.removeIndex(index);

		removeEmpty();
	}

	/**
	 * Removes the empty items from this structure.
	 */
	private void removeEmpty() {
		ArrayList<Group> removalKeys = new ArrayList<Group>();

		for (Entry<Group, PendingMessages> messages : entrySet()) {
			if (messages.getValue().isEmpty())
				removalKeys.add(messages.getKey());
		}

		for (Group key : removalKeys)
			this.remove(key);
	}

	@Override
	public boolean isEmpty() {
		if (!super.isEmpty()) {
			for (PendingMessages message : values()) {
				if (!message.isEmpty())
					return false;
			}
		}

		return true;
	}
}
