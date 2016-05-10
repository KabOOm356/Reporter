package net.KabOOm356.Manager;

import net.KabOOm356.Manager.Messager.Group;
import net.KabOOm356.Manager.Messager.GroupMessages;
import net.KabOOm356.Manager.Messager.Messages.Message;
import net.KabOOm356.Manager.Messager.Messages.ReporterMessage;
import net.KabOOm356.Manager.Messager.Messages.SimpleMessage;
import net.KabOOm356.Manager.Messager.PendingMessages;
import net.KabOOm356.Manager.Messager.PlayerMessages;
import net.KabOOm356.Util.FormattingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A class to manage messaging players that are offline.
 */
public class MessageManager {
	/**
	 * The current pending messages.
	 */
	// PlayerName -> (Message Grouping -> (Messages))
	//private HashMap<String, HashMap<Group, ArrayList<Message>>> messages;
	private final PlayerMessages messages;

	/**
	 * Constructor.
	 */
	public MessageManager() {
		messages = new PlayerMessages();
	}

	/**
	 * Adds a message for the given player.
	 *
	 * @param player  The player.
	 * @param message The message to send to the player.
	 */
	public void addMessage(final String player, final String message) {
		messages.put(player, Group.DEFAULT, new SimpleMessage(message));
	}

	/**
	 * Adds a message for the given player.
	 *
	 * @param player  The player.
	 * @param message The message to send to the player.
	 * @param index   The report index the message is referring to.
	 */
	public void addMessage(final String player, final String message, final int index) {
		addMessage(player, Group.DEFAULT, message, index);
	}

	/**
	 * Adds a message for the given player in the given grouping.
	 *
	 * @param player  The player.
	 * @param group   The {@link Group} the message belongs to.
	 * @param message The message to send to the player.
	 */
	public void addMessage(final String player, final Group group, final String message, final int index) {
		messages.put(player, group, new ReporterMessage(message, index));
	}

	/**
	 * Re-indexes the report indexes.
	 * <br />
	 * This is called when groups of reports are deleted.
	 *
	 * @param remainingIndexes The remaining indexes in the database.
	 */
	public void reindexMessages(final ArrayList<Integer> remainingIndexes) {
		messages.reindexMessages(remainingIndexes);
	}

	/**
	 * Checks if the given player has any pending messages.
	 *
	 * @param player The player.
	 * @return True if the given player has any pending messages, otherwise false.
	 */
	public boolean hasMessages(final String player) {
		return messages.containsKey(player);
	}

	/**
	 * Checks if the given player has any pending messages in the given group
	 *
	 * @param player The player.
	 * @param group  The grouping of messages to check for.
	 * @return True if the given player has any pending messages in the given grouping, otherwise false.
	 */
	public boolean hasGroup(final String player, final Group group) {
		return hasMessages(player) && messages.get(player).containsKey(group);

	}

	/**
	 * Removes a report index from all places where the report index is referenced.
	 * <br /><br />
	 * <b>NOTE:</b> This also re-indexes all the other indexes.
	 *
	 * @param index The report index to remove.
	 */
	public void removeMessage(final int index) {
		messages.removeIndex(index);
	}

	/**
	 * Removes all the messages currently pending.
	 */
	public void removeAll() {
		messages.clear();
	}

	/**
	 * Gets all the pending messages for the given player.
	 *
	 * @param player The player.
	 * @return An {@link ArrayList} of messages that contains all the messages
	 * <br/ >sent since the player has been offline.
	 */
	public ArrayList<String> getMessages(final String player) {
		final ArrayList<String> playerMessages = new ArrayList<String>();

		if (messages.containsKey(player)) {
			for (final Entry<Group, PendingMessages> e : messages.get(player).entrySet()) {
				for (final Message message : e.getValue()) {
					playerMessages.add(message.getMessage());
				}
			}
		}

		return playerMessages;
	}

	/**
	 * Gets all the messages for a player in a given group
	 *
	 * @param player The player to get the messages for.
	 * @param group  The grouping of messages to get.
	 * @return An {@link ArrayList} of messages that contains all the messages
	 * <br/ >sent since the player has been offline.
	 */
	public ArrayList<String> getMessages(final String player, final Group group) {
		final ArrayList<String> playerMessages = new ArrayList<String>();

		if (messages.containsKey(player)) {
			final HashMap<Group, PendingMessages> groupedMessages = messages.get(player);

			if (groupedMessages.containsKey(group)) {
				for (final Message message : groupedMessages.get(group)) {
					playerMessages.add(message.getMessage());
				}
			}
		}

		return playerMessages;
	}

	/**
	 * Returns the number of pending messages there are for a player.
	 *
	 * @param player The player.
	 * @return The number of pending messages there are for the player.
	 */
	public int getNumberOfMessages(final String player) {
		final ArrayList<String> messages = getMessages(player);

		return messages.size();
	}

	/**
	 * Returns the number of pending messages in the given group for a player.
	 *
	 * @param player The player.
	 * @param group  The group.
	 * @return The number of pending messages in the given group for the player.
	 */
	public int getNumberOfMessages(final String player, final Group group) {
		final ArrayList<String> messages = getMessages(player, group);

		return messages.size();
	}

	/**
	 * Removes all the pending messages for the given player name.
	 *
	 * @param player The name of the player to be removed.
	 */
	public void removePlayerMessages(final String player) {
		messages.remove(player);
	}

	/**
	 * Removes the given grouping of messages from all players.
	 *
	 * @param group The {@link Group} to be removed.
	 */
	public void removeGroup(final Group group) {
		messages.remove(group);
	}

	/**
	 * Removes the given grouping of messages from the given player.
	 *
	 * @param player The player to remove the group from.
	 * @param group  The group to remove.
	 */
	public void removeGroupFromPlayer(final String player, final Group group) {
		messages.remove(player, group);
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append(FormattingUtil.addTabsToNewLines("Message Manager\nMessages", 1));
		for (final Entry<String, GroupMessages> players : messages.entrySet()) {
			str.append(FormattingUtil.addTabsToNewLines("\nPlayer: " + players.getKey(), 2));
			for (final Entry<Group, PendingMessages> groupedMessages : players.getValue().entrySet()) {
				str.append(FormattingUtil.addTabsToNewLines("\n" + groupedMessages.getKey(), 3));
				for (final Message message : groupedMessages.getValue()) {
					str.append(FormattingUtil.addTabsToNewLines("\n" + message, 4));
				}
			}
		}
		return str.toString();
	}
}
