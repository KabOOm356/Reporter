package net.KabOOm356.Service.Store;

import org.apache.commons.lang.Validate;

public class Store<T> {
  private final T store;

  protected Store(final T store) {
    Validate.notNull(store);
    this.store = store;
  }

  public T get() {
    return store;
  }
}
