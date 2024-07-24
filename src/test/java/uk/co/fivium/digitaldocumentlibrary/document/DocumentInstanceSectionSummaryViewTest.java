package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            ResolvedDocumentSectionTestUtil.newBuilder().build(),
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
            ResolvedDocumentSectionTestUtil.newBuilder().build(),
            DocumentInstanceSectionUrlsTestUtil.newBuilder().build(),
            List.of()
        )
    )
        .extracting(DocumentInstanceSectionSummaryView::titleWithSectionNumber)
        .isEqualTo("1.2.3 title");
  }

  @Test
  void descendants() {
    var documentInstanceSectionSummaryViewChild1Child1 = DocumentInstanceSectionSummaryViewTestUtil.newBuilder().build();
    var documentInstanceSectionSummaryViewChild1 = DocumentInstanceSectionSummaryViewTestUtil.newBuilder()
        .withChildren(List.of(documentInstanceSectionSummaryViewChild1Child1))
        .build();
    var documentInstanceSectionSummaryViewChild2 = DocumentInstanceSectionSummaryViewTestUtil.newBuilder().build();
    var documentInstanceSectionSummaryView = DocumentInstanceSectionSummaryViewTestUtil.newBuilder()
        .withChildren(List.of(documentInstanceSectionSummaryViewChild1, documentInstanceSectionSummaryViewChild2))
        .build();

    assertThat(documentInstanceSectionSummaryView.descendants()).containsExactly(
        documentInstanceSectionSummaryViewChild1,
        documentInstanceSectionSummaryViewChild1Child1,
        documentInstanceSectionSummaryViewChild2
    );
  }

  @Test
  void from_sectionNumbered() {
    var sectionNumberString = "1.2.3";
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var content = ResolvedDocumentSectionTestUtil.newBuilder()
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
    var documentInstanceSectionUrls = DocumentInstanceSectionUrlsTestUtil.newBuilder().build();
    var children = List.of(mock(DocumentInstanceSectionSummaryView.class));

    var mailMergeResolvedValuesByMnemonic = Map.of(
        "KEY_1", "VALUE_1",
        "KEY_2", "VALUE_2"
    );

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
            sectionNumberString,
            documentInstanceSectionDto.title(),
            content.resolvedContent(),
            documentInstanceSectionDto.hasPageBreakBefore(),
            Collections.emptyList(),
            mailMergeResolvedValuesByMnemonic,
            documentInstanceSectionUrls,
            children
        )
    );
  }
}
