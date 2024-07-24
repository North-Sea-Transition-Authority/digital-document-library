package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record DocumentTemplateSectionSummaryView(
    @Nullable String sectionNumber,
    String title,
    String content,
    @Nullable String conditionTitle,
    boolean hasPageBreakBefore,
    List<String> errorMessages,
    Map<String, String> mailMergeResolvedValuesByMnemonic,
    DocumentTemplateSectionUrls documentTemplateSectionUrls,
    List<DocumentTemplateSectionSummaryView> children
) {

  public String titleWithSectionNumber() {
    if (sectionNumber == null) {
      return title;
    }

    return "%s %s".formatted(sectionNumber, title);
  }

  public List<DocumentTemplateSectionSummaryView> descendants() {
    return children()
        .stream()
        .flatMap(child -> Stream.concat(Stream.of(child), child.descendants().stream()))
        .toList();
  }

  static DocumentTemplateSectionSummaryView from(
      String sectionNumberString,
      String conditionTitle,
      DocumentTemplateSectionDto documentTemplateSectionDto,
      ResolvedDocumentSection resolvedDocumentTemplateSection,
      DocumentTemplateSectionUrls documentTemplateSectionUrls,
      List<DocumentTemplateSectionSummaryView> children) {

    var errorMessages = resolvedDocumentTemplateSection.resolvedDocumentMailMergeFields()
        .stream()
        .map(ResolvedDocumentMailMergeField::documentMailMergeFieldResolveResult)
        .filter(DocumentMailMergeFieldResolveResult::hasError)
        .map(DocumentMailMergeFieldResolveResult::errorMessage)
        .toList();

    var mailMergeResolvedValuesByMnemonic = new HashMap<String, String>();
    for (var resolvedField : resolvedDocumentTemplateSection.resolvedDocumentMailMergeFields()) {
      var mnemonic = resolvedField.documentMailMergeField().getMnemonic();
      var resolvedValue = resolvedField.documentMailMergeFieldResolveResult().resolvedValue();

      mailMergeResolvedValuesByMnemonic.put(mnemonic, resolvedValue);
    }

    return new DocumentTemplateSectionSummaryView(
        sectionNumberString,
        documentTemplateSectionDto.title(),
        resolvedDocumentTemplateSection.resolvedContent(),
        conditionTitle,
        documentTemplateSectionDto.hasPageBreakBefore(),
        errorMessages,
        mailMergeResolvedValuesByMnemonic,
        documentTemplateSectionUrls,
        children
    );
  }
}
