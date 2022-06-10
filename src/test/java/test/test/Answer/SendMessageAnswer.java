package test.test.Answer;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@SuppressWarnings("unused")
public class SendMessageAnswer implements Answer<Object> {
  public static final SendMessageAnswer instance = new SendMessageAnswer();
  private static final Logger log = LogManager.getLogger(SendMessageAnswer.class);

  @Override
  public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
    Validate.isTrue(invocationOnMock.getArguments().length > 0);
    final String message = invocationOnMock.getArguments()[0].toString();
    log.info(String.format("Sending message to sender [%s]", message));
    return new Object();
  }
}
