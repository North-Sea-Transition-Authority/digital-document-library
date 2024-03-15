package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import java.util.List;

public record DocumentInstanceSectionSummaryView(
    int nestingLevel,
    @Nullable String sectionNumber,
    String title,
    String content,
    boolean hasPageBreakBefore,
    List<String> errorMessages,
    DocumentInstanceSectionUrls documentInstanceSectionUrls
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
      DocumentInstanceSectionUrls documentInstanceSectionUrls
  ) {
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
        documentInstanceSectionUrls
    );
  }
}
