package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

public abstract class StatisticPhrases {
  /** /report statistic/stat Player Name Statistic */
  public static final LocalePhrase statisticHelp =
      new LocalePhrase("statisticHelp", "/report statistic/stat <Player Name> <Statistic>");

  /** Gets a statistic for the given player. */
  public static final LocalePhrase statisticHelpDetails =
      new LocalePhrase("statisticHelpDetails", "Gets a statistic for the given player.");

  /** Lists all available statistics. */
  public static final LocalePhrase statisticListHelpDetails =
      new LocalePhrase("statisticListHelpDetails", "Lists all available statistics.");

  /** The given statistic is not valid. */
  public static final LocalePhrase notValidStatistic =
      new LocalePhrase("notValidStatistic", "The given statistic is not valid.");

  /** Displays all statistics for the given player. */
  public static final LocalePhrase statisticAllHelpDetails =
      new LocalePhrase("statisticAllHelpDetails", "Displays all statistics for the given player.");

  /** Displays all mod or player statistics for the given player. */
  public static final LocalePhrase statisticAllModPlayerHelpDetails =
      new LocalePhrase(
          "statisticAllModPlayerHelpDetails",
          "Displays all mod or player statistics for the given player.");

  /** The player %p does not have an entry for the statistic %s! */
  public static final LocalePhrase noStatisticEntry =
      new LocalePhrase(
          "noStatisticEntry", "The player %p does not have an entry for the statistic %s!");

  /** The %s statistic for player %p is %v */
  public static final LocalePhrase displayStatistic =
      new LocalePhrase("displayStatistic", "The %s statistic for player %p is %v");

  /** Available Player Statistics: %s */
  public static final LocalePhrase availablePlayerStatistics =
      new LocalePhrase("availablePlayerStatistics", "Available Player Statistics: %s");

  /** Available Moderator Statistics: %s */
  public static final LocalePhrase availableModeratorStatistics =
      new LocalePhrase("availableModeratorStatistics", "Available Moderator Statistics: %s");

  /** Try /report statistic list */
  public static final LocalePhrase tryStatisticList =
      new LocalePhrase("tryStatisticList", "Try /report statistic list");
}
