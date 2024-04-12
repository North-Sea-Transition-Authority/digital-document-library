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
