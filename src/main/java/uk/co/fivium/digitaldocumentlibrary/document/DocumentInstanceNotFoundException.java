package uk.co.fivium.digitaldocumentlibrary.document;

public class DocumentInstanceNotFoundException extends RuntimeException {

  DocumentInstanceNotFoundException(String message) {
    super(message);
  }
}
