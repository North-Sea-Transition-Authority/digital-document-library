package uk.co.fivium.digitaldocumentlibrary.document;

public class ResolvedDocumentMailMergeFieldTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private DocumentMailMergeField documentMailMergeField = DocumentMailMergeFieldTestUtil.builder().build();
    private DocumentMailMergeFieldResolveResult documentMailMergeFieldResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder().build();

    public Builder withDocumentMailMergeField(DocumentMailMergeField documentMailMergeField) {
      this.documentMailMergeField = documentMailMergeField;
      return this;
    }

    public Builder withDocumentMailMergeFieldResolveResult(DocumentMailMergeFieldResolveResult documentMailMergeFieldResolveResult) {
      this.documentMailMergeFieldResolveResult = documentMailMergeFieldResolveResult;
      return this;
    }

    public ResolvedDocumentMailMergeField build() {
      return new ResolvedDocumentMailMergeField(documentMailMergeField, documentMailMergeFieldResolveResult);
    }

  }

}
