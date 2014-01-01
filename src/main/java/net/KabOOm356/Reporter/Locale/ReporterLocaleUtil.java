package net.KabOOm356.Reporter.Locale;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;

import net.KabOOm356.File.RevisionFile;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.FileIO;
import net.KabOOm356.Util.UrlIO;

import org.bukkit.configuration.file.YamlConfiguration;
import org.xml.sax.SAXException;

/**
 * A class to help initialize, download, and update Reporter's locale file.
 */
public class ReporterLocaleUtil
{
	/**
	 * Initializes the locale file.
	 * <br /><br />
	 * <b>NOTE:</b> This also begins the download/update process.
	 * 
	 * @param localeName The name of the locale to initialize.
	 * @param dataFolder The folder to where the locale will be found or downloaded to.
	 * @param autoDownload If the locale should be automatically downloaded/updated.
	 * @param lowestLevel The lowest level of {@link ReleaseLevel} to check for.
	 * @param keepBackup If the backup file should be kept after updating.
	 * 
	 * @return The initialized and up-to-date {@link YamlConfiguration} locale.
	 */
	public static Locale initLocale(String localeName, File dataFolder, boolean autoDownload, ReleaseLevel lowestLevel, boolean keepBackup)
	{
		Locale locale = new Locale();
		
		if(localeName.equalsIgnoreCase("en_US"))
			return locale;
		
		File localeFile = getLocaleFile(dataFolder, localeName);
		
		boolean update = false;
		boolean downloaded = false;
		
		if(autoDownload)
		{
			if(localeFile.exists())
			{
				update = true;
				String localLocaleVersion = "0";
				
				try
				{
					YamlConfiguration localLocale = YamlConfiguration.loadConfiguration(localeFile);
					localLocaleVersion = localLocale.getString("locale.info.version", "1");
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				
				try
				{
					downloaded = localeUpdateProcess(localeName, localLocaleVersion, localeFile, lowestLevel);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				update = false;
				try
				{
					downloaded = localeDownloadProcess(localeName, localeFile, lowestLevel);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
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
				if(!localeBackupFile.exists())
				{
					Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "The backup file does not exist.");
					Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Using English default.");
				}
				else
				{
					boolean loadSuccessful = true;
					
					do
					{
						Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Attempting to restore backup revision: " + localeBackupFile.getRevision());
						
						localeBackupFile.renameTo(localeFile);
						localeBackupFile.delete();
						
						try
						{
							locale.load(localeFile);
						}
						catch(Exception e)
						{
							Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + e.getMessage());
							Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Failed to load backup revision: " + localeBackupFile.getRevision());
							locale = new Locale();
							localeBackupFile.decrementRevision();
							loadSuccessful = false;
						}
					}
					while(!loadSuccessful && localeBackupFile.exists());
					
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
			else
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Using English default.");
		}
		
		return locale;
	}
	
	/**
	 * Will attempt to download the given locale file to the given destination if it exists.
	 * 
	 * @param localeName The name of the locale file to download.
	 * @param destination The destination for the file to be saved to.
	 * @param lowestLevel The lowest {@link ReleaseLevel} of file to download.
	 * 
	 * @return True if the file is found and downloaded, otherwise false.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 * @throws IOException
	 */
	private static boolean localeDownloadProcess(String localeName, File destination, ReleaseLevel lowestLevel) throws ParserConfigurationException, SAXException, ParseException, IOException
	{
		if(localeName.contains(".yml"))
			localeName = localeName.substring(0, localeName.indexOf(".yml"));
		
		Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Checking for file: " + destination.getName());
		
		VersionedNetworkFile downloadFile = null;
		
		try
		{
			downloadFile = UrlIO.findLatestFile(Reporter.getLocaleXMLUpdateSite(), localeName, lowestLevel);
		}
		catch(FileNotFoundException e)
		{
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Could not find the locale file " + localeName + ".yml!");
		}
		
		if(downloadFile != null)
		{
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Downloading the locale file: " + localeName + ".yml version " + downloadFile.getVersion() + "...");
			UrlIO.downloadFile(downloadFile, destination);
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Locale file successfully downloaded.");
			return true;
		}
		
		Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Failed to download locale file!");
		Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Using English default.");
		
		return false;
	}
	
	/**
	 * Will attempt to update the given locale.
	 * 
	 * @param localeName The name of the locale to attempt to update.
	 * @param localLocaleVersion The version of the locale on the client.
	 * @param destination The destination to download the file to.
	 * @param lowestLevel The lowest {@link ReleaseLevel} to update to.
	 * 
	 * @return True if the locale file is updated.  False if the file is already up to date or failed to updated.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 * @throws IOException
	 */
	private static boolean localeUpdateProcess(String localeName, String localLocaleVersion, File destination, ReleaseLevel lowestLevel) throws ParserConfigurationException, SAXException, ParseException, IOException
	{
		if(localeName.contains(".yml"))
			localeName = localeName.substring(0, localeName.indexOf(".yml"));
		
		Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Checking for update for file: " + destination.getName());
		
		VersionedNetworkFile updateNetworkFile = null;
		
		try
		{
			updateNetworkFile = UrlIO.checkForUpdate(localeName, Reporter.getLocaleXMLUpdateSite(), localLocaleVersion, lowestLevel);
		}
		catch(FileNotFoundException e)
		{
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Could not find the locale file " + localeName + ".yml!");
			Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Failed to check for locale update!");
		}
		
		if(updateNetworkFile == null)
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Locale file is up to date.");
		else
		{
			// Create backup
			RevisionFile localeBackupFile = null;
			
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Updating " + localeName + ".yml from version " + localLocaleVersion + " to version " + updateNetworkFile.getVersion() + "...");
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Creating backup of the locale file...");
			
			localeBackupFile = createLocaleBackup(destination);
			
			if(localeBackupFile != null)
			{
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Locale backup successful.");
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Locale backup created in file: " + localeBackupFile.getFileName());
				destination.delete();
			}
			else
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Creating backup unsuccessful.");
			
			boolean successful;
			
			try
			{
				successful = UrlIO.downloadFile(updateNetworkFile, destination);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				successful = false;
			}
			
			if(successful)
			{
				Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Locale file successfully updated.");
				
				return true;
			}
			else
			{
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "Updating the locale file failed.");
				
				destination.delete();
			}
		}
		
		return false;
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
	
	/**
	 * Creates a locale backup.
	 * 
	 * @param localeFile The file to backup.
	 * 
	 * @return A {@link RevisionFile} where the given File was saved.
	 */
	private static RevisionFile createLocaleBackup(File localeFile)
	{
		RevisionFile localeFileBackup = null;
		
		try
		{
			localeFileBackup = new RevisionFile(localeFile.getParent(), localeFile.getName() + ".backup");
			
			localeFileBackup.incrementToNextRevision();
			
			localeFileBackup.createNewFile();
			
			FileIO.copyTextFile(localeFile, localeFileBackup.getFile());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(localeFileBackup != null)
				localeFileBackup.delete();
			return null;
		}
		
		return localeFileBackup;
	}
}
