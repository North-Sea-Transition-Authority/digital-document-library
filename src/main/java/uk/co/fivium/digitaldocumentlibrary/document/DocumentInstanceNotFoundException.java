package uk.co.fivium.digitaldocumentlibrary.document;

class DocumentInstanceNotFoundException extends RuntimeException {

  DocumentInstanceNotFoundException(String message) {
    super(message);
  }
}
