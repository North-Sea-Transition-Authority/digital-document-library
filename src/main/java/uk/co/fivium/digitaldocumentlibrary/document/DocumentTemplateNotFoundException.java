package uk.co.fivium.digitaldocumentlibrary.document;

public class DocumentTemplateNotFoundException extends RuntimeException {

  DocumentTemplateNotFoundException(String message) {
    super(message);
  }
}
