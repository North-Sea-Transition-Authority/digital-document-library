package uk.co.fivium.digitaldocumentlibrary.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.annotation.Nullable;
import java.util.List;
import uk.co.fivium.digitaldocumentlibrary.mvc.ReverseRouter;

public record DocumentInstanceSectionSummaryView(
    int nestingLevel,
    @Nullable String sectionNumber,
    String title,
    String content,
    boolean hasPageBreakBefore,
    List<String> errorMessages,
    String addSectionBeforeUrl,
    String addSectionAfterUrl,
    String addSubsectionUrl,
    String editUrl,
    String removeUrl
) {

  public String titleWithSectionNumber() {
    if (sectionNumber == null) {
      return title;
    }

    return "%s %s".formatted(sectionNumber, title);
  }

  static DocumentInstanceSectionSummaryView from(
      String sectionNumberString,
      DocumentInstanceSectionDto documentInstanceSectionDto,
      ResolvedDocumentInstanceSection resolvedDocumentInstanceSection,
      Class<? extends DocumentInstanceSectionController> documentInstanceSectionControllerClass
  ) {
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    var errorMessages = resolvedDocumentInstanceSection.fieldResolveResults()
        .stream()
        .filter(DocumentMailMergeFieldResolveResult::hasError)
        .map(DocumentMailMergeFieldResolveResult::errorMessage)
        .toList();

    return new DocumentInstanceSectionSummaryView(
        documentInstanceSectionDto.nestingLevel(),
        sectionNumberString,
        documentInstanceSectionDto.title(),
        resolvedDocumentInstanceSection.resolvedContent(),
        documentInstanceSectionDto.hasPageBreakBefore(),
        errorMessages,
        ReverseRouter.route(on(documentInstanceSectionControllerClass)
            .getAddDocumentInstanceSectionBefore(documentInstanceSectionId)),
        ReverseRouter.route(on(documentInstanceSectionControllerClass)
            .getAddDocumentInstanceSectionAfter(documentInstanceSectionId)),
        ReverseRouter.route(on(documentInstanceSectionControllerClass)
            .getAddDocumentInstanceSubsection(documentInstanceSectionId)),
        ReverseRouter.route(on(documentInstanceSectionControllerClass)
            .getEditDocumentInstanceSection(documentInstanceSectionId)),
        ReverseRouter.route(on(documentInstanceSectionControllerClass)
            .getRemoveDocumentInstanceSection(documentInstanceSectionId))
    );
  }
}
