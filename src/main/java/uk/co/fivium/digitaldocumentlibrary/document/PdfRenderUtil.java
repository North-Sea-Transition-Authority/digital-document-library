package uk.co.fivium.digitaldocumentlibrary.document;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.core.io.ByteArrayResource;

public class PdfRenderUtil {

  private PdfRenderUtil() {
    throw new IllegalStateException("Cannot instantiate PdfRenderUtil as it's a utils class");
  }

  public static ByteArrayResource renderPdfFromHtml(String html) throws IOException {
    var pdfRendererBuilder = new PdfRendererBuilder();
    pdfRendererBuilder.withHtmlContent(html, "classpath://");

    try (var outputStream = new ByteArrayOutputStream()) {
      pdfRendererBuilder.toStream(outputStream);
      pdfRendererBuilder.run();

      return new ByteArrayResource(outputStream.toByteArray());
    }
  }
}
