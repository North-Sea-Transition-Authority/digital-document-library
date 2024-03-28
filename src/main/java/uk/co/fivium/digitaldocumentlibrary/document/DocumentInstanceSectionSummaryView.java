package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public record DocumentInstanceSectionSummaryView(
    @Nullable String sectionNumber,
    String title,
    String content,
    boolean hasPageBreakBefore,
    List<String> errorMessages,
    DocumentInstanceSectionUrls documentInstanceSectionUrls,
    List<DocumentInstanceSectionSummaryView> children
) {

  public String titleWithSectionNumber() {
    if (sectionNumber == null) {
      return title;
    }

    return "%s %s".formatted(sectionNumber, title);
  }

  public List<DocumentInstanceSectionSummaryView> descendants() {
    return children()
        .stream()
        .flatMap(child -> Stream.concat(Stream.of(child), child.descendants().stream()))
        .toList();
  }

  static DocumentInstanceSectionSummaryView from(
      String sectionNumberString,
      DocumentInstanceSectionDto documentInstanceSectionDto,
      ResolvedDocumentInstanceSection resolvedDocumentInstanceSection,
      DocumentInstanceSectionUrls documentInstanceSectionUrls,
      List<DocumentInstanceSectionSummaryView> children
  ) {
    var errorMessages = resolvedDocumentInstanceSection.fieldResolveResults()
        .stream()
        .filter(DocumentMailMergeFieldResolveResult::hasError)
        .map(DocumentMailMergeFieldResolveResult::errorMessage)
        .toList();

    return new DocumentInstanceSectionSummaryView(
        sectionNumberString,
        documentInstanceSectionDto.title(),
        resolvedDocumentInstanceSection.resolvedContent(),
        documentInstanceSectionDto.hasPageBreakBefore(),
        errorMessages,
        documentInstanceSectionUrls,
        children
    );
  }
}
