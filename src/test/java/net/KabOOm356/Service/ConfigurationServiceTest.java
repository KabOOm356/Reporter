package net.KabOOm356.Service;

import net.KabOOm356.Configuration.ConstantEntry;
import net.KabOOm356.Configuration.Entry;
import org.junit.Before;
import org.junit.Test;
import test.test.Answer.ConfigurationAnswer;
import test.test.service.ServiceTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ConfigurationServiceTest extends ServiceTest {
	private ConfigurationService service;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();
		service = spy(new ConfigurationService(getModule()));
		when(getConfiguration().get(anyString(), anyString())).thenAnswer(ConfigurationAnswer.instance);
		when(getConfiguration().get(anyString(), anyInt())).thenAnswer(ConfigurationAnswer.instance);
	}

	@Test
	public void get() {
		when(getConfiguration().get("test", "default")).thenReturn("return");
		final Entry<String> entry = new Entry<>("test", "default");
		assertEquals("return", service.get(entry));
	}

	@Test
	public void getDefault() {
		final Entry<String> entry = new Entry<>("doesNotExist", "default");
		assertEquals("default", service.get(entry));
	}

	@Test
	public void getMismatchingClass() {
		when(getConfiguration().get("test", true)).thenReturn("return");
		final Entry<Boolean> entry = new Entry<>("test", true);
		// The default should be returned in this case.
		assertEquals(true, service.get(entry));
	}

	@Test
	public void getConstantEntryAsEntry() {
		final Entry<Integer> entry = new ConstantEntry<>(5);
		assertEquals(5, service.get(entry).intValue());
	}
}