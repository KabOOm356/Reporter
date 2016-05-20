package net.KabOOm356.Command.Help;

import net.KabOOm356.Locale.Entry.ConstantEntry;
import net.KabOOm356.Locale.Entry.Entry;
import net.KabOOm356.Util.ObjectPair;

public class Usage extends ObjectPair<Entry, Entry> {
	public Usage(final Entry key, final Entry value) {
		super(key, value);
	}

	public Usage(final String key, final Entry value) {
		super(new ConstantEntry(key), value);
	}

	public Usage(final Entry key, final String value) {
		super(key, new ConstantEntry(value));
	}

	public Usage(final String key, final String value) {
		super(new ConstantEntry(key), new ConstantEntry(value));
	}
}
