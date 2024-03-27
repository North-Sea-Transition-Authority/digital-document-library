package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;

public record DocumentInstanceSectionsSummaryView(
    List<DocumentInstanceSectionSummaryView> topLevelDocumentInstanceSectionSummaryViews,
    List<String> errorMessages
) {

}
