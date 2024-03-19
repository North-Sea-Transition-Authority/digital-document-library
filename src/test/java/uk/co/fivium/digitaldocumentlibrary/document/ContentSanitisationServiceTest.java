package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.fivium.digitaldocumentlibrary.document.ContentSanitisationService;

class ContentSanitisationServiceTest {

  private ContentSanitisationService contentSanitisationService;

  @BeforeEach
  void setUp() {
    contentSanitisationService = new ContentSanitisationService();
  }

  @ParameterizedTest
  @MethodSource("sanitiseHtmlArguments")
  void sanitiseHtml(String input, String output) {
    assertThat(contentSanitisationService.getSanitisedContent(input)).isEqualTo(output);
  }

  private static Stream<Arguments> sanitiseHtmlArguments() {
    return Stream.of(
        arguments(
            "some text, ((MY_MAIL_MERGE_FIELD)) there's no html here...",
            "some text, ((MY_MAIL_MERGE_FIELD)) there's no html here..."
        ),
        arguments(
            "<p>some content inside a paragraph</p>",
            "<p>some content inside a paragraph</p>"
        ),
        arguments(
            "<script>alert('oh no');</script><p>content</p>",
            "<p>content</p>"
        ),
        arguments(
            "<p onload=alert('oh no')>content</p>",
            "<p>content</p>"
        ),
        arguments(
            "<p onmouseover=alert('oh no')>content</p>",
            "<p>content</p>"
        ),
        arguments(
            "<p onmouseover=alert('oh no')>((MAIL_MERGE_CONTENT))</p>",
            "<p>((MAIL_MERGE_CONTENT))</p>"
        )
    );
  }

}
