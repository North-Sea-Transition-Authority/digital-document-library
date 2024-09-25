package uk.co.fivium.digitaldocumentlibrary.document;

import io.micrometer.common.util.StringUtils;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@Component
public class DocumentInstanceSectionFormValidator {

  private final DocumentMailMergeFieldService documentMailMergeFieldService;
  static final Pattern MANUAL_FIELD_PATTERN = Pattern.compile("\\?{2}([^?]+)\\?{2}");
  static final String CONTENT_FIELD = "content";

  @Autowired
  DocumentInstanceSectionFormValidator(DocumentMailMergeFieldService documentMailMergeFieldService) {
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  public void validate(DocumentInstanceSectionForm form, DocumentInstanceDto documentInstanceDto, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "title", "title.required", "Enter a title");

    var content = form.content();

    if (StringUtils.isBlank(content) || StringUtils.isBlank(Jsoup.parse(content).text())) {
      errors.rejectValue(CONTENT_FIELD, "%s.required".formatted(CONTENT_FIELD), "Enter the section content");
    } else if (MANUAL_FIELD_PATTERN.matcher(content).results().findFirst().isPresent()) {
      errors.rejectValue(CONTENT_FIELD, "%s.invalid".formatted(CONTENT_FIELD), "Remove '??' from the clause text");
    } else {
      var documentMailMergeValidationResult = documentMailMergeFieldService.validateMailMergeFields(
          documentInstanceDto.documentTemplateDto(),
          content
      );

      if (!documentMailMergeValidationResult.isValid()) {
        errors.rejectValue(
            CONTENT_FIELD,
            "%s.invalid".formatted(CONTENT_FIELD),
            documentMailMergeValidationResult.errorMessage()
        );
      }
    }

    if (form.numbered() == null) {
      errors.rejectValue("numbered", "numbered.required", "Select if this section should be numbered");
    }

    if (form.hasPageBreakBefore() == null) {
      errors.rejectValue(
          "hasPageBreakBefore",
          "hasPageBreakBefore.required",
          "Select if this section should start on a new page"
      );
    }
  }
}
