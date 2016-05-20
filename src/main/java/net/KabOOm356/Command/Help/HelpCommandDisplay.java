package net.KabOOm356.Command.Help;

import net.KabOOm356.Locale.Entry.LocalePhrase;
import org.apache.commons.lang.Validate;

public final class HelpCommandDisplay {
	private final LocalePhrase header;
	private final LocalePhrase alias;
	private final LocalePhrase next;
	private final LocalePhrase hint;

	private HelpCommandDisplay(final Builder builder) {
		Validate.notNull(builder.header);
		Validate.notNull(builder.alias);
		Validate.notNull(builder.next);
		Validate.notNull(builder.hint);
		this.header = builder.header;
		this.alias = builder.alias;
		this.next = builder.next;
		this.hint = builder.hint;
	}

	public LocalePhrase getHeader() {
		return header;
	}

	public LocalePhrase getAlias() {
		return alias;
	}

	public LocalePhrase getNext() {
		return next;
	}

	public LocalePhrase getHint() {
		return hint;
	}

	public static class Builder {
		private LocalePhrase header;
		private LocalePhrase alias;
		private LocalePhrase next;
		private LocalePhrase hint;

		public Builder setHeader(final LocalePhrase header) {
			this.header = header;
			return this;
		}

		public Builder setAlias(final LocalePhrase alias) {
			this.alias = alias;
			return this;
		}

		public Builder setNext(final LocalePhrase next) {
			this.next = next;
			return this;
		}

		public Builder setHint(final LocalePhrase hint) {
			this.hint = hint;
			return this;
		}

		public HelpCommandDisplay build() {
			return new HelpCommandDisplay(this);
		}
	}
}