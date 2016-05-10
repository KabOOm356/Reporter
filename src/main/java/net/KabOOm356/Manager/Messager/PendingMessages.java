package net.KabOOm356.Manager.Messager;

import net.KabOOm356.Manager.Messager.Messages.Message;
import net.KabOOm356.Manager.Messager.Messages.ReporterMessage;

import java.util.ArrayList;
import java.util.Iterator;

public class PendingMessages extends ArrayList<Message> {
	/**
	 * Generated Serial ID.
	 */
	private static final long serialVersionUID = 8224514226473615637L;

	/**
	 * Constructor.
	 */
	public PendingMessages() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param message An initial message.
	 */
	public PendingMessages(Message message) {
		super();

		add(message);
	}

	/**
	 * Constructor.
	 *
	 * @param messages Initial messages.
	 */
	public PendingMessages(ArrayList<Message> messages) {
		super();

		addAll(messages);
	}

	@Override
	public boolean add(Message message) {
		if (message instanceof ReporterMessage) {
			ReporterMessage reporterMessage = (ReporterMessage) message;

			Iterator<Message> messages = this.iterator();
			Message pendingMessage;

			while (messages.hasNext()) {
				pendingMessage = messages.next();

				if (pendingMessage instanceof ReporterMessage) {
					ReporterMessage reporterPendingMessage = (ReporterMessage) pendingMessage;

					if (reporterMessage.messagesEqual(reporterPendingMessage)) {
						reporterPendingMessage.addIndexes(reporterMessage.getIndexes());
						return true;
					}
				}
			}

			return super.add(reporterMessage);
		}

		return super.add(message);
	}

	/**
	 * Re-indexes all the messages.
	 *
	 * @param remainingIndexes The remaining indexes after a batch deletion.
	 */
	public void reindexMessages(ArrayList<Integer> remainingIndexes) {
		for (Message message : this) {
			if (message instanceof ReporterMessage) {
				ReporterMessage reporterMessage = (ReporterMessage) message;

				reporterMessage.reindex(remainingIndexes);
			}
		}

		removeEmpty();
	}

	/**
	 * Removes the given index from all messages linking to it and re-indexes the remaining messages.
	 *
	 * @param index The index to remove from all messages.
	 */
	public void removeIndex(int index) {
		for (Message message : this) {
			if (message instanceof ReporterMessage)
				((ReporterMessage) message).removeIndex(index);
		}

		removeEmpty();
	}

	@Override
	public boolean remove(Object obj) {
		boolean removed = false;

		do {
			removed = removed || super.remove(obj);
		}
		while (contains(obj));

		removeEmpty();

		return removed;
	}

	/**
	 * Cleans up this structure, removing messages with no indexes.
	 */
	private void removeEmpty() {
		ArrayList<Message> deletion = new ArrayList<Message>();

		for (Message message : this) {
			if (message.isEmpty())
				deletion.add(message);
		}

		for (Message message : deletion)
			this.remove(message);
	}

	@Override
	public boolean isEmpty() {
		if (!super.isEmpty()) {
			for (Message message : this) {
				if (!message.isEmpty())
					return false;
			}
		}

		return true;
	}
}
