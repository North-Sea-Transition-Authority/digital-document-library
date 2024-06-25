package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentMailMergeFieldViewService {

  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  DocumentMailMergeFieldViewService(DocumentMailMergeFieldService documentMailMergeFieldService) {
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  /**
   * Gets a list of document mail merge field views for mail merge fields that are applicable to a given document template DTO.
   *
   * @param documentTemplateDto the document template DTO
   * @return the list of mail merge field views
   */
  public List<DocumentMailMergeFieldView> getApplicableDocumentMailMergeFieldViews(DocumentTemplateDto documentTemplateDto) {
    return documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto).stream()
        .map(DocumentMailMergeFieldView::from)
        .toList();
  }
}
