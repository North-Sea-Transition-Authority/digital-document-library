package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
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

  /**
   * Gets a document sections summary view for a given document instance DTO.
   *
   * @param documentInstanceDto the document instance DTO
   * @param urlsFunction A function that is used to generate a DocumentInstanceSectionUrls object with URLs to perform actions
   *                     on the section
   * @param documentMailMergeFieldFormatter A class to define how mail merge fields are formatted once they've been resolved
   * @return the list of summary views
   */
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
        new DocumentSectionMailMergeFieldResolver(documentMailMergeFieldService, documentMailMergeFieldFormatter)
    );

    return DocumentInstanceSectionsSummaryView.from(topLevelDocumentInstanceSectionSummaryViews);
  }

  List<DocumentInstanceSectionSummaryView> getSiblingDocumentInstanceSectionSummaryViews(
      String parentSectionNumberString,
      List<DocumentInstanceSectionDto> siblingDocumentInstanceSectionDtos,
      Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls> urlsFunction,
      DocumentSectionMailMergeFieldResolver documentSectionMailMergeFieldResolver
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

      var resolvedDocumentInstanceSection = documentSectionMailMergeFieldResolver.resolve(documentInstanceSectionDto);

      var children = getSiblingDocumentInstanceSectionSummaryViews(
          sectionNumberString,
          documentInstanceSectionDto.children(),
          urlsFunction,
          documentSectionMailMergeFieldResolver
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
