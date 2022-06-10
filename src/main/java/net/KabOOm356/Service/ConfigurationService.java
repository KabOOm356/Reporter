package net.KabOOm356.Service;

import net.KabOOm356.Configuration.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.Configuration;

public class ConfigurationService extends Service {
  private static final Logger log = LogManager.getLogger(ConfigurationService.class);

  protected ConfigurationService(final ServiceModule module) {
    super(module);
  }

  public <T> T get(final Entry<T> entry) {
    final Configuration configuration = getStore().getConfigurationStore().get();
    final Object value = configuration.get(entry.getPath(), entry.getDefault());
    if (value == null) {
      return entry.getDefault();
    } else if (entry.getDefault().getClass().equals(value.getClass())) {
      return (T) entry.getDefault().getClass().cast(value);
    } else {
      log.warn(
          String.format(
              "Configuration entry [%s] of class [%s] did not match the returned class of [%s]!",
              entry.getPath(),
              entry.getDefault().getClass().getSimpleName(),
              value.getClass().getSimpleName()));
      log.warn(
          String.format(
              "To prevent errors for configuration entry [%s] the default value [%s] will be returned!",
              entry.getPath(), entry.getDefault()));
      return entry.getDefault();
    }
  }
}
