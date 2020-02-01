package net.KabOOm356.Service.Messager;

import net.KabOOm356.Service.Messager.Messages.Message;
import net.KabOOm356.Service.Messager.Messages.ReporterMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	public PendingMessages(final Message message) {
		super();

		add(message);
	}

	/**
	 * Constructor.
	 *
	 * @param messages Initial messages.
	 */
	public PendingMessages(final List<Message> messages) {
		super();

		addAll(messages);
	}

	@Override
	public boolean add(final Message message) {
		if (message instanceof ReporterMessage) {
			final ReporterMessage reporterMessage = (ReporterMessage) message;

			final Iterator<Message> messages = this.iterator();
			Message pendingMessage;

			while (messages.hasNext()) {
				pendingMessage = messages.next();

				if (pendingMessage instanceof ReporterMessage) {
					final ReporterMessage reporterPendingMessage = (ReporterMessage) pendingMessage;

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
	public void reindexMessages(final List<Integer> remainingIndexes) {
		for (final Message message : this) {
			if (message instanceof ReporterMessage) {
				final ReporterMessage reporterMessage = (ReporterMessage) message;

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
	public void removeIndex(final int index) {
		for (final Message message : this) {
			if (message instanceof ReporterMessage) {
				((ReporterMessage) message).removeIndex(index);
			}
		}

		removeEmpty();
	}

	@Override
	public boolean remove(final Object obj) {
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
		final List<Message> deletion = new ArrayList<>();

		for (final Message message : this) {
			if (message.isEmpty()) {
				deletion.add(message);
			}
		}

		for (final Message message : deletion) {
			this.remove(message);
		}
	}

	@Override
	public boolean isEmpty() {
		if (!super.isEmpty()) {
			for (final Message message : this) {
				if (!message.isEmpty()) {
					return false;
				}
			}
		}

		return true;
	}
}
