package uk.co.fivium.digitaldocumentlibrary.document;

public record DocumentTemplateSectionUrls(
    String addSectionBeforeUrl,
    String addSectionAfterUrl,
    String addSubsectionUrl,
    String editUrl,
    String removeUrl
) {
}
