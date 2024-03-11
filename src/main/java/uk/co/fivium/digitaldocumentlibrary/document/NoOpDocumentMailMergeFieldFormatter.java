package uk.co.fivium.digitaldocumentlibrary.document;

public class NoOpDocumentMailMergeFieldFormatter implements DocumentMailMergeFieldFormatter {

  @Override
  public String formatSuccess(String value) {
    return value;
  }

  @Override
  public String formatError(String value) {
    return value;
  }
}
