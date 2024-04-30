package uk.co.fivium.digitaldocumentlibrary.document;

import org.springframework.core.io.ByteArrayResource;

public record PdfRenderResult(
    ByteArrayResource pdfContent,
    String pdfHtml
) {
}
