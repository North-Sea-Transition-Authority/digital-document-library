package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentMailMergeFieldViewService {

  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  DocumentMailMergeFieldViewService(DocumentMailMergeFieldService documentMailMergeFieldService) {
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  public List<DocumentMailMergeFieldView> getApplicableDocumentMailMergeFieldViews(DocumentTemplateDto documentTemplateDto) {
    return documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto).stream()
        .map(DocumentMailMergeFieldView::from)
        .toList();
  }
}
