package net.KabOOm356.Reporter.Configuration;

import net.KabOOm356.Reporter.Reporter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.io.*;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({ReporterConfigurationUtil.class})
public class ReporterConfigurationUtilTest extends PowerMockitoTest {
	@Spy
	final
	File dataFolder = new File("TestFolder");

	@Mock
	FileConfiguration configuration;

	@Mock
	ClassLoader classLoader;

	@Mock
	Reporter reporter;

	@Mock
	File configFile;

	@Mock
	URL defaultConfigurationFile;

	@Mock
	BufferedReader bufferedReader;

	@Mock
	BufferedWriter bufferedWriter;

	@Test
	public void testInitConfigurationExists() throws Exception {
		when(configFile.exists()).thenReturn(true);
		whenNew(File.class).withArguments(dataFolder, "config.yml").thenReturn(configFile);
		ReporterConfigurationUtil.initConfiguration(null, dataFolder, configuration);
		verify(configuration).load(configFile);
	}

	@Test
	public void testInitConfigurationDoesNotExist() throws Exception {
		whenNew(File.class).withArguments(dataFolder, "config.yml").thenReturn(configFile);
		whenNew(FileWriter.class).withAnyArguments().thenReturn(mock(FileWriter.class));
		whenNew(InputStreamReader.class).withAnyArguments().thenReturn(mock(InputStreamReader.class));
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		whenNew(BufferedWriter.class).withAnyArguments().thenReturn(bufferedWriter);

		doReturn(true).when(configFile).createNewFile();
		when(configFile.exists()).thenReturn(false);
		when(defaultConfigurationFile.openStream()).thenReturn(mock(InputStream.class));
		when(bufferedReader.readLine()).thenReturn("Version").thenReturn("Test Junk").thenReturn(null);

		ReporterConfigurationUtil.initConfiguration(defaultConfigurationFile, dataFolder, configuration);
		verify(configFile).createNewFile();
		verify(bufferedWriter).flush();
		verify(configuration).load(configFile);
	}

	@Test
	public void testUpdateConfigurationNoUpdate() throws Exception {
		whenNew(YamlConfiguration.class).withAnyArguments().thenReturn(mock(YamlConfiguration.class));
		when(configuration.isSet(anyString())).thenReturn(true);
		when(configuration.isSet("general.messaging.reportList")).thenReturn(false);
		when(configuration.isSet("general.messaging.broadcast")).thenReturn(false);
		when(configuration.isSet("general.locale")).thenReturn(false);
		when(configuration.isSet("general.localeAutoDownload")).thenReturn(false);
		when(configuration.isSet("general.checkForUpdates")).thenReturn(false);
		when(configuration.isSet("general.checkForDevUpdates")).thenReturn(false);
		when(configuration.isSet("locale.localeAutoDownload")).thenReturn(false);
		when(configuration.isSet("locale.keepLocaleBackupFile")).thenReturn(false);
		when(configuration.isSet("general.reporting.alerts.limitReached")).thenReturn(false);
		when(configuration.isSet("general.reporting.alerts.allowedToReportAgain")).thenReturn(false);

		assertFalse(ReporterConfigurationUtil.updateConfiguration(configuration));
	}

	@Test
	public void testUpdateConfigurationAllUpdate() throws Exception {
		whenNew(YamlConfiguration.class).withAnyArguments().thenReturn(mock(YamlConfiguration.class));
		when(configuration.isSet(anyString())).thenReturn(false);
		when(configuration.isSet("general.messaging.reportList")).thenReturn(true);
		when(configuration.isSet("general.messaging.broadcast")).thenReturn(true);
		when(configuration.isSet("general.locale")).thenReturn(true);
		when(configuration.isSet("general.localeAutoDownload")).thenReturn(true);
		when(configuration.isSet("general.checkForUpdates")).thenReturn(true);
		when(configuration.isSet("general.checkForDevUpdates")).thenReturn(true);
		when(configuration.isSet("locale.localeAutoDownload")).thenReturn(true);
		when(configuration.isSet("locale.keepLocaleBackupFile")).thenReturn(true);
		when(configuration.isSet("general.reporting.alerts.limitReached")).thenReturn(true);
		when(configuration.isSet("general.reporting.alerts.allowedToReportAgain")).thenReturn(true);

		when(configuration.options()).thenReturn(mock(FileConfigurationOptions.class));

		assertTrue(ReporterConfigurationUtil.updateConfiguration(configuration));
		verify(configuration, atLeastOnce()).set(anyString(), any(Object.class));
	}
}
