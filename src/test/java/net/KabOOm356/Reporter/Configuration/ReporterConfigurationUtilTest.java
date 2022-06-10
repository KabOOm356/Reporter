package net.KabOOm356.Reporter.Configuration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.junit.Test;
import org.mockito.Mock;
import test.test.MockitoTest;

public class ReporterConfigurationUtilTest extends MockitoTest {
  @Mock private YamlConfiguration configuration;

  @Test
  public void testUpdateConfigurationNoUpdate() {
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
    when(configuration.isSet("plugin.statistics.opt-out")).thenReturn(false);

    assertFalse(ReporterConfigurationUtil.updateConfiguration(configuration));
  }

  @Test
  public void testUpdateConfigurationAllUpdate() {
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
    when(configuration.isSet("plugin.statistics.opt-out")).thenReturn(true);

    when(configuration.options()).thenReturn(mock(YamlConfigurationOptions.class));

    assertTrue(ReporterConfigurationUtil.updateConfiguration(configuration));
    verify(configuration, atLeastOnce()).set(anyString(), any(Object.class));
  }
}
