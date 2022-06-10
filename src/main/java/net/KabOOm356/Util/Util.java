package net.KabOOm356.Util;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** General utility helper class. */
public final class Util {
  private static final Logger log = LogManager.getLogger(Util.class);

  private Util() {}

  /**
   * Counts the number of times a character occurs in a string.
   *
   * @param str The string.
   * @param character The character to look for.
   * @return The number of times a character occurs in the string.
   */
  public static int countOccurrences(final String str, final char character) {
    final String c = Character.toString(character);
    return countOccurrences(str, c);
  }

  /**
   * Counts the number of times a substring occurs in a string.
   *
   * @param str The string.
   * @param needle The String to look for.
   * @return The number of times a character occurs in the string.
   */
  public static int countOccurrences(final String str, final String needle) {
    Validate.notNull(str, "Parameter 'str' cannot be null!");
    Validate.notNull(needle, "Parameter 'needle' cannot be null!");

    // If the string does not contain the character return zero.
    if (!str.contains(needle)) {
      return 0;
    }

    int it = 0;
    int count = 0;

    while (it != str.lastIndexOf(needle)) {
      it = str.indexOf(needle, it + 1);
      count++;
    }

    return count;
  }

  /**
   * Checks if the given String is an Integer.
   *
   * @param s The String to check if is an Integer.
   * @return True if the String can be parsed to an Integer, otherwise false.
   */
  public static boolean isInteger(final String s) {
    return parseInt(s) != null;
  }

  /**
   * Parses a String into an int.
   *
   * @param str The String to parse into an int.
   * @return If an integer can be parsed from the string that integer is returned, otherwise null.
   */
  public static Integer parseInt(final String str) {
    try {
      return Integer.parseInt(str);
    } catch (final Exception e) {
      log.debug(String.format("Failed to parse integer from string [%s]!", str), e);
      return null;
    }
  }

  /**
   * Checks if the given String ends with the given suffix, ignoring case.
   *
   * @param str The String to check.
   * @param suffix The suffix to check the String for.
   * @return True if the given String ends with the given suffix, otherwise false.
   */
  public static boolean endsWithIgnoreCase(final String str, final String suffix) {
    Validate.notNull(str, "Parameter 'str' cannot be null!");
    Validate.notNull(suffix, "Parameter 'suffix' cannot be null!");

    if (str.length() < suffix.length()) {
      return false;
    }

    final String ending = str.substring(str.length() - suffix.length());
    return ending.equalsIgnoreCase(suffix);
  }

  /**
   * Checks if the given String begins with the given prefix, ignoring case.
   *
   * @param str The String to check.
   * @param prefix The prefix to check the String for.
   * @return True if the given String begins with the given prefix, otherwise false.
   */
  public static boolean startsWithIgnoreCase(final String str, final String prefix) {
    Validate.notNull(str, "Parameter 'str' cannot be null!");
    Validate.notNull(prefix, "Parameter 'prefix' cannot be null!");

    if (str.length() < prefix.length()) {
      return false;
    }

    final int offset = (prefix.length() + 1 <= str.length()) ? prefix.length() : str.length();
    final String beginning = str.substring(0, offset);
    return beginning.equalsIgnoreCase(prefix);
  }
}
