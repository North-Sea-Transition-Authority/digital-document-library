package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record DocumentTemplateSectionsSummaryView(
    List<DocumentTemplateSectionSummaryView> topLevelDocumentTemplateSectionSummaryViews,
    List<String> allErrorMessages,
    Map<String, String> allMailMergeResolvedValuesByMnemonic
) {

  public static DocumentTemplateSectionsSummaryView from(
      List<DocumentTemplateSectionSummaryView> topLevelDocumentTemplateSectionSummaryViews
  ) {
    var allErrorMessages = topLevelDocumentTemplateSectionSummaryViews
        .stream()
        .flatMap(view ->
            Stream.concat(
                view.errorMessages().stream(),
                view.descendants().stream().flatMap(descendant -> descendant.errorMessages().stream())
            )
        )
        .distinct()
        .toList();

    var allMailMergeResolvedValuesByMnemonic = new HashMap<String, String>();
    for (var view : topLevelDocumentTemplateSectionSummaryViews) {
      allMailMergeResolvedValuesByMnemonic.putAll(view.mailMergeResolvedValuesByMnemonic());

      for (var descendant : view.descendants()) {
        allMailMergeResolvedValuesByMnemonic.putAll(descendant.mailMergeResolvedValuesByMnemonic());
      }
    }

    return new DocumentTemplateSectionsSummaryView(
        topLevelDocumentTemplateSectionSummaryViews,
        allErrorMessages,
        allMailMergeResolvedValuesByMnemonic
    );
  }

}
