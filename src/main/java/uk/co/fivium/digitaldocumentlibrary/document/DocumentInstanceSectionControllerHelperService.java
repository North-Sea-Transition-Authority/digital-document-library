package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class DocumentInstanceSectionControllerHelperService {

  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  DocumentInstanceSectionControllerHelperService(
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

    var sectionSummaryViews = getDocumentInstanceSectionSummaryViewsForSectionSiblings(
        null,
        topLevelDocumentInstanceSectionDtos,
        urlsFunction,
        documentMailMergeFieldFormatter
    );

    var errorMessages = sectionSummaryViews.stream().flatMap(view -> view.errorMessages().stream()).distinct().toList();

    return new DocumentInstanceSectionsSummaryView(sectionSummaryViews, errorMessages);
  }

  List<DocumentInstanceSectionSummaryView> getDocumentInstanceSectionSummaryViewsForSectionSiblings(
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

      var documentInstanceSectionSummaryView = DocumentInstanceSectionSummaryView.from(
          sectionNumberString,
          documentInstanceSectionDto,
          resolvedDocumentInstanceSection,
          urlsFunction.apply(documentInstanceSectionDto)
      );
      documentInstanceSectionSummaryViews.add(documentInstanceSectionSummaryView);

      var childrenDocumentInstanceSectionSummaryViews = getDocumentInstanceSectionSummaryViewsForSectionSiblings(
          sectionNumberString,
          documentInstanceSectionDto.children(),
          urlsFunction,
          documentMailMergeFieldFormatter
      );
      documentInstanceSectionSummaryViews.addAll(childrenDocumentInstanceSectionSummaryViews);
    }

    return documentInstanceSectionSummaryViews;
  }

  public void createDocumentInstanceSection(
      DocumentInstanceDto documentInstanceDto,
      @Nullable DocumentInstanceSectionDto parentDto,
      DocumentInstanceSectionForm form,
      int displayOrder
  ) {
    documentInstanceSectionService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore(),
        displayOrder
    );
  }

  public void editDocumentInstanceSection(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    documentInstanceSectionService.editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore()
    );
  }
}
