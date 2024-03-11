package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DocumentMailMergeFieldResolveResultTest {

  @Test
  void success() {
    var value = "value";

    var resolveResult = DocumentMailMergeFieldResolveResult.success(value);

    assertThat(resolveResult).isEqualTo(new DocumentMailMergeFieldResolveResult(value, false, null));
    assertThat(resolveResult.resolvedValueOrThrow()).isEqualTo(value);
  }

  @Test
  void error() {
    var errorMessage = "error message";

    var resolveResult = DocumentMailMergeFieldResolveResult.error(errorMessage);

    assertThat(resolveResult).isEqualTo(new DocumentMailMergeFieldResolveResult(null, true, errorMessage));
    assertThatThrownBy(resolveResult::resolvedValueOrThrow)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot get resolved value for ResolveResult which has an error. %s".formatted(errorMessage));
  }


}
