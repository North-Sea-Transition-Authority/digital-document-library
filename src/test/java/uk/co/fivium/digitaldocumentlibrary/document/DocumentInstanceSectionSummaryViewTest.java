package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class DocumentInstanceSectionSummaryViewTest {

  @Test
  void titleWithSectionNumber_nullSectionNumber() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(
        DocumentInstanceSectionSummaryView.from(
            null,
            documentInstanceSectionDto,
            ResolvedDocumentInstanceSectionTestUtil.newBuilder().build(),
            DocumentInstanceSectionUrlsTestUtil.newBuilder().build()
        )
    )
        .extracting(DocumentInstanceSectionSummaryView::titleWithSectionNumber)
        .isEqualTo(documentInstanceSectionDto.title());
  }

  @Test
  void titleWithSectionNumber_withSectionNumber() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(
        DocumentInstanceSectionSummaryView.from(
            "1.2.3",
            documentInstanceSectionDto,
            ResolvedDocumentInstanceSectionTestUtil.newBuilder().build(),
            DocumentInstanceSectionUrlsTestUtil.newBuilder().build()
        )
    )
        .extracting(DocumentInstanceSectionSummaryView::titleWithSectionNumber)
        .isEqualTo("1.2.3 title");
  }

  @Test
  void from_sectionNumbered() {
    var sectionNumberString = "1.2.3";
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var content = ResolvedDocumentInstanceSectionTestUtil.newBuilder().build();
    var documentInstanceSectionUrls = DocumentInstanceSectionUrlsTestUtil.newBuilder().build();

    assertThat(
        DocumentInstanceSectionSummaryView.from(
            sectionNumberString,
            documentInstanceSectionDto,
            content,
            documentInstanceSectionUrls
        )
    ).isEqualTo(
        new DocumentInstanceSectionSummaryView(
            documentInstanceSectionDto.nestingLevel(),
            sectionNumberString,
            documentInstanceSectionDto.title(),
            content.resolvedContent(),
            documentInstanceSectionDto.hasPageBreakBefore(),
            Collections.emptyList(),
            documentInstanceSectionUrls
        )
    );
  }
}
