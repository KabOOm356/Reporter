package test.test.Answer;

import net.KabOOm356.Configuration.Entry;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class LocaleEntryAnswer implements Answer<String> {
  public static final LocaleEntryAnswer instance = new LocaleEntryAnswer();
  private static final Logger log = LogManager.getLogger(LocaleEntryAnswer.class);

  private LocaleEntryAnswer() {}

  @Override
  public String answer(final InvocationOnMock invocationOnMock) throws Throwable {
    Validate.notEmpty(invocationOnMock.getArguments());
    String answer = "";
    if (invocationOnMock.getArguments()[0] instanceof Entry) {
      final Entry entry = (Entry) invocationOnMock.getArguments()[0];
      answer = entry.getDefault().toString();
    } else {
      log.warn(
          "First parameter was not an Entry! Returning an empty string!",
          invocationOnMock.getArguments());
    }
    return answer;
  }
}
