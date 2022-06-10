package net.KabOOm356.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.KabOOm356.Service.Messager.Group;
import net.KabOOm356.Service.Messager.GroupMessages;
import net.KabOOm356.Service.Messager.Messages.Message;
import net.KabOOm356.Service.Messager.Messages.ReporterMessage;
import net.KabOOm356.Service.Messager.Messages.SimpleMessage;
import net.KabOOm356.Service.Messager.PendingMessages;
import net.KabOOm356.Service.Messager.PlayerMessages;
import net.KabOOm356.Util.FormattingUtil;

/** A class to manage messaging players that are offline. */
public class PlayerMessageService extends Service {
  /** Constructor. */
  protected PlayerMessageService(final ServiceModule module) {
    super(module);
  }

  /**
   * Adds a message for the given player.
   *
   * @param player The player.
   * @param message The message to send to the player.
   */
  public void addMessage(final String player, final String message) {
    getMessages().put(player, Group.DEFAULT, new SimpleMessage(message));
  }

  /**
   * Adds a message for the given player.
   *
   * @param player The player.
   * @param message The message to send to the player.
   * @param index The report index the message is referring to.
   */
  public void addMessage(final String player, final String message, final int index) {
    addMessage(player, Group.DEFAULT, message, index);
  }

  /**
   * Adds a message for the given player in the given grouping.
   *
   * @param player The player.
   * @param group The {@link Group} the message belongs to.
   * @param message The message to send to the player.
   */
  public void addMessage(
      final String player, final Group group, final String message, final int index) {
    getMessages().put(player, group, new ReporterMessage(message, index));
  }

  /**
   * Re-indexes the report indexes. <br>
   * This is called when groups of reports are deleted.
   *
   * @param remainingIndexes The remaining indexes in the database.
   */
  public void reindexMessages(final List<Integer> remainingIndexes) {
    getMessages().reindexMessages(remainingIndexes);
  }

  /**
   * Checks if the given player has any pending messages.
   *
   * @param player The player.
   * @return True if the given player has any pending messages, otherwise false.
   */
  public boolean hasMessages(final String player) {
    return getMessages().containsKey(player);
  }

  /**
   * Checks if the given player has any pending messages in the given group
   *
   * @param player The player.
   * @param group The grouping of messages to check for.
   * @return True if the given player has any pending messages in the given grouping, otherwise
   *     false.
   */
  public boolean hasGroup(final String player, final Group group) {
    return hasMessages(player) && getMessages().get(player).containsKey(group);
  }

  /**
   * Removes a report index from all places where the report index is referenced. <br>
   * <br>
   * <b>NOTE:</b> This also re-indexes all the other indexes.
   *
   * @param index The report index to remove.
   */
  public void removeMessage(final int index) {
    getMessages().removeIndex(index);
  }

  /** Removes all the messages currently pending. */
  public void removeAll() {
    getMessages().clear();
  }

  /**
   * Gets all the pending messages for the given player.
   *
   * @param player The player.
   * @return An {@link ArrayList} of messages that contains all the messages <br>
   *     sent since the player has been offline.
   */
  public List<String> getMessages(final String player) {
    final List<String> playerMessages = new ArrayList<>();

    if (getMessages().containsKey(player)) {
      for (final Entry<Group, PendingMessages> e : getMessages().get(player).entrySet()) {
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
   * @param group The grouping of messages to get.
   * @return An {@link ArrayList} of messages that contains all the messages <br>
   *     sent since the player has been offline.
   */
  public List<String> getMessages(final String player, final Group group) {
    final List<String> playerMessages = new ArrayList<>();

    if (getMessages().containsKey(player)) {
      final Map<Group, PendingMessages> groupedMessages = getMessages().get(player);

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
    final List<String> messages = getMessages(player);

    return messages.size();
  }

  /**
   * Returns the number of pending messages in the given group for a player.
   *
   * @param player The player.
   * @param group The group.
   * @return The number of pending messages in the given group for the player.
   */
  public int getNumberOfMessages(final String player, final Group group) {
    final List<String> messages = getMessages(player, group);

    return messages.size();
  }

  /**
   * Removes all the pending messages for the given player name.
   *
   * @param player The name of the player to be removed.
   */
  public void removePlayerMessages(final String player) {
    getMessages().remove(player);
  }

  /**
   * Removes the given grouping of messages from all players.
   *
   * @param group The {@link Group} to be removed.
   */
  public void removeGroup(final Group group) {
    getMessages().remove(group);
  }

  /**
   * Removes the given grouping of messages from the given player.
   *
   * @param player The player to remove the group from.
   * @param group The group to remove.
   */
  public void removeGroupFromPlayer(final String player, final Group group) {
    getMessages().remove(player, group);
  }

  private PlayerMessages getMessages() {
    return getStore().getPlayerMessagesStore().get();
  }

  @Override
  public String toString() {
    final StringBuilder str = new StringBuilder();
    str.append(FormattingUtil.addTabsToNewLines("Message Service\nMessages", 1));
    for (final Entry<String, GroupMessages> players : getMessages().entrySet()) {
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
