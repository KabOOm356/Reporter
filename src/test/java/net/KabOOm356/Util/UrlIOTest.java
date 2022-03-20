package net.KabOOm356.Util;

import net.KabOOm356.File.AbstractFiles.NetworkFile;
import org.junit.Test;
import org.mockito.Mock;
import test.test.MockitoTest;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class UrlIOTest extends MockitoTest {
	@Mock
	public HttpURLConnection httpURLConnection;

	@Mock
	public NetworkFile networkFile;

	@Mock
	public File file;

	@Test
	public void testGetResponse() throws IOException {
		when(httpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
		UrlIO.getResponse(httpURLConnection);
	}

	@Test
	public void testIsResponseValid() throws IOException {
		when(httpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
		assertTrue(UrlIO.isResponseValid(httpURLConnection));

		when(httpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
		assertFalse(UrlIO.isResponseValid(httpURLConnection));
	}

	@Test
	public void testDownloadFileNull() throws IOException {
		try {
			UrlIO.downloadFile(null, null);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			UrlIO.downloadFile(networkFile, null);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			UrlIO.downloadFile(null, file);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}
	}

	@Test
	public void testGetVersion() {
		assertEquals("", UrlIO.getVersion(""));
		assertEquals("", UrlIO.getVersion("hello"));
		assertEquals("1", UrlIO.getVersion("hello 1"));
		assertEquals("1", UrlIO.getVersion("hello v1"));
		assertEquals("1", UrlIO.getVersion("hello V1"));
		assertEquals("", UrlIO.getVersion("hello alpha"));
		assertEquals("1-alpha", UrlIO.getVersion("hello 1 alpha"));
	}
}
