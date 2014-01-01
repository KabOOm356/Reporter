package net.KabOOm356.Runnable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;


import net.KabOOm356.File.AbstractFiles.UpdateSite;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.UrlIO;
import org.xml.sax.SAXException;

/**
 * A {@link Runnable} class for checking if there is an update available.
 */
public class UpdateThread implements Runnable
{
	/** The {@link UpdateSite} to parse for the updates. */
	private UpdateSite updateSite;
	/** The current version of this plugin on this machine. */
	private String localVersion;
	/** The lowest level that will be checked for updating. */
	private ReleaseLevel level;
	
	/**
	 * Constructor
	 * 
	 * @param updateSite The {@link UpdateSite} to check if there is an update for the plugin.
	 * @param localVersion The current version of this plugin on this machine.
	 * @param level The lowest level that will be checked for updating (ALPHA, BETA, RC).
	 */
	public UpdateThread(UpdateSite updateSite, String localVersion, ReleaseLevel level)
	{
		this.updateSite = updateSite;
		this.localVersion = localVersion;
		this.level = level;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			checkForUpdates();
		}
		catch (Exception e)
		{
			Reporter.getLog().severe(Reporter.getDefaultConsolePrefix() + "Update thread failed!");
			e.printStackTrace();
		}
	}
	
	/**
	 * The actual method that will check for an update and message the console if one is available.
	 * 
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParseException
	 */
	private void checkForUpdates() throws URISyntaxException, MalformedURLException, IOException, SAXException, ParserConfigurationException, ParseException
	{
		VersionedNetworkFile update = UrlIO.checkForUpdate("Reporter", updateSite, localVersion, level);
		
		if(update == null)
			Reporter.getLog().info(Reporter.getDefaultConsolePrefix() + "Reporter is up to date!");
		else
		{
			if(update.getVersion() != null)
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "There is a new update available on BukkitDev: Version " + update.getVersion());
			else
				Reporter.getLog().warning(Reporter.getDefaultConsolePrefix() + "There is a new update available on BukkitDev!");
		}
	}
}
