package uk.co.fivium.digitaldocumentlibrary.document;

import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldService.FOOTNOTE_PATTERN;
import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldService.MAIL_MERGE_FIELD_PATTERN;
import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldService.MANUAL_FIELD_PATTERN;
import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldUtil.getMnemonicFromMailMergeFieldText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class DocumentSectionMailMergeFieldResolver {

  private final DocumentMailMergeFieldService documentMailMergeFieldService;
  private final DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter;
  private final Map<String, DocumentMailMergeFieldResolveResult> resolveResultsByMnemonic = new HashMap<>();

  DocumentSectionMailMergeFieldResolver(DocumentMailMergeFieldService documentMailMergeFieldService,
                                        DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter) {
    this.documentMailMergeFieldService = documentMailMergeFieldService;
    this.documentMailMergeFieldFormatter = documentMailMergeFieldFormatter;
  }


  ResolvedDocumentSection resolve(DocumentInstanceSectionDto documentInstanceSectionDto) {
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    return resolve(
        documentInstanceDto.documentTemplateDto(),
        documentInstanceSectionDto.content(),
        documentMailMergeField -> documentMailMergeField.resolve(documentInstanceDto)
    );
  }

  ResolvedDocumentSection resolve(DocumentTemplateSectionDto documentTemplateSectionDto) {
    return resolve(
        documentTemplateSectionDto.documentTemplateDto(),
        documentTemplateSectionDto.content(),
        documentMailMergeField -> documentMailMergeField.resolve(documentTemplateSectionDto.documentTemplateDto())
    );
  }

  private ResolvedDocumentSection resolve(
      DocumentTemplateDto documentTemplateDto,
      String content,
      Function<DocumentMailMergeField, DocumentMailMergeFieldResolveResult> resolveFunction
  ) {
    var resolvedDocumentMailMergeFields = new ArrayList<ResolvedDocumentMailMergeField>();

    var resolvedContent = MAIL_MERGE_FIELD_PATTERN.matcher(content).replaceAll(matcher -> {
      var matchText = matcher.group();
      var mnemonic = getMnemonicFromMailMergeFieldText(matchText);

      var mailMergeFieldOptional = documentMailMergeFieldService
          .getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic);
      if (mailMergeFieldOptional.isEmpty()) {
        return documentMailMergeFieldFormatter.formatError(matchText);
      }

      var mailMergeField = mailMergeFieldOptional.get();
      var mailMergeResolveResult =
          resolveResultsByMnemonic.computeIfAbsent(mnemonic, s -> resolveFunction.apply(mailMergeField));

      resolvedDocumentMailMergeFields.add(new ResolvedDocumentMailMergeField(mailMergeField, mailMergeResolveResult));

      return mailMergeResolveResult.hasError()
          ? documentMailMergeFieldFormatter.formatError(matchText)
          : documentMailMergeFieldFormatter.formatSuccess(mailMergeResolveResult.resolvedValue());
    });

    resolvedContent = MANUAL_FIELD_PATTERN.matcher(resolvedContent)
        .replaceAll(matcher -> documentMailMergeFieldFormatter.formatError(matcher.group()));

    resolvedContent = FOOTNOTE_PATTERN.matcher(resolvedContent)
        .replaceAll(matchResult -> documentMailMergeFieldFormatter.formatFootnotes("<span class=\"footnote\">%s</span>".formatted(
            matchResult.group(1))));

    return new ResolvedDocumentSection(resolvedContent, resolvedDocumentMailMergeFields);
  }
}
