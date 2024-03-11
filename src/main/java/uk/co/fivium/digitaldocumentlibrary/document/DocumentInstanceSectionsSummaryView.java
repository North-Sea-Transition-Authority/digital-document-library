package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;

public record DocumentInstanceSectionsSummaryView(
    List<DocumentInstanceSectionSummaryView> sectionSummaryViews,
    List<String> errorMessages
) {

}
