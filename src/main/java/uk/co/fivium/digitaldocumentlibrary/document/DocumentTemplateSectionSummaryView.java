package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import java.util.List;

public record DocumentTemplateSectionSummaryView(
    @Nullable String sectionNumber,
    String title,
    String content,
    @Nullable String conditionTitle,
    boolean hasPageBreakBefore,
    DocumentTemplateSectionUrls documentTemplateSectionUrls,
    List<DocumentTemplateSectionSummaryView> children
) {

  public String titleWithSectionNumber() {
    if (sectionNumber == null) {
      return title;
    }

    return "%s %s".formatted(sectionNumber, title);
  }

  static DocumentTemplateSectionSummaryView from(
      String sectionNumberString,
      String conditionTitle,
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentTemplateSectionUrls documentTemplateSectionUrls,
      List<DocumentTemplateSectionSummaryView> children
  ) {
    return new DocumentTemplateSectionSummaryView(
        sectionNumberString,
        documentTemplateSectionDto.title(),
        documentTemplateSectionDto.content(),
        conditionTitle,
        documentTemplateSectionDto.hasPageBreakBefore(),
        documentTemplateSectionUrls,
        children
    );
  }
}
