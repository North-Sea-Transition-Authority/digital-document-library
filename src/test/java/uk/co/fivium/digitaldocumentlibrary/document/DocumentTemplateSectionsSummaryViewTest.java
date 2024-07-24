package uk.co.fivium.digitaldocumentlibrary.document;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTemplateSectionsSummaryViewTest {

  @Test
  void from() {
    var topLevelDocumentTemplateSectionSummaryView1 = DocumentTemplateSectionSummaryViewTestUtil.newBuilder()
        .withErrorMessages(List.of("Error message 1", "Error message 2"))
        .withMailMergeResolvedValuesByMnemonic(Map.of(
            "ISSUE_DATE", "Friday 26th April 2024",
            "RECIPIENT", "test@example.com"
        ))
        .withChildren(List.of(
            DocumentTemplateSectionSummaryViewTestUtil.newBuilder()
                .withMailMergeResolvedValuesByMnemonic(Map.of("RECIPIENT", "test@example.com"))
                .withErrorMessages(List.of("Error message 2", "Error message 1"))
                .build()
        ))
        .build();

    var topLevelDocumentTemplateSectionSummaryView2 = DocumentTemplateSectionSummaryViewTestUtil.newBuilder()
        .withErrorMessages(List.of("Error message 1", "Error message 3"))
        .withChildren(List.of(
            DocumentTemplateSectionSummaryViewTestUtil.newBuilder()
                .withErrorMessages(List.of("Error message 2", "Error message 4"))
                .build(),
            DocumentTemplateSectionSummaryViewTestUtil.newBuilder()
                .withMailMergeResolvedValuesByMnemonic(Map.of(
                    "ISSUE_DATE", "Friday 26th April 2024",
                    "FOO", "BAR"
                ))
                .withErrorMessages(List.of("Error message 5", "Error message 4"))
                .withChildren(List.of(
                    DocumentTemplateSectionSummaryViewTestUtil.newBuilder()
                        .withErrorMessages(List.of("Error message 6", "Error message 3"))
                        .withMailMergeResolvedValuesByMnemonic(Map.of(
                            "ADDRESS", "15 Adam Street",
                            "FOO", "BAR"
                        ))
                        .build()
                ))
                .build()
        ))
        .build();

    var topLevelDocumentTemplateSectionSummaryViews = List.of(
        topLevelDocumentTemplateSectionSummaryView1,
        topLevelDocumentTemplateSectionSummaryView2
    );

    assertThat(DocumentTemplateSectionsSummaryView.from(topLevelDocumentTemplateSectionSummaryViews))
        .isEqualTo(new DocumentTemplateSectionsSummaryView(
            topLevelDocumentTemplateSectionSummaryViews,
            List.of(
                "Error message 1",
                "Error message 2",
                "Error message 3",
                "Error message 4",
                "Error message 5",
                "Error message 6"
            ),
            Map.of(
                "ADDRESS", "15 Adam Street",
                "ISSUE_DATE", "Friday 26th April 2024",
                "RECIPIENT", "test@example.com",
                "FOO", "BAR"
            )
        ));
  }

}