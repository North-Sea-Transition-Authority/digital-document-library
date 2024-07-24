package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import java.util.Map;

class DocumentTemplateSectionSummaryViewTestUtil {

  static Builder newBuilder() {
    return new Builder();
  }

  static class Builder {

    private String sectionNumber = "1";
    private String title = "Test title";
    private String content = "Test content";
    private String conditionTitle;
    private boolean hasPageBreakBefore;
    private List<String> errorMessages = List.of();
    private Map<String, String> mailMergeResolvedValuesByMnemonic = Map.of();
    private DocumentTemplateSectionUrls documentTemplateSectionUrls = DocumentTemplateSectionUrlsTestUtil.newBuilder().build();
    private List<DocumentTemplateSectionSummaryView> children = List.of();

    private Builder() {
    }

    Builder withSectionNumber(String sectionNumber) {
      this.sectionNumber = sectionNumber;
      return this;
    }

    Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    Builder withContent(String content) {
      this.content = content;
      return this;
    }

    Builder withConditionTitle(String conditionTitle) {
      this.conditionTitle = conditionTitle;
      return this;
    }

    Builder withHasPageBreakBefore(boolean hasPageBreakBefore) {
      this.hasPageBreakBefore = hasPageBreakBefore;
      return this;
    }

    Builder withErrorMessages(List<String> errorMessages) {
      this.errorMessages = errorMessages;
      return this;
    }

    Builder withMailMergeResolvedValuesByMnemonic(Map<String, String> mailMergeResolvedValuesByMnemonic) {
      this.mailMergeResolvedValuesByMnemonic = mailMergeResolvedValuesByMnemonic;
      return this;
    }

    Builder withDocumentTemplateSectionUrls(DocumentTemplateSectionUrls documentTemplateSectionUrls) {
      this.documentTemplateSectionUrls = documentTemplateSectionUrls;
      return this;
    }

    Builder withChildren(List<DocumentTemplateSectionSummaryView> children) {
      this.children = children;
      return this;
    }

    DocumentTemplateSectionSummaryView build() {
      return new DocumentTemplateSectionSummaryView(
          sectionNumber,
          title,
          content,
          conditionTitle,
          hasPageBreakBefore,
          errorMessages,
          mailMergeResolvedValuesByMnemonic,
          documentTemplateSectionUrls,
          children
      );
    }
  }
}
