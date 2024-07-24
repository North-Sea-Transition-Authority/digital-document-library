package uk.co.fivium.digitaldocumentlibrary.document;

public interface DocumentMailMergeField {

  String getMnemonic();

  String getDescription();

  boolean isApplicable(DocumentTemplateDto documentTemplateDto);

  DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto);

  default DocumentMailMergeFieldResolveResult resolve(DocumentTemplateDto documentTemplateDto) {
    return DocumentMailMergeFieldResolveResult.success("((%s))".formatted(getMnemonic()));
  }
}
