package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.UUID;

public record DocumentInstanceSummaryView(
    UUID documentInstanceId,
    String title,
    String description,
    String viewUrl
) {

  static DocumentInstanceSummaryView from(DocumentInstanceDto documentInstanceDto, String viewUrl) {
    return new DocumentInstanceSummaryView(
        documentInstanceDto.id(),
        documentInstanceDto.title(),
        documentInstanceDto.description(),
        viewUrl
    );
  }
}
