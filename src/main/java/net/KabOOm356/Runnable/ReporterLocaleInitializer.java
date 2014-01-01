package net.KabOOm356.Runnable;

import java.io.File;

import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Reporter.Locale.ReporterLocaleUtil;

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
		this.localeName = localeName;
		this.dataFolder = dataFolder;
		this.autoDownload = autoDownload;
		this.lowestLevel = lowestLevel;
		this.keepBackup = keepBackup;
	}
	
	@Override
	public void run()
	{
		Locale locale = ReporterLocaleUtil.initLocale(localeName, dataFolder, autoDownload, lowestLevel, keepBackup);
		
		plugin.updateLocale(locale);
	}
}
