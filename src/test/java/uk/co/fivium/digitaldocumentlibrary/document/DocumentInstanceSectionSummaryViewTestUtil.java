package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import java.util.Map;
import java.util.UUID;

class DocumentInstanceSectionSummaryViewTestUtil {

  static Builder newBuilder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private String sectionNumber = "1";
    private String title = "Test title";
    private String content = "Test content";
    private boolean hasPageBreakBefore;
    private List<String> errorMessages = List.of();
    private Map<String, String> mailMergeResolvedValuesByMnemonic = Map.of();
    private DocumentInstanceSectionUrls documentInstanceSectionUrls = DocumentInstanceSectionUrlsTestUtil.newBuilder().build();
    private List<DocumentInstanceSectionSummaryView> children = List.of();

    private Builder() {
    }

    Builder withId(UUID id) {
      this.id = id;
      return this;
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

    Builder withDocumentInstanceSectionUrls(DocumentInstanceSectionUrls documentInstanceSectionUrls) {
      this.documentInstanceSectionUrls = documentInstanceSectionUrls;
      return this;
    }

    Builder withChildren(List<DocumentInstanceSectionSummaryView> children) {
      this.children = children;
      return this;
    }

    DocumentInstanceSectionSummaryView build() {
      return new DocumentInstanceSectionSummaryView(
          id,
          sectionNumber,
          title,
          content,
          hasPageBreakBefore,
          errorMessages,
          mailMergeResolvedValuesByMnemonic,
          documentInstanceSectionUrls,
          children
      );
    }
  }
}
