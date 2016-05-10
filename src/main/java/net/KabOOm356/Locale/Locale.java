package net.KabOOm356.Locale;

import net.KabOOm356.Locale.Entry.LocaleEntry;
import net.KabOOm356.Locale.Entry.LocaleInfo;
import net.KabOOm356.Locale.Entry.LocalePhrase;
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
	public String getString(LocaleEntry entry) {
		return getString(entry.getPath(), entry.getDefault());
	}

	/**
	 * Returns a line from the locale file under the phrases section.
	 *
	 * @param phrase The phrase line to get.
	 * @return The line from the locale file under the phrases section.
	 */
	public String getPhrase(String phrase) {
		return super.getString(LocaleEntry.prefix + LocalePhrase.prefix + phrase);
	}

	/**
	 * Returns a line from the locale file under the info section.
	 *
	 * @param info The info line to get.
	 * @return The line from the locale file under the info section.
	 */
	public String getInfo(String info) {
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
