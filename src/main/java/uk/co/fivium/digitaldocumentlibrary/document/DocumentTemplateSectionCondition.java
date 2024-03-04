package uk.co.fivium.digitaldocumentlibrary.document;

public interface DocumentTemplateSectionCondition {

  String getMnemonic();

  String getTitle();

  boolean isApplicable(DocumentTemplateDto documentTemplateDto);

  boolean evaluate(DocumentInstanceDto documentInstanceDto);
}
