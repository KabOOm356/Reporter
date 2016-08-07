package net.KabOOm356.Service;

import net.KabOOm356.Configuration.ConstantEntry;
import net.KabOOm356.Configuration.Entry;
import org.junit.Before;
import org.junit.Test;
import test.test.service.ServiceTest;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class ConfigurationServiceTest extends ServiceTest {
	private ConfigurationService service;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();
		service = spy(new ConfigurationService(getModule()));
		when(getConfiguration().get("test", "default")).thenReturn("return");
	}

	@Test
	public void get() {
		final Entry<String> entry = new Entry<String>("test", "default");
		assertEquals("return", service.get(entry));
	}

	@Test
	public void getDefault() {
		final Entry<String> entry = new Entry<String>("doesNotExist", "default");
		assertEquals("default", service.get(entry));
	}

	@Test
	public void getMismatchingClass() {
		when(getConfiguration().get("test", true)).thenReturn("return");
		final Entry<Boolean> entry = new Entry<Boolean>("test", true);
		// The default should be returned in this case.
		assertEquals(true, service.get(entry));
	}

	@Test
	public void getConstantEntryAsEntry() {
		final Entry<Integer> entry = new ConstantEntry<Integer>(5);
		assertEquals(5, service.get(entry).intValue());
	}
}