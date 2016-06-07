package net.KabOOm356.Service;

import net.KabOOm356.Service.Store.StoreModule;

public abstract class Service {
	private final ServiceModule module;

	protected Service(final ServiceModule module) {
		this.module = module;
	}

	protected ServiceModule getModule() {
		return module;
	}

	protected StoreModule getStore() {
		return getModule().getStore();
	}
}
