package test.test;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@SuppressWarnings("EmptyClass")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public abstract class PowerMockitoTest {
}
