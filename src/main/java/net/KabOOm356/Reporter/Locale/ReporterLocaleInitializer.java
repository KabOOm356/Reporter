package net.KabOOm356.Reporter.Locale;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import net.KabOOm356.File.AbstractFiles.UpdateSite;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.File.RevisionFile;
import net.KabOOm356.Locale.ConstantsLocale;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Updater.LocaleUpdater;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xml.sax.SAXException;

/**
 * A {@link Runnable} that when run will initialize the Locale file to be used. <br>
 * This will download and update the locale file.
 */
public class ReporterLocaleInitializer implements Runnable {
  private static final Logger log = LogManager.getLogger(ReporterLocaleInitializer.class);

  private final Reporter plugin;
  private final String localeName;
  private final File dataFolder;
  private final boolean autoDownload;
  private final ReleaseLevel lowestLevel;
  private final boolean keepBackup;
  private final Locale locale;
  private boolean update = false;

  /**
   * Constructor.
   *
   * @param plugin The main {@link Reporter} instance.
   * @param localeName The name of the locale.
   * @param dataFolder The folder the locale should be in.
   * @param autoDownload If the locale should be automatically download and updated.
   * @param lowestLevel The lowest level of {@link ReleaseLevel} that should be downloaded or
   *     updated to.
   * @param keepBackup Whether the old locale file should be retained after a successful locale
   *     update.
   */
  public ReporterLocaleInitializer(
      final Reporter plugin,
      final String localeName,
      final File dataFolder,
      final boolean autoDownload,
      final ReleaseLevel lowestLevel,
      final boolean keepBackup) {
    this.plugin = plugin;

    this.locale = plugin.getLocale();

    this.localeName = localeName;
    this.dataFolder = dataFolder;
    this.autoDownload = autoDownload;
    this.lowestLevel = lowestLevel;
    this.keepBackup = keepBackup;
  }

  /**
   * Returns a file in the given dataFolder with a name of the given localeName.
   *
   * @param dataFolder The folder where the locale file should be.
   * @param localeName The name of the locale file.
   * @return A file in the given dataFolder with a name of the given localeName.
   */
  private static File getLocaleFile(final File dataFolder, String localeName) {
    if (localeName.contains(".")) {
      localeName = localeName.substring(0, localeName.indexOf('.'));
    }

    return new File(dataFolder, localeName + ConstantsLocale.LOCALE_FILE_EXTENSION);
  }

  @Override
  public void run() {
    // There should be no other action on the locale until the initialization/update is completed.
    synchronized (locale) {
      initLocale();

      // Notify other threads that the locale is initialized.
      locale.notify();
    }

    plugin.loadLocale();
  }

  /**
   * Initializes the locale file. <br>
   * <br>
   * <b>NOTE:</b> This also begins the download/update process.
   *
   * @return The initialized and up-to-date {@link YamlConfiguration} locale.
   */
  public Locale initLocale() {
    // No need to initialize the locale if it is already initialized.
    if (locale.isInitialized()) {
      return locale;
    }

    if (localeName.equalsIgnoreCase(ConstantsLocale.ENGLISH_LOCALE)) {
      locale.initialized();
      return locale;
    }

    File localeFile = getLocaleFile(dataFolder, localeName);

    boolean downloaded = false;

    if (autoDownload) {
      try {
        downloaded = downloadOrUpdate(localeFile);
      } catch (final Exception e) {
        log.error(
            Reporter.getDefaultConsolePrefix() + "Error downloading or updating the locale file!",
            e);
      }
    } else if (!localeFile.exists()) {
      log.warn(
          Reporter.getDefaultConsolePrefix()
              + "Locale file "
              + localeName
              + " does not exist locally!");
      log.warn(
          Reporter.getDefaultConsolePrefix()
              + "Try setting locale.updates.autoDownload to true in the configuration.");
      log.warn(Reporter.getDefaultConsolePrefix() + "Using English default.");
    }

    if (!localeFile.exists()) {
      localeFile = null;
    }

    final RevisionFile localeBackupFile =
        new RevisionFile(
            dataFolder,
            localeName
                + ConstantsLocale.LOCALE_FILE_EXTENSION
                + ConstantsLocale.BACKUP_FILE_EXTENSION);

    localeBackupFile.incrementToLatestRevision();

    try {
      if (localeFile != null) {
        log.info(
            Reporter.getDefaultConsolePrefix() + "Loading locale file: " + localeFile.getName());

        locale.load(localeFile);

        if (update && downloaded) {
          if (!keepBackup) {
            log.info(
                Reporter.getDefaultConsolePrefix()
                    + "Purging backup file "
                    + localeBackupFile.getFileName());
            localeBackupFile.delete();
          } else {
            log.info(
                Reporter.getDefaultConsolePrefix()
                    + "Retaining backup file "
                    + localeBackupFile.getFileName());
          }
        }
      }
    } catch (final Exception e) {
      log.log(
          Level.ERROR,
          Reporter.getDefaultConsolePrefix() + "There was an error loading " + localeFile.getName(),
          e);

      if (e.getMessage().contains(ConstantsLocale.CONVERT_LOCALE_EXCEPTION_MESSAGE)) {
        log.warn(
            Reporter.getDefaultConsolePrefix()
                + "Try converting the file to UTF-8 without BOM (Byte Order Marks) then try to reload it.");
      } else {
        log.warn(Reporter.getDefaultConsolePrefix() + "Please let the author know this.");
      }

      if (update) {
        this.restoreBackup(localeFile, localeBackupFile);
      } else {
        log.warn(Reporter.getDefaultConsolePrefix() + "Using English default.");
      }
    }

    locale.initialized();

    return locale;
  }

  /**
   * Initializes the updater that will download/update the file.
   *
   * @param localeFile The file the main locale resides.
   * @return A new {@link LocaleUpdater}.
   * @throws IOException
   */
  private LocaleUpdater initUpdater(final File localeFile) throws IOException {
    String localLocaleVersion = "1";
    try {
      final YamlConfiguration localLocale = YamlConfiguration.loadConfiguration(localeFile);
      localLocaleVersion = localLocale.getString("locale.info.version", "1");
    } catch (final Exception e) {
      log.log(Level.WARN, "Failed to pre-load locale file!", e);
    }

    final UpdateSite localeXMLUpdateSite = Reporter.getLocaleXMLUpdateSite();

    return new LocaleUpdater(localeXMLUpdateSite, localeName, localLocaleVersion, lowestLevel);
  }

  /**
   * Attempts to download or update the locale file. <br>
   * <br>
   * <b>NOTE:</b> If the file goes through an update the {@link ReporterLocaleInitializer#update}
   * flag is set to true. If the file only downloads {@link ReporterLocaleInitializer#update} flag
   * is set to false.
   *
   * @param localeFile The file the main locale resides.
   * @return True if the the file was successfully downloaded. <br>
   *     <br>
   *     <b>NOTE:</b> Downloaded means if the original file was overwritten.
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  private boolean downloadOrUpdate(final File localeFile)
      throws ParserConfigurationException, SAXException, IOException {
    final LocaleUpdater updater = initUpdater(localeFile);

    if (localeFile.exists()) {
      update = true;
      return updater.localeUpdateProcess(localeFile);
    } else {
      update = false;
      return updater.localeDownloadProcess(localeFile);
    }
  }

  /**
   * Attempts to restore the locale file from backups.
   *
   * @param localeFile The file the main locale resides.
   * @param localeBackupFile The backup {@link RevisionFile} for the locale.
   */
  private void restoreBackup(final File localeFile, final RevisionFile localeBackupFile) {
    if (!localeBackupFile.exists()) {
      log.warn(Reporter.getDefaultConsolePrefix() + "The backup file does not exist.");
      log.warn(Reporter.getDefaultConsolePrefix() + "Using English default.");
    } else {
      final boolean loadSuccessful = attemptToLoadBackups(localeFile, localeBackupFile);

      if (loadSuccessful) {
        log.info(
            Reporter.getDefaultConsolePrefix() + "Successfully restored and loaded backup file.");
      } else {
        localeFile.delete();
        log.warn(Reporter.getDefaultConsolePrefix() + "Failed to restore backups.");
        log.warn(Reporter.getDefaultConsolePrefix() + "Using English default.");
      }
    }
  }

  /**
   * Attempts to copy and load a backup locale file to the main locale file. If an error occurs when
   * loading the backup file, it is deleted and the next backup will attempt to load. This continues
   * until either a backup file is loaded successfully, or there are no more backups.
   *
   * @param localeFile The file the main locale resides.
   * @param localeBackupFile The backup {@link RevisionFile} for the locale.
   * @return True on successful load and restore of one of the backups, otherwise false.
   */
  private boolean attemptToLoadBackups(final File localeFile, final RevisionFile localeBackupFile) {
    boolean loadSuccessful = false;

    // Set the backup file to the latest revision.
    localeBackupFile.incrementToLatestRevision();

    do {
      log.info(
          Reporter.getDefaultConsolePrefix()
              + "Attempting to restore backup revision: "
              + localeBackupFile.getRevision());

      localeBackupFile.renameTo(localeFile);
      localeBackupFile.delete();

      try {
        locale.load(localeFile);
      } catch (final Exception e) // On error reset locale and decrement the backup revision.
      {
        log.warn(
            Reporter.getDefaultConsolePrefix()
                + "Failed to load backup revision: "
                + localeBackupFile.getRevision(),
            e);
        localeBackupFile.decrementRevision();
        loadSuccessful = false;
      }
    } while (!loadSuccessful && localeBackupFile.exists());

    return loadSuccessful;
  }
}
