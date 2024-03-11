package uk.co.fivium.digitaldocumentlibrary.document;

public class DocumentMailMergeFieldResolveResultTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String resolvedValue = "Resolved value";
    private boolean hasError;
    private String errorMessage;

    public Builder withResolvedValue(String resolvedValue) {
      this.resolvedValue = resolvedValue;
      return this;
    }

    public Builder withHasError(boolean isError) {
      this.hasError = isError;
      return this;
    }

    public Builder withErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public DocumentMailMergeFieldResolveResult build() {
      return new DocumentMailMergeFieldResolveResult(
          resolvedValue,
          hasError,
          errorMessage
      );
    }

  }

}
