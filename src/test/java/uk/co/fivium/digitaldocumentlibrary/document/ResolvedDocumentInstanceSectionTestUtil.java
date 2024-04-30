package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;

class ResolvedDocumentInstanceSectionTestUtil {

  static Builder newBuilder() {
    return new Builder();
  }

  static class Builder {

    private String resolvedContent = "here are some values: value 1, value 2";
    private List<ResolvedDocumentMailMergeField> resolvedDocumentMailMergeFields = List.of(
        ResolvedDocumentMailMergeFieldTestUtil.newBuilder().build(),
        ResolvedDocumentMailMergeFieldTestUtil.newBuilder().build()
    );

    Builder withResolvedContent(String resolvedContent) {
      this.resolvedContent = resolvedContent;
      return this;
    }

    Builder withResolvedDocumentMailMergeField(List<ResolvedDocumentMailMergeField> resolvedDocumentMailMergeFields) {
      this.resolvedDocumentMailMergeFields = resolvedDocumentMailMergeFields;
      return this;
    }

    ResolvedDocumentInstanceSection build() {
      return new ResolvedDocumentInstanceSection(resolvedContent, resolvedDocumentMailMergeFields);
    }

  }

}
