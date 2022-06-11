package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by {@link
 * net.KabOOm356.Command.Commands.ReportCommand}.
 */
public abstract class ReportPhrases {
  /** Report submitted. Thank you. */
  public static final LocalePhrase playerReport =
      new LocalePhrase("playerReport", "Report submitted. Thank you.");

  /** You have reached the reporting limit! */
  public static final LocalePhrase reachedReportingLimit =
      new LocalePhrase("reachedReportingLimit", "You have reached the reporting limit!");

  /** You must wait %h hours, %m minutes and %s seconds before reporting again! */
  public static final LocalePhrase remainingTimeForReport =
      new LocalePhrase(
          "remainingTimeForReport",
          "You must wait %h hours, %m minutes and %s seconds before reporting again!");

  /** A report has just been submitted at index: %i */
  public static final LocalePhrase broadcastSubmitted =
      new LocalePhrase("broadcastSubmitted", "A report has just been submitted at index: %i");

  /** /report player/!/* details */
  public static final LocalePhrase reportHelp =
      new LocalePhrase("reportHelp", "/report <player/!/*> <details>");

  /** Reports a player with the details of the offense. ! and * if the player name is not known. */
  public static final LocalePhrase reportHelpDetails =
      new LocalePhrase(
          "reportHelpDetails",
          "Reports a player with the details of the offense.  ! and * if the player name is not known.");

  /** You are now allowed to report again! */
  public static final LocalePhrase allowedToReportAgain =
      new LocalePhrase("allowedToReportAgain", "You are now allowed to report again!");

  /** You are now allowed to report %r again! */
  public static final LocalePhrase allowedToReportPlayerAgain =
      new LocalePhrase("allowedToReportPlayerAgain", "You are now allowed to report %r again!");

  /** You have reached the reporting limit against %r! */
  public static final LocalePhrase reachedReportingLimitAgaintPlayer =
      new LocalePhrase(
          "reachedReportingLimitAgaintPlayer", "You have reached the reporting limit against %r!");

  /** You must wait %h hours, %m minutes and %s seconds before reporting %r again! */
  public static final LocalePhrase remainingTimeToReportPlayer =
      new LocalePhrase(
          "remainingTimeToReportPlayer",
          "You must wait %h hours, %m minutes and %s seconds before reporting %r again!");
}
