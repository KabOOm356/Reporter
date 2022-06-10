package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by {@link
 * net.KabOOm356.Command.Commands.RespondCommand}.
 */
public abstract class RespondPhrases {
  /** Teleporting to the reported player's location */
  public static final LocalePhrase telReported =
      new LocalePhrase("telReported", "Teleporting to the reported player's location");

  /** Teleporting to the report sender's location */
  public static final LocalePhrase telSender =
      new LocalePhrase("telSender", "Teleporting to the report sender's location");

  /** Both sender and reported players locations could not be found! */
  public static final LocalePhrase bothPlayerLocNF =
      new LocalePhrase(
          "bothPlayerLocNF", "Both sender and reported players locations could not be found!");

  /** Aborting Teleport */
  public static final LocalePhrase teleAbort = new LocalePhrase("teleAbort", "Aborting Teleport");

  /** Reported players location could not be found! */
  public static final LocalePhrase reportedPlayerLocNF =
      new LocalePhrase("reportedPlayerLocNF", "Reported players location could not be found!");

  /** Sender's location could not be found! */
  public static final LocalePhrase senderLocNF =
      new LocalePhrase("senderLocNF", "Sender's location could not be found!");

  /** You are at location (World, X, Y, Z): %world, %x, %y, %z */
  public static final LocalePhrase respondTeleportLocation =
      new LocalePhrase(
          "respondTeleportLocation", "You are at location (World, X, Y, Z): %world, %x, %y, %z");

  /** /respond Index/last [reported/sender] */
  public static final LocalePhrase respondHelp =
      new LocalePhrase("respondHelp", "/respond <Index/last> [reported/sender]");

  /**
   * Teleports to the location of the report defaults to the reported player's location, if
   * reported/sender is not specified.
   */
  public static final LocalePhrase respondHelpDetails =
      new LocalePhrase(
          "respondHelpDetails",
          "Teleports to the location of the report defaults to the reported player's location, if reported/sender is not specified.");
}
