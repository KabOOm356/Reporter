package test.KabOOm356.Manager;

import static org.junit.Assert.*;

import java.util.ArrayList;

import net.KabOOm356.Manager.SQLStatManager.SQLStat;
import net.KabOOm356.Manager.SQLStatManagers.ModeratorStatManager.ModeratorStat;
import net.KabOOm356.Manager.SQLStatManagers.PlayerStatManager.PlayerStat;

import org.junit.Test;

public class SQLStatTest
{
	@Test
	public void testGetByName()
	{
		SQLStat stat = SQLStat.ALL;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		
		stat = ModeratorStat.ASSIGNED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = ModeratorStat.CLAIMED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = ModeratorStat.COMPLETED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = ModeratorStat.DELETED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = ModeratorStat.MOVED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = ModeratorStat.RESPONDED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = ModeratorStat.UNASSIGNED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = ModeratorStat.UNCLAIMED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		
		stat = PlayerStat.FIRSTREPORTDATE;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = PlayerStat.FIRSTREPORTEDDATE;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = PlayerStat.LASTREPORTDATE;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = PlayerStat.LASTREPORTEDDATE;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = PlayerStat.REPORTCOUNT;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
		stat = PlayerStat.REPORTED;
		assertEquals(stat, SQLStat.getByName(stat.getName()));
	}
	
	@Test
	public void testGetAll()
	{
		ArrayList<SQLStat> stats = SQLStat.getAll(SQLStat.class);
		
		assertEquals(1, stats.size());
		assertTrue(stats.contains(SQLStat.ALL));
		
		stats = SQLStat.getAll(ModeratorStat.class);
		
		assertTrue(stats.contains(ModeratorStat.ASSIGNED));
		assertTrue(stats.contains(ModeratorStat.CLAIMED));
		assertTrue(stats.contains(ModeratorStat.COMPLETED));
		assertTrue(stats.contains(ModeratorStat.DELETED));
		assertTrue(stats.contains(ModeratorStat.MOVED));
		assertTrue(stats.contains(ModeratorStat.RESPONDED));
		assertTrue(stats.contains(ModeratorStat.UNASSIGNED));
		assertTrue(stats.contains(ModeratorStat.UNCLAIMED));
		
		stats = SQLStat.getAll(PlayerStat.class);
		
		assertTrue(stats.contains(PlayerStat.FIRSTREPORTDATE));
		assertTrue(stats.contains(PlayerStat.FIRSTREPORTEDDATE));
		assertTrue(stats.contains(PlayerStat.LASTREPORTDATE));
		assertTrue(stats.contains(PlayerStat.LASTREPORTEDDATE));
		assertTrue(stats.contains(PlayerStat.REPORTCOUNT));
		assertTrue(stats.contains(PlayerStat.REPORTED));
	}
}
