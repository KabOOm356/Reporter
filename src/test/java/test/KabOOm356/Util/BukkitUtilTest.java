package test.KabOOm356.Util;

import static org.junit.Assert.*;

import net.KabOOm356.Util.BukkitUtil;

import org.junit.Test;

public class BukkitUtilTest
{
	@Test
	public void testIsUsernameValid()
	{
		String username = "*";
		assertFalse(BukkitUtil.isUsernameValid(username));
		username = "**";
		assertFalse(BukkitUtil.isUsernameValid(username));
		
		username = "!";
		assertFalse(BukkitUtil.isUsernameValid(username));
		username = "!!";
		assertFalse(BukkitUtil.isUsernameValid(username));
		
		username = "a";
		assertFalse(BukkitUtil.isUsernameValid(username));
		username = "A";
		assertFalse(BukkitUtil.isUsernameValid(username));
		username = "_";
		assertFalse(BukkitUtil.isUsernameValid(username));
		username = "0";
		assertFalse(BukkitUtil.isUsernameValid(username));
		
		username = "ab";
		assertTrue(BukkitUtil.isUsernameValid(username));
		username = "AB";
		assertTrue(BukkitUtil.isUsernameValid(username));
		username = "Ab";
		assertTrue(BukkitUtil.isUsernameValid(username));
		username = "ab_";
		assertTrue(BukkitUtil.isUsernameValid(username));
		username = "ab0";
		assertTrue(BukkitUtil.isUsernameValid(username));
		username = "aB_";
		assertTrue(BukkitUtil.isUsernameValid(username));
		username = "aB0";
		assertTrue(BukkitUtil.isUsernameValid(username));
		username = "abcdefghijklmnopqrst0123456789_";
		assertTrue(BukkitUtil.isUsernameValid(username));
		
		// Username is too long.
		username = "abcdefghijklmnopqrstuvwxyz0123456789_";
		assertFalse(BukkitUtil.isUsernameValid(username));
	}
}
