package uk.co.fivium.digitaldocumentlibrary.document;

class DocumentMailMergeFieldUtil {

  private DocumentMailMergeFieldUtil() {
    throw new IllegalStateException("Utility class");
  }

  static String getMnemonicFromMailMergeFieldText(String mailMergeFieldText) {
    return mailMergeFieldText.substring(2, mailMergeFieldText.length() - 2);
  }

}
