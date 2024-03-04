package uk.co.fivium.digitaldocumentlibrary.document;

public record DocumentMailMergeFieldView(String mnemonic, String description) {

  static DocumentMailMergeFieldView from(DocumentMailMergeField documentMailMergeField) {
    return new DocumentMailMergeFieldView(
        documentMailMergeField.getMnemonic(),
        documentMailMergeField.getDescription()
    );
  }
}
