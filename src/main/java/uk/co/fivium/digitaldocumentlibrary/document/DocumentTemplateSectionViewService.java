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

  DocumentTemplateSectionViewService(
      DocumentTemplateSectionService documentTemplateSectionService,
      DocumentTemplateSectionConditionService documentTemplateSectionConditionService
  ) {
    this.documentTemplateSectionService = documentTemplateSectionService;
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
  }

  /**
   * Gets a list of top level document template section summary views for a given document template DTO.
   *
   * @param documentTemplateDto the document template DTO
   * @param urlsFunction A function that is used to generate a DocumentTemplateSectionUrls object with URLs to perform actions
   *                     on the section
   * @return the list of summary views
   */
  public List<DocumentTemplateSectionSummaryView> getTopLevelDocumentTemplateSectionSummaryViews(
      DocumentTemplateDto documentTemplateDto,
      Function<DocumentTemplateSectionDto, DocumentTemplateSectionUrls> urlsFunction
  ) {
    var topLevelDocumentTemplateSectionDtos =
        documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto);

    return getSiblingDocumentTemplateSectionSummaryViews(
        null,
        topLevelDocumentTemplateSectionDtos,
        urlsFunction
    );
  }

  List<DocumentTemplateSectionSummaryView> getSiblingDocumentTemplateSectionSummaryViews(
      String parentSectionNumberString,
      List<DocumentTemplateSectionDto> siblingDocumentTemplateSectionDtos,
      Function<DocumentTemplateSectionDto, DocumentTemplateSectionUrls> urlsFunction
  ) {
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

      var children = getSiblingDocumentTemplateSectionSummaryViews(
          sectionNumberString,
          documentTemplateSectionDto.children(),
          urlsFunction
      );

      var documentTemplateSectionSummaryView = DocumentTemplateSectionSummaryView.from(
          sectionNumberString,
          conditionTitle,
          documentTemplateSectionDto,
          urlsFunction.apply(documentTemplateSectionDto),
          children
      );
      documentTemplateSectionSummaryViews.add(documentTemplateSectionSummaryView);
    }

    return documentTemplateSectionSummaryViews;
  }
}
