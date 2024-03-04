package uk.co.fivium.digitaldocumentlibrary.document;

record DocumentMailMergeValidationResult(boolean isValid, String errorMessage) {

  static DocumentMailMergeValidationResult valid() {
    return new DocumentMailMergeValidationResult(true, null);
  }

  static DocumentMailMergeValidationResult invalid(String errorMessage) {
    return new DocumentMailMergeValidationResult(false, errorMessage);
  }
}
