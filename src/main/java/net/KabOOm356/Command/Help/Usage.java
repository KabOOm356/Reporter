package net.KabOOm356.Command.Help;

import net.KabOOm356.Configuration.ConstantEntry;
import net.KabOOm356.Configuration.Entry;
import net.KabOOm356.Util.ObjectPair;

public class Usage extends ObjectPair<Entry<String>, Entry<String>> {
  public Usage(final Entry<String> key, final Entry<String> value) {
    super(key, value);
  }

  public Usage(final String key, final Entry<String> value) {
    super(new ConstantEntry<>(key), value);
  }

  public Usage(final Entry<String> key, final String value) {
    super(key, new ConstantEntry<>(value));
  }

  public Usage(final String key, final String value) {
    super(new ConstantEntry<>(key), new ConstantEntry<>(value));
  }
}
