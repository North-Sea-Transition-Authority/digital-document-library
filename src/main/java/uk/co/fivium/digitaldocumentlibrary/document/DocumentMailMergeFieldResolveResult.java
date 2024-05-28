package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import org.apache.commons.text.StringEscapeUtils;

public record DocumentMailMergeFieldResolveResult(
    @Nullable String resolvedValue,
    boolean hasError,
    @Nullable String errorMessage
) {

  public String resolvedValueOrThrow() {
    if (hasError) {
      throw new IllegalStateException("Cannot get resolved value for ResolveResult which has an error. %s"
          .formatted(errorMessage));
    }

    return resolvedValue;
  }

  public static DocumentMailMergeFieldResolveResult success(String value) {
    // Escape the value as consumers will render the entire section content including resolved mail merge fields in a freemarker
    // template with no_esc to support rich text HTML section content and characters such as & in mail merge values will break
    // openhtmltopdf.
    return new DocumentMailMergeFieldResolveResult(StringEscapeUtils.escapeXml11(value), false, null);
  }

  public static DocumentMailMergeFieldResolveResult successNoEsc(String value) {
    return new DocumentMailMergeFieldResolveResult(value, false, null);
  }

  public static DocumentMailMergeFieldResolveResult error(String errorMessage) {
    // Escape the error message for the same reason as we escape the value above.
    return new DocumentMailMergeFieldResolveResult(null, true, StringEscapeUtils.escapeXml11(errorMessage));
  }
}
