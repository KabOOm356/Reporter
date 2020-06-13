package test.test.Answer;

import net.KabOOm356.Configuration.Entry;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ConfigurationAnswer implements Answer<Object> {
	public static final ConfigurationAnswer instance = new ConfigurationAnswer();
	private static final Logger log = LogManager.getLogger(ConfigurationAnswer.class);

	private ConfigurationAnswer() {
	}

	@Override
	public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
		Validate.notEmpty(invocationOnMock.getArguments());
		Object answer = null;
		if (invocationOnMock.getArguments()[0] instanceof Entry) {
			final Entry<?> entry = (Entry<?>) invocationOnMock.getArguments()[0];
			answer = entry.getDefault();
		} else if (invocationOnMock.getArguments().length >= 2) {
			answer = invocationOnMock.getArguments()[1];
		} else {
			log.warn("First parameter was not an Entry! Returning null!", invocationOnMock.getArguments());
		}
		return answer;
	}
}
