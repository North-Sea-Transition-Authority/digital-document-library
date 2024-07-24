package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

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
            ResolvedDocumentSectionTestUtil.newBuilder().build(),
            DocumentTemplateSectionUrlsTestUtil.newBuilder().build(),
            List.of())
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
            ResolvedDocumentSectionTestUtil.newBuilder().build(),
            DocumentTemplateSectionUrlsTestUtil.newBuilder().build(),
            List.of())
    )
        .extracting(DocumentTemplateSectionSummaryView::titleWithSectionNumber)
        .isEqualTo("1.2.3 title");
  }

  @Test
  void descendants() {
    var documentTemplateSectionSummaryViewChild1Child1 = DocumentTemplateSectionSummaryViewTestUtil.newBuilder().build();
    var documentTemplateSectionSummaryViewChild1 = DocumentTemplateSectionSummaryViewTestUtil.newBuilder()
        .withChildren(List.of(documentTemplateSectionSummaryViewChild1Child1))
        .build();
    var documentTemplateSectionSummaryViewChild2 = DocumentTemplateSectionSummaryViewTestUtil.newBuilder().build();
    var documentTemplateSectionSummaryView = DocumentTemplateSectionSummaryViewTestUtil.newBuilder()
        .withChildren(List.of(documentTemplateSectionSummaryViewChild1, documentTemplateSectionSummaryViewChild2))
        .build();

    assertThat(documentTemplateSectionSummaryView.descendants()).containsExactly(
        documentTemplateSectionSummaryViewChild1,
        documentTemplateSectionSummaryViewChild1Child1,
        documentTemplateSectionSummaryViewChild2
    );
  }

  @Test
  void from() {
    var sectionNumberString = "1.2.3";
    var conditionTitle = "Test condition title";
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateSectionUrls = DocumentTemplateSectionUrlsTestUtil.newBuilder().build();
    var children = List.of(mock(DocumentTemplateSectionSummaryView.class));

    var resolvedDocumentSection = ResolvedDocumentSectionTestUtil.newBuilder()
        .withResolvedDocumentMailMergeField(List.of(
            ResolvedDocumentMailMergeFieldTestUtil.newBuilder()
                .withDocumentMailMergeField(DocumentMailMergeFieldTestUtil.builder()
                    .withMnemonic("KEY_1")
                    .build())
                .withDocumentMailMergeFieldResolveResult(DocumentMailMergeFieldResolveResult.success("VALUE_1"))
                .build(),
            ResolvedDocumentMailMergeFieldTestUtil.newBuilder()
                .withDocumentMailMergeField(DocumentMailMergeFieldTestUtil.builder()
                    .withMnemonic("KEY_2")
                    .build())
                .withDocumentMailMergeFieldResolveResult(DocumentMailMergeFieldResolveResult.success("VALUE_2"))
                .build()
        ))
        .build();

    var mailMergeResolvedValuesByMnemonic = Map.of(
        "KEY_1", "VALUE_1",
        "KEY_2", "VALUE_2"
    );

    assertThat(
        DocumentTemplateSectionSummaryView.from(
            sectionNumberString,
            conditionTitle,
            documentTemplateSectionDto,
            resolvedDocumentSection,
            documentTemplateSectionUrls,
            children)
    ).isEqualTo(
        new DocumentTemplateSectionSummaryView(
            sectionNumberString,
            documentTemplateSectionDto.title(),
            resolvedDocumentSection.resolvedContent(),
            conditionTitle,
            documentTemplateSectionDto.hasPageBreakBefore(),
            List.of(),
            mailMergeResolvedValuesByMnemonic,
            documentTemplateSectionUrls,
            children
        )
    );
  }
}
