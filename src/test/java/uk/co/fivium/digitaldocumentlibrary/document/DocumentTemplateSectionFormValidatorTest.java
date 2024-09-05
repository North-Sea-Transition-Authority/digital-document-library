package uk.co.fivium.digitaldocumentlibrary.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateSectionFormValidatorTest {

  @Mock
  private DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  private DocumentTemplateSectionFormValidator documentTemplateSectionFormValidator;

  @ParameterizedTest
  @NullAndEmptySource
  void validate_nullOrEmptyTitle(String title) {
    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withTitle(title)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.getContent()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("title", "title.required", "Enter a title")
        );
  }

  @Test
  void validate_nullConditionMnemonic() {
    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withConditionMnemonic(null)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.getContent()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors()).isEmpty();
  }

  @Test
  void validate_nonNullConditionMnemonicAndConditionDoesNotExist() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withConditionMnemonic(conditionMnemonic)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionCondition(
            documentTemplateDto,
            conditionMnemonic
        )
    ).thenReturn(Optional.empty());
    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.getContent()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("conditionMnemonic", "conditionMnemonic.invalid", "Select a valid condition")
        );
  }

  @Test
  void validate_nonNullConditionMnemonicAndConditionExists() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withConditionMnemonic(conditionMnemonic)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    var documentTemplateSectionCondition = DocumentTemplateSectionConditionTestUtil.builder().build();

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionCondition(
            documentTemplateDto,
            conditionMnemonic
        )
    ).thenReturn(Optional.of(documentTemplateSectionCondition));
    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.getContent()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors()).isEmpty();
  }

  @Test
  void validate_contentHasMailMergeValidationErrors() {
    var form = DocumentTemplateSectionFormTestUtil.builder().build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    var mailMergeErrorMessage = "Test error message";

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.getContent()))
        .thenReturn(DocumentMailMergeValidationResult.invalid(mailMergeErrorMessage));

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("content", "content.invalid", mailMergeErrorMessage)
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "<p></p>")
  void validate_contentIsEmpty(String emptyContent) {
    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withContent(emptyContent)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("content", "content.required", "Enter the section content")
        );

    verify(documentMailMergeFieldService, never()).validateMailMergeFields(any(), any());
  }

  @Test
  void validate_numberedNull() {
    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withNumbered(null)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.getContent()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("numbered", "numbered.required", "Select if this section should be numbered")
        );
  }

  @Test
  void validate_hasPageBreakBeforeNull() {
    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withPageBreakBefore(null)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.getContent()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("hasPageBreakBefore", "hasPageBreakBefore.required", "Select if this section should start on a new page")
        );
  }
}
