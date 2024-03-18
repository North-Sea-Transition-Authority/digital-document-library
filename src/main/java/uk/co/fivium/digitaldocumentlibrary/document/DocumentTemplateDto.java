package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.UUID;

public record DocumentTemplateDto(
    UUID id,
    String mnemonic,
    String title,
    String description,
    String documentInstancePdfTemplatePath,
    int displayOrder
) {

  static DocumentTemplateDto from(DocumentTemplate documentTemplate) {
    return new DocumentTemplateDto(
        documentTemplate.getId(),
        documentTemplate.getMnemonic(),
        documentTemplate.getTitle(),
        documentTemplate.getDescription(),
        documentTemplate.getDocumentInstancePdfTemplatePath(),
        documentTemplate.getDisplayOrder()
    );
  }
}
