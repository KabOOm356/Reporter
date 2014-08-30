package net.KabOOm356.Reporter.Locale;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.xml.sax.SAXException;

import net.KabOOm356.File.RevisionFile;
import net.KabOOm356.File.AbstractFiles.UpdateSite;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Updater.LocaleUpdater;

/**
 * A {@link Runnable} that when run will initialize the Locale file to be used.
 * <br/>
 * This will download and update the locale file.
 */
public class ReporterLocaleInitializer implements Runnable
{
	private Reporter plugin;
	private String localeName;
	private File dataFolder;
	private boolean autoDownload;
	private ReleaseLevel lowestLevel;
	private boolean keepBackup;
	
	private boolean update = false;
	
	private Locale locale;
	
	/**
	 * Constructor.
	 * 
	 * @param plugin The main {@link Reporter} instance.
	 * @param localeName The name of the locale.
	 * @param dataFolder The folder the locale should be in .
	 * @param autoDownload If the locale should be automatically download and updated.
	 * @param lowestLevel The lowest level of {@link ReleaseLevel} that should be downloaded or updated to.
	 * @param keepBackup Whether the old locale file should be retained after a successful locale update.
	 */
	public ReporterLocaleInitializer(Reporter plugin, String localeName, File dataFolder, boolean autoDownload, ReleaseLevel lowestLevel, boolean keepBackup)
	{
		this.plugin = plugin;
		
		this.locale = plugin.getLocale();
		
		this.localeName = localeName;
		this.dataFolder = dataFolder;
		this.autoDownload = autoDownload;
		this.lowestLevel = lowestLevel;
		this.keepBackup = keepBackup;
	}
	
	@Override
	public void run()
	{
		// There should be no other action on the locale until the initialization/update is completed.
		synchronized(locale)
		{
			initLocale();
			
			// Notify other threads that the locale is initialized.
			locale.notify();
		}
		
		plugin.loadLocale();
	}
	
	/**
	 * Initializes the locale file.
	 * <br /><br />
	 * <b>NOTE:</b> This also begins the download/update process.
	 * 
	 * @return The initialized and up-to-date {@link YamlConfiguration} locale.
	 */
	public Locale initLocale()
	{
		if(localeName.equalsIgnoreCase("en_US"))
			return locale;
		
		File localeFile = getLocaleFile(dataFolder, localeName);
		
		boolean downloaded = false;
		
		if(autoDownload)
		{
			try
			{
				downloaded = downloadOrUpdate(localeFile);
			}
			catch (Exception e)
			{
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + 
						"Error downloading or updating the locale file!");
				e.printStackTrace();
			}
		}
		else if(!localeFile.exists())
		{
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Locale file " + localeName + " does not exist locally!");
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Try setting locale.updates.autoDownload to true in the configuration.");
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Using English default.");
		}
		
		if(!localeFile.exists())
			localeFile = null;
	
		RevisionFile localeBackupFile = new RevisionFile(dataFolder, localeName+".yml.backup");
		
		localeBackupFile.incrementToLatestRevision();
		
		try
		{
			if(localeFile != null)
			{
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Loading locale file: " + localeFile.getName());
				
				locale.load(localeFile);
				
				if(update && downloaded)
				{
					if(!keepBackup)
					{
						Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Purging backup file " + localeBackupFile.getFileName());
						localeBackupFile.delete();
					}
					else
						Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Retaining backup file " + localeBackupFile.getFileName());
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "There was an error loading " + localeFile.getName());
			
			if(ex.getMessage().contains("unacceptable character"))
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Try converting the file to UTF-8 without BOM (Byte Order Marks) then try to reload it.");
			else
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Please let the author know this.");
			
			if(update)
			{
				this.restoreBackup(localeFile, localeBackupFile);
			}
			else
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Using English default.");
		}
		
		return locale;
	}
	
	/**
	 * Initializes the updater that will download/update the file.
	 * 
	 * @param localeFile The file the main locale resides.
	 * 
	 * @return A new {@link LocaleUpdater}.
	 * 
	 * @throws IOException
	 */
	private LocaleUpdater initUpdater(File localeFile) throws IOException
	{
		String localLocaleVersion = "1";
		try
		{
			YamlConfiguration localLocale = YamlConfiguration.loadConfiguration(localeFile);
			localLocaleVersion = localLocale.getString("locale.info.version", "1");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		UpdateSite localeXMLUpdateSite = Reporter.getLocaleXMLUpdateSite();
		
		return new LocaleUpdater(localeXMLUpdateSite, localeName, localLocaleVersion, lowestLevel);
	}
	
	/**
	 * Attempts to download or update the locale file.
	 * <br /><br />
	 * <b>NOTE:</b> If the file goes through an update the {@link ReporterLocaleInitializer#update} flag is set to true.
	 * If the file only downloads {@link ReporterLocaleInitializer#update} flag is set to false.
	 * 
	 * @param localeFile The file the main locale resides.
	 * 
	 * @return True if the the file was successfully downloaded.
	 * <br /><br />
	 * <b>NOTE:</b> Downloaded means if the original file was overwritten.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 * @throws IOException
	 */
	private boolean downloadOrUpdate(File localeFile) throws ParserConfigurationException, SAXException, ParseException, IOException
	{
		LocaleUpdater updater = initUpdater(localeFile);
		
		if(localeFile.exists())
		{
			update = true;
			
			return updater.localeUpdateProcess(localeFile);
		}
		else
		{
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
	private void restoreBackup(File localeFile, RevisionFile localeBackupFile)
	{
		if(!localeBackupFile.exists())
		{
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "The backup file does not exist.");
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Using English default.");
		}
		else
		{
			boolean loadSuccessful = attemptToLoadBackups(localeFile, localeBackupFile);
			
			if(loadSuccessful)
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Successfully restored and loaded backup file.");
			else
			{
				localeFile.delete();
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Failed to restore backups.");
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Using English default.");
			}
		}
	}
	
	/**
	 * Attempts to copy and load a backup locale file to the main locale file.
	 * If an error occurs when loading the backup file, it is deleted and the next backup will attempt to load.
	 * This continues until either a backup file is loaded successfully, or there are no more backups.
	 * 
	 * @param localeFile The file the main locale resides.
	 * @param localeBackupFile The backup {@link RevisionFile} for the locale.
	 * 
	 * @return True on successful load and restore of one of the backups, otherwise false.
	 */
	private boolean attemptToLoadBackups(File localeFile, RevisionFile localeBackupFile)
	{
		boolean loadSuccessful = false;
		
		// Set the backup file to the latest revision.
		localeBackupFile.incrementToLatestRevision();
		
		do
		{
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Attempting to restore backup revision: " + localeBackupFile.getRevision());
			
			localeBackupFile.renameTo(localeFile);
			localeBackupFile.delete();
			
			try
			{
				locale.load(localeFile);
			}
			catch(Exception e) // On error reset locale and decrement the backup revision.
			{
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + e.getMessage());
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Failed to load backup revision: " + localeBackupFile.getRevision());
				localeBackupFile.decrementRevision();
				loadSuccessful = false;
			}
		}
		while(!loadSuccessful && localeBackupFile.exists());
		
		return loadSuccessful;
	}
	
	/**
	 * Returns a file in the given dataFolder with a name of the given localeName.
	 * 
	 * @param dataFolder The folder where the locale file should be.
	 * @param localeName The name of the locale file.
	 * 
	 * @return A file in the given dataFolder with a name of the given localeName.
	 */
	private static File getLocaleFile(File dataFolder, String localeName)
	{
		if(localeName.contains("."))
			localeName = localeName.substring(0, localeName.indexOf("."));
		
		return new File(dataFolder, localeName + ".yml");
	}
}
