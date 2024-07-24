package uk.co.fivium.digitaldocumentlibrary.document;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PdfRenderUtilTest {
    @Test
    void renderPdfFromHtml() throws IOException {
        assertThat(PdfRenderUtil.renderPdfFromHtml("<html></html>").getByteArray()).isNotEmpty();
    }
}