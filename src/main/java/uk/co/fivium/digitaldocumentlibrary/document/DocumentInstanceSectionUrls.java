package uk.co.fivium.digitaldocumentlibrary.document;

public record DocumentInstanceSectionUrls(
    String addSectionBeforeUrl,
    String addSectionAfterUrl,
    String addSubsectionUrl,
    String editUrl,
    String removeUrl
) {
}
