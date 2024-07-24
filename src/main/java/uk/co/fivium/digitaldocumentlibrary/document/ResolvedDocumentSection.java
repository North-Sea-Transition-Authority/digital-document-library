package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;

record ResolvedDocumentSection(
    String resolvedContent,
    List<ResolvedDocumentMailMergeField> resolvedDocumentMailMergeFields
) {
}
