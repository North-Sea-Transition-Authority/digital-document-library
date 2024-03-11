package uk.co.fivium.digitaldocumentlibrary.document;

public interface DocumentMailMergeField {

  String getMnemonic();

  String getDescription();

  boolean isApplicable(DocumentTemplateDto documentTemplateDto);

  DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto);
}
