package uk.co.fivium.digitaldocumentlibrary.document;

public record DocumentTemplateSummaryView(
    String title,
    String description,
    String viewUrl
) {

  static DocumentTemplateSummaryView from(DocumentTemplateDto documentTemplateDto, String viewUrl) {
    return new DocumentTemplateSummaryView(
        documentTemplateDto.title(),
        documentTemplateDto.description(),
        viewUrl
    );
  }
}
