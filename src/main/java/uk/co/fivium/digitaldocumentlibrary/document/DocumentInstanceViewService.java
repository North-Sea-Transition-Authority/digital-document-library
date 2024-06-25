package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class DocumentInstanceViewService {

  /**
   * Gets a list of summary views for a given list of document instance DTOs.
   *
   * @param documentInstanceDtos the list of document instance DTOs
   * @param viewUrlFunction a function that is used to generate a URL to a page to view each document instance section
   * @return the list of summary views
   */
  public List<DocumentInstanceSummaryView> getDocumentInstanceSummaryViews(
      Collection<DocumentInstanceDto> documentInstanceDtos,
      Function<DocumentInstanceDto, String> viewUrlFunction
  ) {
    return documentInstanceDtos
        .stream()
        .sorted(Comparator.comparingInt(documentInstance -> documentInstance.documentTemplateDto().displayOrder()))
        .map(documentInstanceDto ->
            DocumentInstanceSummaryView.from(documentInstanceDto, viewUrlFunction.apply(documentInstanceDto))
        )
        .toList();
  }
}
