package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentTemplateSectionSummaryViewTest {

  @Test
  void titleWithSectionNumber_nullSectionNumber() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(
        DocumentTemplateSectionSummaryView.from(
            null,
            "condition title",
            documentTemplateSectionDto,
            DocumentTemplateSectionUrlsTestUtil.newBuilder().build()
        )
    )
        .extracting(DocumentTemplateSectionSummaryView::titleWithSectionNumber)
        .isEqualTo(documentTemplateSectionDto.title());
  }

  @Test
  void titleWithSectionNumber_withSectionNumber() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withTitle("title")
        .build();

    assertThat(
        DocumentTemplateSectionSummaryView.from(
            "1.2.3",
            "condition title",
            documentTemplateSectionDto,
            DocumentTemplateSectionUrlsTestUtil.newBuilder().build()
        )
    )
        .extracting(DocumentTemplateSectionSummaryView::titleWithSectionNumber)
        .isEqualTo("1.2.3 title");
  }

  @Test
  void from() {
    var sectionNumberString = "1.2.3";
    var conditionTitle = "Test condition title";
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateSectionUrls = DocumentTemplateSectionUrlsTestUtil.newBuilder().build();

    assertThat(
        DocumentTemplateSectionSummaryView.from(
            sectionNumberString,
            conditionTitle,
            documentTemplateSectionDto,
            documentTemplateSectionUrls
        )
    ).isEqualTo(
        new DocumentTemplateSectionSummaryView(
            sectionNumberString,
            documentTemplateSectionDto.title(),
            documentTemplateSectionDto.content(),
            conditionTitle,
            documentTemplateSectionDto.hasPageBreakBefore(),
            documentTemplateSectionUrls
        )
    );
  }
}
