package uk.co.fivium.digitaldocumentlibrary.document;

public class NoOpDocumentMailMergeFieldFormatter implements DocumentMailMergeFieldFormatter {

  static final NoOpDocumentMailMergeFieldFormatter INSTANCE = new NoOpDocumentMailMergeFieldFormatter();

  @Override
  public String formatSuccess(String value) {
    return value;
  }

  @Override
  public String formatError(String value) {
    return value;
  }

  @Override
  public String formatFootnotes(String value) {
    return value;
  }
}
