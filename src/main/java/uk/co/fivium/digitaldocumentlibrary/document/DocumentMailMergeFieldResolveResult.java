package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;

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
    return new DocumentMailMergeFieldResolveResult(value, false, null);
  }

  public static DocumentMailMergeFieldResolveResult error(String error) {
    return new DocumentMailMergeFieldResolveResult(null, true, error);
  }

}
