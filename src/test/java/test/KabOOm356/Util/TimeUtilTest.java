package test.KabOOm356.Util;

import static org.junit.Assert.*;
import net.KabOOm356.Util.TimeUtil;

import org.junit.Test;

public class TimeUtilTest {

	@Test
	public void testGetMinutes() {
		assertEquals(0, TimeUtil.getMinutes(0));
		assertEquals(0, TimeUtil.getMinutes(59));
		assertEquals(1, TimeUtil.getMinutes(TimeUtil.secondsPerMinute));
		assertEquals(1, TimeUtil.getMinutes(60));
		assertEquals(1, TimeUtil.getMinutes(61));
		assertEquals(2, TimeUtil.getMinutes(2 * TimeUtil.secondsPerMinute));
		assertEquals(10, TimeUtil.getMinutes(600));
		assertEquals(10, TimeUtil.getMinutes(630));
	}

	@Test
	public void testGetHours() {
		assertEquals(0, TimeUtil.getHours(0));
		assertEquals(0, TimeUtil.getHours(59));
		assertEquals(0, TimeUtil.getHours(601));
		assertEquals(1, TimeUtil.getHours(TimeUtil.secondsPerHour));
		assertEquals(1, TimeUtil.getHours(3660));
		assertEquals(1, TimeUtil.getHours(3661));
		assertEquals(2, TimeUtil.getHours(2 * TimeUtil.secondsPerHour));
		assertEquals(10, TimeUtil.getHours(36000));
		assertEquals(10, TimeUtil.getHours(36630));
	}

}
