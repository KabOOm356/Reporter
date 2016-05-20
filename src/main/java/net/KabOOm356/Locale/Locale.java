package net.KabOOm356.Locale;

import net.KabOOm356.Locale.Entry.*;
import net.KabOOm356.Util.Initializable;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * A class to help with reading a locale file.
 */
public class Locale extends YamlConfiguration implements Initializable {
	private boolean isInitialized = Initializable.isInitialized;

	/**
	 * Gets an entry from the locale file.
	 *
	 * @param entry The entry to get.
	 * @return The line in the locale if it exists, or the entry's default line if it does not exist.
	 */
	public String getString(final Entry entry) {
		if (entry instanceof ConstantEntry) {
			final ConstantEntry constantEntry = ConstantEntry.class.cast(entry);
			return getString(constantEntry);
		}
		return getString(entry.getPath(), entry.getDefault());
	}

	/**
	 * Returns the given entry's default, because it is constant.
	 *
	 * @param entry The entry to get.
	 * @return The
	 */
	public String getString(final ConstantEntry entry) {
		// No need to look up the entry.  It is constant.
		return entry.getDefault();
	}

	/**
	 * Returns a line from the locale file under the phrases section.
	 *
	 * @param phrase The phrase line to get.
	 * @return The line from the locale file under the phrases section.
	 */
	public String getPhrase(final String phrase) {
		return super.getString(LocaleEntry.prefix + LocalePhrase.prefix + phrase);
	}

	/**
	 * Returns a line from the locale file under the info section.
	 *
	 * @param info The info line to get.
	 * @return The line from the locale file under the info section.
	 */
	public String getInfo(final String info) {
		return getString(LocaleEntry.prefix + LocaleInfo.prefix + info);
	}

	@Override
	public boolean isInitialized() {
		return isInitialized;
	}

	@Override
	public void initialized() {
		isInitialized = true;
	}
}
