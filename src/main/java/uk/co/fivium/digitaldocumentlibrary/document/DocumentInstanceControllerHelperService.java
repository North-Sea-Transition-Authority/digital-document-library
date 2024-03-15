package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class DocumentInstanceControllerHelperService {

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
