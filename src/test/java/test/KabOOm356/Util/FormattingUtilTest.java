package test.KabOOm356.Util;

import static org.junit.Assert.*;
import net.KabOOm356.Util.FormattingUtil;
import net.KabOOm356.Util.Util;

import org.junit.Test;

public class FormattingUtilTest
{
	String str = "This is a string.\nThis is a new line.\n\tThis is a tabbed new line!";
	
	String newLine = FormattingUtil.newLine;
	String tab = FormattingUtil.tab;
	
	@Test
	public void testAppendCharacterAfterStringStringStringInt()
	{
		// Test positive integers 0-99.
		for(int LCV = 0; LCV < 100; LCV++)
		{
			String returned = FormattingUtil.appendCharacterAfter(str, newLine, tab, LCV);
			
			testCharacterInsertion(returned, str, newLine, tab, LCV);
		}
	}
	
	@Test
	public void testAppendCharacterAfterStringCharCharInt()
	{
		char newLineCharacter = FormattingUtil.newLineCharacter;
		char tabCharacter = FormattingUtil.tabCharacter;
		
		// Test positive integers 0-99.
		for(int LCV = 0; LCV < 100; LCV++)
		{
			String returned = FormattingUtil.appendCharacterAfter(str, newLineCharacter, tabCharacter, LCV);
			
			testCharacterInsertion(returned, str, newLine, tab, LCV);
		}
	}
	
	@Test
	public void testAddCharacterToNewLinesStringCharInt()
	{
		// Test positive integers 0-99.
		for(int LCV = 0; LCV < 100; LCV++)
		{
			String returned = FormattingUtil.addCharacterToNewLines(str, FormattingUtil.tabCharacter, LCV);
			
			testCharacterInsertion(returned, str, newLine, tab, LCV);
		}
	}
	
	@Test
	public void testAddCharacterToNewLinesStringStringInt()
	{
		// Test positive integers 0-99.
		for(int LCV = 0; LCV < 100; LCV++)
		{
			String returned = FormattingUtil.addCharacterToNewLines(str, tab, LCV);
			
			testCharacterInsertion(returned, str, newLine, tab, LCV);
		}
	}
	
	@Test
	public void testAddTabsToNewLinesStringInt()
	{
		// Test positive integers 0-99.
		for(int LCV = 0; LCV < 100; LCV++)
		{
			String returned = FormattingUtil.addTabsToNewLines(str, LCV);
			
			testCharacterInsertion(returned, str, newLine, tab, LCV);
		}
	}
	
	private void testCharacterInsertion(
			String returned,
			String str,
			String character,
			String additionCharacter,
			int additions)
	{
		int timesInserted = Util.countOccurrences(str, character);
		int characterOccurrences = Util.countOccurrences(str, additionCharacter);
		
		// Get the total amount of occurrences there should be after inserting the character.
		// (number of additions * the number of times inserted) + number of times the character appears before insertion
		int expectedOccurrences = (additions * timesInserted) + characterOccurrences;
		
		int actualOccurrences = Util.countOccurrences(returned, additionCharacter);
		
		assertEquals(expectedOccurrences, actualOccurrences);
	}
	
	@Test
	public void testAddCharacterBeforeCharacterIllegalArgumentException()
	{
		String nil = null;
		int negative = -1;
		int positive = 1;
		
		// Proof that there is no exception with these parameters.
		FormattingUtil.appendCharacterAfter(str, newLine, tab, positive);
		
		try
		{
			// Replace the string with null, keep other parameters the same as the proof.
			FormattingUtil.appendCharacterAfter(nil, newLine, tab, positive);
			
			// Fail if the expected exception is not thrown.
			fail("Expected IllegalArgumentException was not thrown!");
		}
		catch(IllegalArgumentException e)
		{
			// Catch the exception that should be thrown.
		}
		
		try
		{
			// Replace the addition parameter with a negative value.
			// Keep the other parameters the same as the proof.
			FormattingUtil.appendCharacterAfter(str, newLine, tab, negative);
			
			// Fail if the expected exception is not thrown.
			fail("Expected IllegalArgumentException was not thrown!");
		}
		catch(IllegalArgumentException e)
		{
			// Catch the exception that should be thrown.
		}
		
		try
		{
			// Replace the character parameter with null.
			// Keep the other parameters the same as the proof.
			FormattingUtil.appendCharacterAfter(str, nil, tab, positive);
			
			// Fail if the expected exception is not thrown.
			fail("Expected IllegalArgumentException was not thrown!");
		}
		catch(IllegalArgumentException e)
		{
			// Catch the exception that should be thrown.
		}
		
		try
		{
			// Replace the additionCharacter parameter with null.
			// Keep the other parameters the same as the proof.
			FormattingUtil.appendCharacterAfter(str, newLine, nil, positive);
			
			// Fail if the expected exception is not thrown.
			fail("Expected IllegalArgumentException was not thrown!");
		}
		catch(IllegalArgumentException e)
		{
			// Catch the exception that should be thrown.
		}
	}
	
	@Test
	public void testCapitalizeFirstCharacterString()
	{
		assertEquals("Hello", FormattingUtil.capitalizeFirstCharacter("hello"));
		assertEquals("Hello", FormattingUtil.capitalizeFirstCharacter("heLLo"));
		assertEquals("Hello", FormattingUtil.capitalizeFirstCharacter("Hello"));
		assertEquals("Ok", FormattingUtil.capitalizeFirstCharacter("ok"));
		assertEquals("*", FormattingUtil.capitalizeFirstCharacter("*"));
	}
}
