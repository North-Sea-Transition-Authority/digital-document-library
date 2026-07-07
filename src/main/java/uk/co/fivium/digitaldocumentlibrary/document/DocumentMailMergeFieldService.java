package uk.co.fivium.digitaldocumentlibrary.document;

import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldUtil.getMnemonicFromMailMergeFieldText;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
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

  private static final Set<String> INLINE_ELEMENTS = Set.of(
      "a", "abbr", "b", "bdi", "bdo", "br", "cite", "code", "data", "dfn", "em", "i", "kbd", "mark", "q", "rp", "rt",
      "ruby", "s", "samp", "small", "span", "strike", "strong", "sub", "sup", "time", "u", "var", "wbr"
  );

  static final String SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE = "Mail merge field %s is not valid";
  static final String MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE = "Remove '??' from the clause text";
  static final String MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE = "Mail merge fields %s are not valid";
  static final String MALFORMED_MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE =
      "Optional text surrounded by '??' must not be split over multiple paragraphs or bullet points, and " +
          "formatting such as bold or italics must not cross the start or end of the optional text";

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

  /**
   * Validates the mail merge fields within the given text against the mail merge fields applicable to the document
   * template.
   *
   * @param documentTemplateDto the document template the text belongs to
   * @param text the text to validate
   * @param includeManualMailMergeValidation when true, any manual mail merge field ({@code ??optional text??}) is
   *                                         rejected, as manual fields must be resolved before a document is
   *                                         submitted. When false, manual fields are allowed but must contain
   *                                         balanced HTML tags so that they can be highlighted when the document
   *                                         is rendered.
   * @return the validation result
   */
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
    var hasMalformedManualMailMergeFields = false;

    if (includeManualMailMergeValidation) {
      hasManualMailMergeFields = MANUAL_FIELD_PATTERN.matcher(text).find();
    } else {
      hasMalformedManualMailMergeFields = hasMalformedManualMailMergeFields(text);
    }

    if (invalidMnemonics.isEmpty() && !hasManualMailMergeFields && !hasMalformedManualMailMergeFields) {
      return DocumentMailMergeValidationResult.valid();
    }

    return DocumentMailMergeValidationResult.invalid(getErrorMessageForInvalidMailMergeFields(
        invalidMnemonics,
        hasManualMailMergeFields,
        hasMalformedManualMailMergeFields
    ));
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

  private String getErrorMessageForInvalidMailMergeFields(
      List<String> invalidMnemonics,
      boolean hasManualMailMergeFields,
      boolean hasMalformedManualMailMergeFields
  ) {
    var errorMessages = new ArrayList<String>();

    if (!invalidMnemonics.isEmpty()) {
      errorMessages.add(invalidMnemonics.size() == 1
          ? SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE.formatted(invalidMnemonics.getFirst())
          : MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE.formatted(StringUtil.formatStringList(invalidMnemonics)));
    }

    if (hasManualMailMergeFields) {
      errorMessages.add(MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE);
    }

    if (hasMalformedManualMailMergeFields) {
      errorMessages.add(MALFORMED_MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE);
    }

    if (errorMessages.size() == 1) {
      return errorMessages.getFirst();
    }

    return "There are the following errors in this section: %s".formatted(String.join(", ", errorMessages));
  }

  // When a document is rendered, each manual mail merge field matched by MANUAL_FIELD_PATTERN is wrapped in a
  // highlighting span. If a field spans two block elements, or formatting starts outside the field and ends inside it
  // (or vice versa), the wrapping span interleaves with those tags, producing malformed XHTML which cannot be
  // rendered to PDF.
  private static boolean hasMalformedManualMailMergeFields(String text) {
    return MANUAL_FIELD_PATTERN.matcher(text).results()
        .anyMatch(matchResult -> !canBeWrappedInHighlightingSpan(matchResult.group(1)));
  }

  private static boolean canBeWrappedInHighlightingSpan(String manualFieldContent) {
    var parser = Parser.htmlParser().setTrackErrors(1);
    var document = Jsoup.parse(manualFieldContent, "", parser);

    // Orphaned closing tags and unclosed opening tags are reported as parse errors
    if (!parser.getErrors().isEmpty()) {
      return false;
    }

    // Block elements within the field mean it spans more than one paragraph, list item etc.
    return document.body().getAllElements().stream()
        // getAllElements() includes the body element itself
        .filter(element -> !"body".equals(element.normalName()))
        .allMatch(element -> INLINE_ELEMENTS.contains(element.normalName()));
  }
}
