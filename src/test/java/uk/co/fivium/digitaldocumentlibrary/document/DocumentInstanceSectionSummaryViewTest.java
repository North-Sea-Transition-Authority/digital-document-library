package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
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
            DocumentInstanceSectionUrlsTestUtil.newBuilder().build(),
            List.of()
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
            DocumentInstanceSectionUrlsTestUtil.newBuilder().build(),
            List.of()
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
    var children = List.of(mock(DocumentInstanceSectionSummaryView.class));

    assertThat(
        DocumentInstanceSectionSummaryView.from(
            sectionNumberString,
            documentInstanceSectionDto,
            content,
            documentInstanceSectionUrls,
            children
        )
    ).isEqualTo(
        new DocumentInstanceSectionSummaryView(
            documentInstanceSectionDto.nestingLevel(),
            sectionNumberString,
            documentInstanceSectionDto.title(),
            content.resolvedContent(),
            documentInstanceSectionDto.hasPageBreakBefore(),
            Collections.emptyList(),
            documentInstanceSectionUrls,
            children
        )
    );
  }
}
