package uk.co.fivium.digitaldocumentlibrary.document;

import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldUtil.getMnemonicFromMailMergeFieldText;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.util.StringUtil;

@Service
public class DocumentMailMergeFieldService {

  // This is the same regex as GOV.UK Notify uses:
  // https://github.com/alphagov/notifications-utils/blob/main/notifications_utils/field.py#L64
  // This must only match the innermost brackets, e.g. (((TEST))) should match ((TEST)).
  public static final Pattern MAIL_MERGE_FIELD_PATTERN = Pattern.compile("\\({2}([^()]+)\\){2}");
  public static final Pattern MANUAL_FIELD_PATTERN = Pattern.compile("\\?{2}([^?]+)\\?{2}");
  public static final Pattern FOOTNOTE_PATTERN = Pattern.compile("\\[{2}(.*?)\\]{2}");

  static final String SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE = "Mail merge field %s is not valid";
  static final String MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE = "Remove '??' from the clause text";
  static final String MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE = "Mail merge fields %s are not valid";

  private final List<DocumentMailMergeField> documentMailMergeFields;

  @Autowired
  DocumentMailMergeFieldService(List<DocumentMailMergeField> documentMailMergeFields) {
    this.documentMailMergeFields = documentMailMergeFields;
  }

  /**
   * Gets a list of document mail merge fields that are applicable to a given document template DTO.
   *
   * @param documentTemplateDto the document template DTO
   * @return the list of document mail merge fields
   */
  public List<DocumentMailMergeField> getApplicableDocumentMailMergeFields(DocumentTemplateDto documentTemplateDto) {
    return documentMailMergeFields.stream()
        .filter(documentMailMergeField -> documentMailMergeField.isApplicable(documentTemplateDto))
        .toList();
  }

  DocumentMailMergeValidationResult validateMailMergeFields(
      DocumentTemplateDto documentTemplateDto,
      String text,
      boolean includeManualMailMergeValidation
  ) {
    var textMailMergeFieldMnemonics = MAIL_MERGE_FIELD_PATTERN.matcher(text).results()
        .map(matchResult -> getMnemonicFromMailMergeFieldText(matchResult.group()))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    var invalidMnemonics = textMailMergeFieldMnemonics.stream()
        .filter(mnemonic -> getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic).isEmpty())
        .toList();

    var hasManualMailMergeFields = false;

    if (includeManualMailMergeValidation) {
      hasManualMailMergeFields = MANUAL_FIELD_PATTERN.matcher(text).find();
    }

    if (invalidMnemonics.isEmpty() && !hasManualMailMergeFields) {
      return DocumentMailMergeValidationResult.valid();
    }

    return DocumentMailMergeValidationResult.invalid(
        getErrorMessageForInvalidMailMergeFields(invalidMnemonics, hasManualMailMergeFields));
  }

  Optional<DocumentMailMergeField> getApplicableDocumentMailMergeField(
      DocumentTemplateDto documentTemplateDto,
      String mnemonic
  ) {
    return documentMailMergeFields.stream()
        .filter(documentMailMergeField -> documentMailMergeField.getMnemonic().equals(mnemonic))
        .filter(documentMailMergeField -> documentMailMergeField.isApplicable(documentTemplateDto))
        .findFirst();
  }

  private String getErrorMessageForInvalidMailMergeFields(List<String> invalidMnemonics, boolean hasManualMailMergeFields) {
    if (invalidMnemonics.isEmpty()) {
      return MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE;
    }

    String invalidMailMergeFieldError = invalidMnemonics.size() == 1
        ? SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE.formatted(invalidMnemonics.getFirst())
        : MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE.formatted(StringUtil.formatStringList(invalidMnemonics));

    if (!hasManualMailMergeFields) {
      return invalidMailMergeFieldError;
    }

    return "There are the following errors in this section: %s, %s"
        .formatted(invalidMailMergeFieldError, MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE);
  }
}
