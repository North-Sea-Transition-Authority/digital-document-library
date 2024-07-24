package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record DocumentInstanceSectionSummaryView(
    @Nullable String sectionNumber,
    String title,
    String content,
    boolean hasPageBreakBefore,
    List<String> errorMessages,
    Map<String, String> mailMergeResolvedValuesByMnemonic,
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
      ResolvedDocumentSection resolvedDocumentSection,
      DocumentInstanceSectionUrls documentInstanceSectionUrls,
      List<DocumentInstanceSectionSummaryView> children
  ) {
    var errorMessages = resolvedDocumentSection.resolvedDocumentMailMergeFields()
        .stream()
        .map(ResolvedDocumentMailMergeField::documentMailMergeFieldResolveResult)
        .filter(DocumentMailMergeFieldResolveResult::hasError)
        .map(DocumentMailMergeFieldResolveResult::errorMessage)
        .toList();

    var mailMergeResolvedValuesByMnemonic = new HashMap<String, String>();
    for (var resolvedField : resolvedDocumentSection.resolvedDocumentMailMergeFields()) {
      var mnemonic = resolvedField.documentMailMergeField().getMnemonic();
      var resolvedValue = resolvedField.documentMailMergeFieldResolveResult().resolvedValue();

      mailMergeResolvedValuesByMnemonic.put(mnemonic, resolvedValue);
    }

    return new DocumentInstanceSectionSummaryView(
        sectionNumberString,
        documentInstanceSectionDto.title(),
        resolvedDocumentSection.resolvedContent(),
        documentInstanceSectionDto.hasPageBreakBefore(),
        errorMessages,
        mailMergeResolvedValuesByMnemonic,
        documentInstanceSectionUrls,
        children
    );
  }

}
