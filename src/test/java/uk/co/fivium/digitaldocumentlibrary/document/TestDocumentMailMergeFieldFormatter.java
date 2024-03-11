package uk.co.fivium.digitaldocumentlibrary.document;

public class TestDocumentMailMergeFieldFormatter implements DocumentMailMergeFieldFormatter {

  @Override
  public String formatSuccess(String value) {
    return "%s (success)".formatted(value);
  }

  @Override
  public String formatError(String value) {
    return "%s (error)".formatted(value);
  }
}
