package uk.co.fivium.digitaldocumentlibrary.document;

public interface DocumentMailMergeFieldFormatter {

  String formatSuccess(String value);

  String formatError(String value);

  String formatFootnotes(String value);

  static NoOpDocumentMailMergeFieldFormatter noOp() {
    return NoOpDocumentMailMergeFieldFormatter.INSTANCE;
  }
}
