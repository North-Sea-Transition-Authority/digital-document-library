package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class DocumentTemplateViewService {

  private final DocumentTemplateService documentTemplateService;

  DocumentTemplateViewService(DocumentTemplateService documentTemplateService) {
    this.documentTemplateService = documentTemplateService;
  }

  /**
   * Gets a list of summary views for all document templates.
   *
   * @param viewUrlFunction a function that is used to generate a URL to a page to view each document template section
   * @return the list of summary views
   */
  public List<DocumentTemplateSummaryView> getDocumentTemplateSummaryViews(
      Function<DocumentTemplateDto, String> viewUrlFunction
  ) {
    return documentTemplateService.getDocumentTemplateDtos().stream()
        .sorted(Comparator.comparingInt(DocumentTemplateDto::displayOrder))
        .map(documentTemplateDto ->
            DocumentTemplateSummaryView.from(documentTemplateDto, viewUrlFunction.apply(documentTemplateDto))
        )
        .toList();
  }
}
