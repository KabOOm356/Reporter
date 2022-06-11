package net.KabOOm356.Util;

/** Utility class to handle time conversions. */
public final class TimeUtil {
  public static final int secondsPerMinute = 60;
  public static final int secondsPerHour = 3600;

  private TimeUtil() {}

  /**
   * Returns the number of minutes in the given seconds.
   *
   * @param seconds The number of seconds.
   * @return The number of minutes in the given seconds.
   */
  public static int getMinutes(final int seconds) {
    return convert(seconds, secondsPerMinute);
  }

  /**
   * Returns the number of hours in the given seconds.
   *
   * @param seconds The number of seconds.
   * @return The number of hours in the given seconds.
   */
  public static int getHours(final int seconds) {
    return convert(seconds, secondsPerHour);
  }

  private static int convert(final int seconds, final int perSeconds) {
    if (seconds == 0) {
      return 0;
    }
    return (int) Math.ceil(seconds / perSeconds);
  }
}
