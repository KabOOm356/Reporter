package net.KabOOm356.Manager.Messager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.KabOOm356.Manager.Messager.Messages.Message;

/**
 * A {@link HashMap} that uses a player name for the key and points to a {@link GroupMessages} object.
 */
public class PlayerMessages extends HashMap<String, GroupMessages>
{
	/**
	 * Generated Serial ID.
	 */
	private static final long serialVersionUID = -7504179174406586207L;

	/**
	 * Constructor.
	 */
	public PlayerMessages()
	{
		super();
	}
	
	/**
	 * Adds a message to the given player.
	 * <br />
	 * The message is added to the {@link Group#DEFAULT} grouping.
	 * 
	 * @param player The player to add the message to.
	 * @param message The message to add to the player.
	 */
	public void put(String player, Message message)
	{
		put(player, Group.DEFAULT, message);
	}
	
	/**
	 * Adds the given message to the given group to the given player.
	 * 
	 * @param player The player to add the message and group to.
	 * @param group The group to add to the player.
	 * @param message The message to add to the group.
	 */
	public void put(String player, Group group, Message message)
	{
		if(!containsKey(player))
			put(player, new GroupMessages(group, message));
		else
			get(player).put(group, message);
	}
	
	@Override
	public GroupMessages remove(Object key)
	{
		GroupMessages removed = super.remove(key);
		
		removeEmpty();
		
		return removed;
	}
	
	/**
	 * Removes the given group from the given player.
	 * 
	 * @param player The player to remove the group from.
	 * @param group The group to remove.
	 */
	public void remove(String player, Group group)
	{
		if(this.containsKey(player))
		{
			GroupMessages messages = get(player);
			
			if(messages.containsKey(group))
				messages.remove(group);
		}
		
		removeEmpty();
	}
	
	/**
	 * Removes the given message from players.
	 * 
	 * @param message The message to remove.
	 */
	public void remove(Message message)
	{
		for(GroupMessages messages : this.values())
			messages.remove(message);
		
		removeEmpty();
	}
	
	/**
	 * Removes the given {@link Group} from all players.
	 * 
	 * @param group The group to remove.
	 */
	public void remove(Group group)
	{
		for(GroupMessages messages : this.values())
			messages.remove(group);
		
		removeEmpty();
	}
	
	/**
	 * Re-indexes all the remaining indexes after a batch deletion.
	 * 
	 * @param remainingIndexes The remaining indexes after a batch deletion.
	 */
	public void reindexMessages(ArrayList<Integer> remainingIndexes)
	{
		for(Entry<String, GroupMessages> messages : this.entrySet())
		{
			GroupMessages groupMessages = messages.getValue();
			
			groupMessages.reindexMessages(remainingIndexes);
		}
		
		removeEmpty();
	}
	
	/**
	 * Removes all instances of the given index, and re-indexes all the remaining indexes.
	 * 
	 * @param index The index to remove.
	 */
	public void removeIndex(int index)
	{
		for(GroupMessages messages : this.values())
			messages.removeIndex(index);
		
		removeEmpty();
	}
	
	private void removeEmpty()
	{
		ArrayList<String> removalKeys = new ArrayList<String>();
		
		for(Entry<String, GroupMessages> messages: entrySet())
		{
			if(messages.getValue().isEmpty())
				removalKeys.add(messages.getKey());
		}
		
		for(String key : removalKeys)
			this.remove(key);
	}
	
	@Override
	public boolean isEmpty()
	{
		if(!super.isEmpty())
		{
			for(GroupMessages message : values())
			{
				if(!message.isEmpty())
					return false;
			}
		}
		
		return true;
	}
}
