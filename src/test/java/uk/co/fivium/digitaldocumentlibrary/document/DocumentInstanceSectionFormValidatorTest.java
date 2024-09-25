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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceSectionFormValidatorTest {

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  private DocumentInstanceSectionFormValidator documentInstanceSectionFormValidator;

  @ParameterizedTest
  @NullAndEmptySource
  void validate_nullOrEmptyTitle(String title) {
    var form = DocumentInstanceSectionFormTestUtil.builder()
        .withTitle(title)
        .build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(
        documentMailMergeFieldService.validateMailMergeFields(
            documentInstanceDto.documentTemplateDto(),
            form.content()
        )
    ).thenReturn(DocumentMailMergeValidationResult.valid());

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, errors);

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
  void validate_contentHasMailMergeValidationError() {
    var form = DocumentInstanceSectionFormTestUtil.builder().build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    var mailMergeErrorMessage = "Test error message";

    when(
        documentMailMergeFieldService.validateMailMergeFields(
            documentInstanceDto.documentTemplateDto(),
            form.content()
        )
    ).thenReturn(DocumentMailMergeValidationResult.invalid(mailMergeErrorMessage));

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, errors);

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

  @Test
  void validate_numberedNull() {
    var form = DocumentInstanceSectionFormTestUtil.builder()
        .withNumbered(null)
        .build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(
        documentMailMergeFieldService.validateMailMergeFields(
            documentInstanceDto.documentTemplateDto(),
            form.content()
        )
    ).thenReturn(DocumentMailMergeValidationResult.valid());

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, errors);

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
    var form = DocumentInstanceSectionFormTestUtil.builder()
        .withPageBreakBefore(null)
        .build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(
        documentMailMergeFieldService.validateMailMergeFields(
            documentInstanceDto.documentTemplateDto(),
            form.content()
        )
    ).thenReturn(DocumentMailMergeValidationResult.valid());

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, errors);

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

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "<p></p>")
  void validate_contentIsEmpty(String emptyContent) {
    var form = DocumentInstanceSectionFormTestUtil.builder()
        .withContent(emptyContent)
        .build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, errors);

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
  void validate_contentHasManualMailMergeValue() {
    var form = DocumentInstanceSectionFormTestUtil.builder()
        .withContent("??MANUAL MAIL MERGE??")
        .build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("content", "content.invalid", "Remove '??' from the clause text")
        );

    verify(documentMailMergeFieldService, never()).validateMailMergeFields(any(), any());
  }
}
