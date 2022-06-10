package test.test.service;

import static org.mockito.Mockito.*;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.PermissionHandler;
import net.KabOOm356.Service.Messager.PlayerMessages;
import net.KabOOm356.Service.ServiceModule;
import net.KabOOm356.Service.Store.StoreModule;
import net.KabOOm356.Service.Store.type.LastViewed;
import net.KabOOm356.Service.Store.type.PlayerReport;
import org.bukkit.configuration.Configuration;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Spy;
import test.test.MockitoTest;

public abstract class ServiceTest extends MockitoTest {
  private ServiceModule module;
  @Spy private final LastViewed lastViewed = new LastViewed();
  @Spy private final PlayerMessages playerMessages = new PlayerMessages();
  @Mock private PlayerReport playerReports;
  @Mock private Configuration configuration;
  @Mock private ExtendedDatabaseHandler databaseHandler;
  @Mock private PermissionHandler permissionHandler;
  @Mock private Locale locale;

  @Before
  public void setupMocks() throws Exception {
    final StoreModule store =
        new StoreModule(
            configuration,
            databaseHandler,
            locale,
            permissionHandler,
            lastViewed,
            playerMessages,
            playerReports);
    module =
        mock(
            ServiceModule.class,
            withSettings().useConstructor(store).defaultAnswer(CALLS_REAL_METHODS));
  }

  public ServiceModule getModule() {
    return module;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public ExtendedDatabaseHandler getDatabaseHandler() {
    return databaseHandler;
  }

  public PermissionHandler getPermissionHandler() {
    return permissionHandler;
  }

  public LastViewed getLastViewedHandler() {
    return lastViewed;
  }

  public PlayerReport getPlayerReports() {
    return playerReports;
  }

  public Locale getLocale() {
    return locale;
  }
}
