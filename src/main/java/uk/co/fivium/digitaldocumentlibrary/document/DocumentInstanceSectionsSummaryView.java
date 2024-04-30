package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record DocumentInstanceSectionsSummaryView(
    List<DocumentInstanceSectionSummaryView> topLevelDocumentInstanceSectionSummaryViews,
    List<String> allErrorMessages,
    Map<String, String> allMailMergeResolvedValuesByMnemonic
) {

  public static DocumentInstanceSectionsSummaryView from(
      List<DocumentInstanceSectionSummaryView> topLevelDocumentInstanceSectionSummaryViews
  ) {
    var allErrorMessages = topLevelDocumentInstanceSectionSummaryViews
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
    for (var view : topLevelDocumentInstanceSectionSummaryViews) {
      allMailMergeResolvedValuesByMnemonic.putAll(view.mailMergeResolvedValuesByMnemonic());

      for (var descendant : view.descendants()) {
        allMailMergeResolvedValuesByMnemonic.putAll(descendant.mailMergeResolvedValuesByMnemonic());
      }
    }

    return new DocumentInstanceSectionsSummaryView(
        topLevelDocumentInstanceSectionSummaryViews,
        allErrorMessages,
        allMailMergeResolvedValuesByMnemonic
    );
  }

}
