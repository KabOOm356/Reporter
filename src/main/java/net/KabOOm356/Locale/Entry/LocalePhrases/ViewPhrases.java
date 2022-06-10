package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by {@link
 * net.KabOOm356.Command.Commands.ViewCommand}.
 */
public abstract class ViewPhrases {
  /** Report summary: */
  public static final LocalePhrase viewCompletedSummary =
      new LocalePhrase("viewCompletedSummary", "Report summary:");

  /** Uncompleted */
  public static final LocalePhrase viewUnfinished =
      new LocalePhrase("viewUnfinished", "Uncompleted");

  /** Completed */
  public static final LocalePhrase viewFinished = new LocalePhrase("viewFinished", "Completed");

  /** Report completed on: */
  public static final LocalePhrase viewCompletedOn =
      new LocalePhrase("viewCompletedOn", "Report completed on:");

  /** Report completed by: */
  public static final LocalePhrase viewCompletedBy =
      new LocalePhrase("viewCompletedBy", "Report completed by:");

  /** Report status: */
  public static final LocalePhrase viewStatus = new LocalePhrase("viewStatus", "Report status:");

  /** Report Summary */
  public static final LocalePhrase viewSummaryTitle =
      new LocalePhrase("viewSummaryTitle", "Report Summary");

  /** Report submission date: */
  public static final LocalePhrase viewDate =
      new LocalePhrase("viewDate", "Report submission date:");

  /** Report Details: */
  public static final LocalePhrase viewDetails = new LocalePhrase("viewDetails", "Report Details:");

  /** Player Reported: */
  public static final LocalePhrase viewReported =
      new LocalePhrase("viewReported", "Player Reported:");

  /** Report submitted by: */
  public static final LocalePhrase viewSender =
      new LocalePhrase("viewSender", "Report submitted by:");

  /** Begin Report %i */
  public static final LocalePhrase viewBegin = new LocalePhrase("viewBegin", "Begin Report %i");

  /** Report %i: %s reported %r */
  public static final LocalePhrase viewAllReportHeader =
      new LocalePhrase("viewAllReportHeader", "Report %i: %s reported %r");

  /** Your Claimed Reports */
  public static final LocalePhrase viewYourClaimedReportsHeader =
      new LocalePhrase("viewYourClaimedReportsHeader", "Your Claimed Reports");

  /** %p Priority Reports */
  public static final LocalePhrase viewPriorityHeader =
      new LocalePhrase("viewPriorityHeader", "%p Priority Reports");

  /** Details: %d */
  public static final LocalePhrase viewAllReportDetails =
      new LocalePhrase("viewAllReportDetails", "Details: %d");

  /** There are no reports to view! */
  public static final LocalePhrase noReportsToView =
      new LocalePhrase("noReportsToView", "There are no reports to view!");

  /** Completed Reports */
  public static final LocalePhrase viewAllCompleteHeader =
      new LocalePhrase("viewAllCompleteHeader", "Completed Reports");

  /** Unfinished Reports */
  public static final LocalePhrase viewAllUnfinishedHeader =
      new LocalePhrase("viewAllUnfinishedHeader", "Unfinished Reports");

  /** All Reports */
  public static final LocalePhrase viewAllBeginHeader =
      new LocalePhrase("viewAllBeginHeader", "All Reports");

  /** Claimed status: */
  public static final LocalePhrase viewClaimHeader =
      new LocalePhrase("viewClaimHeader", "Claimed status:");

  /** Claimed */
  public static final LocalePhrase viewStatusClaimed =
      new LocalePhrase("viewStatusClaimed", "Claimed");

  /** Unclaimed */
  public static final LocalePhrase viewStatusUnclaimed =
      new LocalePhrase("viewStatusUnclaimed", "Unclaimed");

  /** Claimed by: */
  public static final LocalePhrase viewClaimedBy = new LocalePhrase("viewClaimedBy", "Claimed by:");

  /** Claimed on: */
  public static final LocalePhrase viewClaimedOn = new LocalePhrase("viewClaimedOn", "Claimed on:");

  /** Completion status: */
  public static final LocalePhrase viewCompletionStatus =
      new LocalePhrase("viewCompletionStatus", "Completion status:");

  /** No summary given */
  public static final LocalePhrase viewNoSummary =
      new LocalePhrase("viewNoSummary", "No summary given");

  /** /report view Index/last [name] */
  public static final LocalePhrase viewHelp =
      new LocalePhrase("viewHelp", "/report view <Index/last> [name]");

  /** Views a report. Optional parameter name will display the player's real names. */
  public static final LocalePhrase viewHelpDetails =
      new LocalePhrase(
          "viewHelpDetails",
          "Views a report. Optional parameter name will display the player's real names.");

  /** Views all reports. */
  public static final LocalePhrase viewHelpAllDetails =
      new LocalePhrase("viewHelpAllDetails", "Views all reports.");

  /** Views all completed reports. */
  public static final LocalePhrase viewHelpCompletedDetails =
      new LocalePhrase("viewHelpCompletedDetails", "Views all completed reports.");

  /** Views all incomplete reports. */
  public static final LocalePhrase viewHelpIncompleteDetails =
      new LocalePhrase("viewHelpIncompleteDetails", "Views all incomplete reports.");

  /** Views all reports sorted by priority. */
  public static final LocalePhrase viewHelpPriorityDetails =
      new LocalePhrase("viewHelpPriorityDetails", "Views all reports sorted by priority.");

  /** /report view priority [Priority] [name] */
  public static final LocalePhrase viewHelpGivenPriority =
      new LocalePhrase("viewHelpGivenPriority", "/report view priority <Priority> [name]");

  /** Views all reports that have the given priority. */
  public static final LocalePhrase viewHelpGivenPriorityDetails =
      new LocalePhrase(
          "viewHelpGivenPriorityDetails", "Views all reports that have the given priority.");

  /** Views all reports claimed by you. */
  public static final LocalePhrase viewHelpClaimedDetails =
      new LocalePhrase("viewHelpClaimedDetails", "Views all reports claimed by you.");

  /** /report view claimed priority [Priority] [name] */
  public static final LocalePhrase viewHelpClaimedGivenPriority =
      new LocalePhrase(
          "viewHelpClaimedGivenPriority", "/report view claimed priority <Priority> [name]");

  /** Views all reports claimed by you, that have the given priority. */
  public static final LocalePhrase viewHelpClaimedPriorityDetails =
      new LocalePhrase(
          "viewHelpClaimedPriorityDetails",
          "Views all reports claimed by you, that have the given priority.");
  /** Report Priority: */
  public static final LocalePhrase viewPriority =
      new LocalePhrase("viewPriority", "Report Priority:");
}
