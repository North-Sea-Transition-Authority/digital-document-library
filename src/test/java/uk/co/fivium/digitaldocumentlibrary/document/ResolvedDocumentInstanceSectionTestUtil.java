package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;

public class ResolvedDocumentInstanceSectionTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String resolvedContent = "here are some values: value 1, value 2";
    private List<DocumentMailMergeFieldResolveResult> fieldResolveResults = List.of(
        DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
            .withResolvedValue("value 1")
            .build(),
        DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
            .withResolvedValue("value 2")
            .build()
    );

    public Builder withResolvedContent(String resolvedContent) {
      this.resolvedContent = resolvedContent;
      return this;
    }

    public Builder withFieldResolveResults(List<DocumentMailMergeFieldResolveResult> fieldResolveResults) {
      this.fieldResolveResults = fieldResolveResults;
      return this;
    }

    public ResolvedDocumentInstanceSection build() {
      return new ResolvedDocumentInstanceSection(resolvedContent, fieldResolveResults);
    }

  }

}
