package net.KabOOm356.Util;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilTest {
  @Test
  public void testCountOccurrences() {
    final String str = "This is a string.\nThis is a new line!";

    assertEquals(0, Util.countOccurrences(str, 'z'));
    assertEquals(0, Util.countOccurrences(str, '1'));
    assertEquals(0, Util.countOccurrences(str, 'A'));

    assertEquals(1, Util.countOccurrences(str, '\n'));
    assertEquals(1, Util.countOccurrences(str, '.'));
    assertEquals(1, Util.countOccurrences(str, '!'));
    assertEquals(7, Util.countOccurrences(str, ' '));
    assertEquals(2, Util.countOccurrences(str, 'a'));
  }

  @Test
  public void testParseInt() {
    String stringValue;

    // Test to parse values between -1000 and 1000.
    for (Integer LCV = -1000; LCV <= 1000; LCV++) {
      stringValue = LCV.toString();

      assertEquals(LCV, Util.parseInt(stringValue));
    }
  }

  @Test
  public void testIsInteger() {
    String stringValue;

    // Test values between -1000 and 1000.
    for (int LCV = -1000; LCV <= 1000; LCV++) {
      stringValue = Integer.toString(LCV);

      assertTrue(Util.isInteger(stringValue));
    }

    // Test ASCII characters.
    for (int LCV = 32;
        LCV <= 126;
        LCV++) // Loop through the ASCII characters. Space(' ') to tilde('~').
    {
      stringValue = Character.toString((char) LCV);

      if (LCV < 48 || LCV > 57) // If the ASCII value is outside of the numeric characters.
      {
        assertFalse(Util.isInteger(stringValue));
      } else // If the ASCII value is a numeric (0-9).
      {
        assertTrue(Util.isInteger(stringValue));
      }
    }
  }

  @Test
  public void testEndsWithIgnoreCase() {
    assertTrue(Util.endsWithIgnoreCase("", ""));
    assertTrue(Util.endsWithIgnoreCase("testing", "testing"));
    assertTrue(Util.endsWithIgnoreCase("testing", "TESTING"));
    assertTrue(Util.endsWithIgnoreCase("ANOTHER TEST", "test"));
    assertTrue(Util.endsWithIgnoreCase("testing", "ING"));
    assertTrue(Util.endsWithIgnoreCase("TeStIng", "EsTing"));

    assertFalse(Util.endsWithIgnoreCase("TESTING", "no"));
    assertFalse(Util.endsWithIgnoreCase("", "testing"));
  }

  @Test
  public void testStartsWithIgnoreCase() {
    assertTrue(Util.startsWithIgnoreCase("", ""));
    assertTrue(Util.startsWithIgnoreCase("testing", "testing"));
    assertTrue(Util.startsWithIgnoreCase("testing", "TESTING"));
    assertTrue(Util.startsWithIgnoreCase("ANOTHER TEST", "another"));
    assertTrue(Util.startsWithIgnoreCase("testing", "TEST"));
    assertTrue(Util.startsWithIgnoreCase("TeSting", "TeStIn"));

    assertFalse(Util.startsWithIgnoreCase("TESTING", "no"));
    assertFalse(Util.startsWithIgnoreCase("more testing", "no More"));
    assertFalse(Util.startsWithIgnoreCase("", "testing"));
  }
}
