package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DocumentInstanceSectionsSummaryViewTest {

  @Test
  void from() {
    var topLevelDocumentInstanceSectionSummaryView1 = DocumentInstanceSectionSummaryViewTestUtil.newBuilder()
        .withErrorMessages(List.of("Error message 1", "Error message 2"))
        .withMailMergeResolvedValuesByMnemonic(Map.of(
            "ISSUE_DATE", "Friday 26th April 2024",
            "RECIPIENT", "test@example.com"
        ))
        .withChildren(List.of(
            DocumentInstanceSectionSummaryViewTestUtil.newBuilder()
                .withMailMergeResolvedValuesByMnemonic(Map.of("RECIPIENT", "test@example.com"))
                .withErrorMessages(List.of("Error message 2", "Error message 1"))
                .build()
        ))
        .build();

    var topLevelDocumentInstanceSectionSummaryView2 = DocumentInstanceSectionSummaryViewTestUtil.newBuilder()
        .withErrorMessages(List.of("Error message 1", "Error message 3"))
        .withChildren(List.of(
            DocumentInstanceSectionSummaryViewTestUtil.newBuilder()
                .withErrorMessages(List.of("Error message 2", "Error message 4"))
                .build(),
            DocumentInstanceSectionSummaryViewTestUtil.newBuilder()
                .withMailMergeResolvedValuesByMnemonic(Map.of(
                    "ISSUE_DATE", "Friday 26th April 2024",
                    "FOO", "BAR"
                ))
                .withErrorMessages(List.of("Error message 5", "Error message 4"))
                .withChildren(List.of(
                    DocumentInstanceSectionSummaryViewTestUtil.newBuilder()
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

    var topLevelDocumentInstanceSectionSummaryViews = List.of(
        topLevelDocumentInstanceSectionSummaryView1,
        topLevelDocumentInstanceSectionSummaryView2
    );

    assertThat(DocumentInstanceSectionsSummaryView.from(topLevelDocumentInstanceSectionSummaryViews))
        .isEqualTo(new DocumentInstanceSectionsSummaryView(
            topLevelDocumentInstanceSectionSummaryViews,
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
