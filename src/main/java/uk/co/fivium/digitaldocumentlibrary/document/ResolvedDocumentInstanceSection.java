package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;

record ResolvedDocumentInstanceSection(
    String resolvedContent,
    List<ResolvedDocumentMailMergeField> resolvedDocumentMailMergeFields
) {
}
