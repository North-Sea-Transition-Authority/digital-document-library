package uk.co.fivium.digitaldocumentlibrary.document;

import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldService.MAIL_MERGE_FIELD_PATTERN;
import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldUtil.getMnemonicFromMailMergeFieldText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class DocumentInstanceSectionMailMergeFieldResolver {

  private final DocumentMailMergeFieldService documentMailMergeFieldService;
  private final DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter;
  private final Map<String, DocumentMailMergeFieldResolveResult> resolveResultsByMnemonic = new HashMap<>();

  DocumentInstanceSectionMailMergeFieldResolver(
      DocumentMailMergeFieldService documentMailMergeFieldService,
      DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter
  ) {
    this.documentMailMergeFieldService = documentMailMergeFieldService;
    this.documentMailMergeFieldFormatter = documentMailMergeFieldFormatter;
  }

  ResolvedDocumentInstanceSection resolve(DocumentInstanceSectionDto documentInstanceSectionDto) {
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    var resolvedDocumentMailMergeFields = new ArrayList<ResolvedDocumentMailMergeField>();

    var resolvedContent = MAIL_MERGE_FIELD_PATTERN.matcher(documentInstanceSectionDto.content()).replaceAll(matcher -> {
      var matchText = matcher.group();
      var mnemonic = getMnemonicFromMailMergeFieldText(matchText);

      var mailMergeFieldOptional =
          documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic);
      if (mailMergeFieldOptional.isEmpty()) {
        return documentMailMergeFieldFormatter.formatError(matchText);
      }

      var mailMergeField = mailMergeFieldOptional.get();
      var mailMergeResolveResult = resolveResultsByMnemonic
          .computeIfAbsent(mnemonic, s -> mailMergeField.resolve(documentInstanceDto));

      resolvedDocumentMailMergeFields.add(new ResolvedDocumentMailMergeField(mailMergeField, mailMergeResolveResult));

      return mailMergeResolveResult.hasError()
          ? documentMailMergeFieldFormatter.formatError(matchText)
          : documentMailMergeFieldFormatter.formatSuccess(mailMergeResolveResult.resolvedValue());
    });

    return new ResolvedDocumentInstanceSection(resolvedContent, resolvedDocumentMailMergeFields);
  }

}
