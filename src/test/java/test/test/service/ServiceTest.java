package test.test.service;

import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Locale.Entry.LocalePhrase;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Permission.PermissionHandler;
import net.KabOOm356.Service.Messager.PlayerMessages;
import net.KabOOm356.Service.ServiceModule;
import net.KabOOm356.Service.Store.StoreModule;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Spy;
import test.test.Answer.LocaleEntryAnswer;
import test.test.PowerMockitoTest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;

public abstract class ServiceTest extends PowerMockitoTest {
	private static final Method getStore = method(ServiceModule.class, "getStore");

	@Mock
	private ServiceModule module;

	@Mock
	private Configuration configuration;

	@Mock
	private ExtendedDatabaseHandler databaseHandler;

	@Mock
	private PermissionHandler permissionHandler;

	@Spy
	private Map<CommandSender, Integer> lastViewed = new HashMap<CommandSender, Integer>();

	@Spy
	private PlayerMessages playerMessages = new PlayerMessages();

	@Mock
	private Locale locale;

	@Before
	public void setupMocks() throws Exception {
		final StoreModule store = new StoreModule(configuration, databaseHandler, locale, permissionHandler, lastViewed, playerMessages);
		doReturn(store).when(module, getStore).withNoArguments();
		when(locale.getString(any(LocalePhrase.class))).thenAnswer(LocaleEntryAnswer.instance);
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

	public Map<CommandSender, Integer> getLastViewedHandler() {
		return lastViewed;
	}

	public PlayerMessages getPlayerMessages() {
		return playerMessages;
	}

	public Locale getLocale() {
		return locale;
	}
}
