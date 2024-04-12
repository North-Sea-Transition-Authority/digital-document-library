package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class DocumentInstanceSectionViewService {

  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  DocumentInstanceSectionViewService(
      DocumentInstanceSectionService documentInstanceSectionService,
      DocumentMailMergeFieldService documentMailMergeFieldService
  ) {
    this.documentInstanceSectionService = documentInstanceSectionService;
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  public DocumentInstanceSectionsSummaryView getDocumentInstanceSectionsSummaryView(
      DocumentInstanceDto documentInstanceDto,
      Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls> urlsFunction,
      DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter
  ) {
    var topLevelDocumentInstanceSectionDtos =
        documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto);

    var topLevelDocumentInstanceSectionSummaryViews = getSiblingDocumentInstanceSectionSummaryViews(
        null,
        topLevelDocumentInstanceSectionDtos,
        urlsFunction,
        documentMailMergeFieldFormatter
    );

    var errorMessages = topLevelDocumentInstanceSectionSummaryViews
        .stream()
        .flatMap(view ->
            Stream.concat(
                view.errorMessages().stream(),
                view.descendants().stream().flatMap(descendant -> descendant.errorMessages().stream())
            )
        )
        .distinct()
        .toList();

    return new DocumentInstanceSectionsSummaryView(topLevelDocumentInstanceSectionSummaryViews, errorMessages);
  }

  List<DocumentInstanceSectionSummaryView> getSiblingDocumentInstanceSectionSummaryViews(
      String parentSectionNumberString,
      List<DocumentInstanceSectionDto> siblingDocumentInstanceSectionDtos,
      Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls> urlsFunction,
      DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter
  ) {
    var documentInstanceSectionSummaryViews = new ArrayList<DocumentInstanceSectionSummaryView>();

    var sortedSiblingDocumentInstanceSectionDtos = siblingDocumentInstanceSectionDtos.stream()
        .sorted(Comparator.comparingInt(DocumentInstanceSectionDto::displayOrder))
        .toList();

    var currentSectionNumber = 0;

    for (var documentInstanceSectionDto : sortedSiblingDocumentInstanceSectionDtos) {
      String sectionNumberString;

      if (documentInstanceSectionDto.numbered()) {
        currentSectionNumber++;

        sectionNumberString = DocumentSectionNumberingUtil.getFullNumberSectionNumberString(
            parentSectionNumberString,
            currentSectionNumber
        );
      } else {
        sectionNumberString = null;
      }

      var resolvedDocumentInstanceSection = documentMailMergeFieldService.resolveMailMergeFields(
          documentInstanceSectionDto,
          documentMailMergeFieldFormatter
      );

      var children = getSiblingDocumentInstanceSectionSummaryViews(
          sectionNumberString,
          documentInstanceSectionDto.children(),
          urlsFunction,
          documentMailMergeFieldFormatter
      );

      var documentInstanceSectionSummaryView = DocumentInstanceSectionSummaryView.from(
          sectionNumberString,
          documentInstanceSectionDto,
          resolvedDocumentInstanceSection,
          urlsFunction.apply(documentInstanceSectionDto),
          children
      );
      documentInstanceSectionSummaryViews.add(documentInstanceSectionSummaryView);
    }

    return documentInstanceSectionSummaryViews;
  }
}
