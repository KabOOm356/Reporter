package net.KabOOm356.Updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.KabOOm356.File.RevisionFile;
import net.KabOOm356.File.AbstractFiles.UpdateSite;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.FileIO;
import net.KabOOm356.Util.UrlIO;
import net.KabOOm356.Util.Util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An {@link Updater} to update the locale file from an XML update site.
 */
public class LocaleUpdater extends Updater
{
	/**
	 * Constructor.
	 * 
	 * @param updateSite The {@link UpdateSite} to parse for updates.
	 * @param name The name of the file to parse for.
	 * @param localVersion The local version.
	 * @param lowestLevel The lowest {@link ReleaseLevel} to consider.
	 * 
	 * @throws IOException
	 */
	public LocaleUpdater(UpdateSite updateSite, String name, String localVersion, ReleaseLevel lowestLevel) throws IOException
	{
		super(updateSite, name, localVersion, lowestLevel);
	}
	
	/**
	 * Finds the latest file released from the locale XML update site. 
	 * 
	 * @return A {@link VersionedNetworkFile} representation of the latest file found.
	 * 
	 * @throws FileNotFoundException Thrown if no files match the file name.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public VersionedNetworkFile findLatestFile() throws SAXException, IOException, ParserConfigurationException
	{
		URLConnection connection = getConnection();
		
		String name = getName();
		ReleaseLevel lowestLevel = getLowestLevel();
		
		if(connection == null)
		{
			if(name == null || name.equals(""))
				throw new IllegalArgumentException("Both the connection and the name cannot be null!");
			throw new IllegalArgumentException("The connection cannot be null!");
		}
		else if(name == null || name.equals(""))
			throw new IllegalArgumentException("File name to search for cannot be null!");
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(connection.getInputStream());
		
		doc.getDocumentElement().normalize();
		
		NodeList nodeList = doc.getElementsByTagName("locale");
		
		VersionedNetworkFile file = null;
		VersionedNetworkFile latestFile = null;
		
		for(int LCV = 0; LCV < nodeList.getLength(); LCV++)
		{
			Node nNode = nodeList.item(LCV);
			if (nNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element = (Element) nNode;
				
				if(Util.startsWithIgnoreCase(UrlIO.getNodeValue(element, "file_name"), name))
				{
					String fileName = UrlIO.getNodeValue(element, "file_name");
					String version = UrlIO.getNodeValue(element, "version");
					String encoding = UrlIO.getNodeValue(element, "encoding");
					String link = UrlIO.getNodeValue(element, "download_link");
					
					file = new VersionedNetworkFile(fileName, version, encoding, link);
					
					// The release level must be greater than or equal to the lowest release level specified.
					if(file.getReleaseLevel().compareToByValue(lowestLevel) >= 0)
					{
						// If latestFile is not initialized, set latestFile to the current file.
						// If the current file's version is greater than the latestFile's version, set latestFile to the current file.
						if(latestFile == null || latestFile.compareVersionTo(file) < 0)
							latestFile = file;
					}
				}
			}
		}
		
		if(latestFile == null)
			throw new FileNotFoundException("File " + name + " could not be found!");
		
		return latestFile;
	}
	
	/**
	 * Will attempt to download the locale file to the given destination if it exists.
	 * 
	 * @return True if the file is found and downloaded, otherwise false.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean localeDownloadProcess(File destination) throws ParserConfigurationException, SAXException, ParseException, IOException
	{
		String localeName = getName();
		
		if(localeName.contains(".yml"))
			localeName = localeName.substring(0, localeName.indexOf(".yml"));
		
		Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Checking for file: " + destination.getName());
		
		VersionedNetworkFile downloadFile = null;
		
		try
		{
			downloadFile = findLatestFile();
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
	 * Will attempt to update the locale file.
	 * 
	 * @return True if the locale file is updated.  False if the file is already up to date or failed to updated.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean localeUpdateProcess(File destination) throws ParserConfigurationException, SAXException, ParseException, IOException
	{
		String localeName = getName();
		
		if(localeName.contains(".yml"))
			localeName = localeName.substring(0, localeName.indexOf(".yml"));
		
		Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Checking for update for file: " + destination.getName());
		
		VersionedNetworkFile updateNetworkFile = null;
		
		try
		{
			updateNetworkFile = checkForUpdates();
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
			
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Updating " + localeName + ".yml from version " + getLocalVersion() + " to version " + updateNetworkFile.getVersion() + "...");
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Creating backup of the locale file...");
			
			localeBackupFile = FileIO.createBackup(destination);
			
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
}
