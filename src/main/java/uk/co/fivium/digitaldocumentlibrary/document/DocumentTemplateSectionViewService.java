package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class DocumentTemplateSectionViewService {

  private final DocumentTemplateSectionService documentTemplateSectionService;
  private final DocumentTemplateSectionConditionService documentTemplateSectionConditionService;
  private final DocumentMailMergeFieldService documentMailMergeFieldService;


  DocumentTemplateSectionViewService(
      DocumentTemplateSectionService documentTemplateSectionService,
      DocumentTemplateSectionConditionService documentTemplateSectionConditionService,
      DocumentMailMergeFieldService documentMailMergeFieldService
  ) {
    this.documentTemplateSectionService = documentTemplateSectionService;
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  /**
   * Gets a document sections summary view for a given document template DTO.
   *
   * @param documentTemplateDto the document template DTO
   * @param urlsFunction A function that is used to generate a DocumentTemplateSectionUrls object with URLs to perform actions
   *                     on the section
   * @param documentMailMergeFieldFormatter A class to define how mail merge fields are formatted once they've been resolved
   * @return the list of summary views
   */
  public DocumentTemplateSectionsSummaryView getDocumentTemplateSectionsSummaryView(
      DocumentTemplateDto documentTemplateDto,
      Function<DocumentTemplateSectionDto, DocumentTemplateSectionUrls> urlsFunction,
      DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter
  ) {
    var topLevelDocumentTemplateSectionDtos =
        documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto);

    var topLevelDocumentTemplateSectionSummaryViews = getSiblingDocumentTemplateSectionSummaryViews(
        null,
        topLevelDocumentTemplateSectionDtos,
        urlsFunction,
        new DocumentSectionMailMergeFieldResolver(documentMailMergeFieldService, documentMailMergeFieldFormatter)
    );

    return DocumentTemplateSectionsSummaryView.from(topLevelDocumentTemplateSectionSummaryViews);
  }

  List<DocumentTemplateSectionSummaryView> getSiblingDocumentTemplateSectionSummaryViews(
      String parentSectionNumberString,
      List<DocumentTemplateSectionDto> siblingDocumentTemplateSectionDtos,
      Function<DocumentTemplateSectionDto, DocumentTemplateSectionUrls> urlsFunction,
      DocumentSectionMailMergeFieldResolver documentSectionMailMergeFieldResolver) {
    var documentTemplateSectionSummaryViews = new ArrayList<DocumentTemplateSectionSummaryView>();

    var sortedSiblingDocumentTemplateSectionDtos = siblingDocumentTemplateSectionDtos.stream()
        .sorted(Comparator.comparingInt(DocumentTemplateSectionDto::displayOrder))
        .toList();

    var currentSectionNumber = 0;

    for (var documentTemplateSectionDto : sortedSiblingDocumentTemplateSectionDtos) {
      String sectionNumberString;

      if (documentTemplateSectionDto.numbered()) {
        currentSectionNumber++;

        sectionNumberString = DocumentSectionNumberingUtil.getFullNumberSectionNumberString(
            parentSectionNumberString,
            currentSectionNumber
        );
      } else {
        sectionNumberString = null;
      }

      var conditionMnemonic = documentTemplateSectionDto.conditionMnemonic();

      String conditionTitle = null;
      if (conditionMnemonic != null) {
        conditionTitle = documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateSectionDto.documentTemplateDto(),
            conditionMnemonic
        ).getTitle();
      }

      var resolvedDocumentTemplateSection = documentSectionMailMergeFieldResolver.resolve(documentTemplateSectionDto);

      var children = getSiblingDocumentTemplateSectionSummaryViews(
          sectionNumberString,
          documentTemplateSectionDto.children(),
          urlsFunction,
          documentSectionMailMergeFieldResolver);

      var documentTemplateSectionSummaryView = DocumentTemplateSectionSummaryView.from(
          sectionNumberString,
          conditionTitle,
          documentTemplateSectionDto,
          resolvedDocumentTemplateSection,
          urlsFunction.apply(documentTemplateSectionDto),
          children
      );
      documentTemplateSectionSummaryViews.add(documentTemplateSectionSummaryView);
    }

    return documentTemplateSectionSummaryViews;
  }
}
