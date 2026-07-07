package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldService.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.util.StringUtil;

@ExtendWith(MockitoExtension.class)
class DocumentMailMergeFieldServiceTest {

  @Mock
  private List<DocumentMailMergeField> documentMailMergeFields;

  @InjectMocks
  @Spy
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @Test
  void getApplicableDocumentMailMergeFields() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField2 = mock(DocumentMailMergeField.class);
    var documentMailMergeField3 = mock(DocumentMailMergeField.class);

    when(documentMailMergeField1.isApplicable(documentTemplateDto)).thenReturn(true);
    when(documentMailMergeField2.isApplicable(documentTemplateDto)).thenReturn(true);
    when(documentMailMergeField3.isApplicable(documentTemplateDto)).thenReturn(false);

    when(documentMailMergeFields.stream())
        .thenReturn(Stream.of(documentMailMergeField1, documentMailMergeField2, documentMailMergeField3));

    assertThat(documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto)).containsExactly(
        documentMailMergeField1,
        documentMailMergeField2
    );
  }

  @Test
  void validateMailMergeFields_allMailMergeFieldsValid() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        (((MAIL_MERGE_FIELD_2)))
        ((((MAIL_MERGE_FIELD_2))))
        (((((MAIL_MERGE_FIELD_2)))))
        (Example text in brackets)
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.valid());
  }

  @Test
  void validateMailMergeFields_singleMailMergeFieldInvalid() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");

    var expectedErrorMessage =
        DocumentMailMergeFieldService.SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE.formatted("MAIL_MERGE_FIELD_2");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(expectedErrorMessage));
  }

  @Test
  void validateMailMergeFields_singleMailMergeFieldInvalidAndUsedMultipleTimes() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        ((MAIL_MERGE_FIELD_2))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");

    var expectedErrorMessage =
        DocumentMailMergeFieldService.SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE.formatted("MAIL_MERGE_FIELD_2");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(expectedErrorMessage));
  }

  @Test
  void validateMailMergeFields_twoMailMergeFieldsInvalid() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        ((MAIL_MERGE_FIELD_3))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_3");

    var expectedErrorMessage = DocumentMailMergeFieldService.MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE.formatted(
        StringUtil.formatStringList(List.of("MAIL_MERGE_FIELD_2", "MAIL_MERGE_FIELD_3"))
    );

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(expectedErrorMessage));
  }

  @Test
  void validateMailMergeFields_threeMailMergeFieldsInvalid() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        ((MAIL_MERGE_FIELD_3))
        ((MAIL_MERGE_FIELD_4))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_3");

    var expectedErrorMessage = DocumentMailMergeFieldService.MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE.formatted(
        StringUtil.formatStringList(List.of("MAIL_MERGE_FIELD_2", "MAIL_MERGE_FIELD_3", "MAIL_MERGE_FIELD_4"))
    );

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(expectedErrorMessage));
  }

  @Test
  void validateMailMergeFields_whenHasManualMailMergeFieldsAndOneInvalidMailMergeField() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ??MAIL_MERGE_FIELD_2??
        ((MAIL_MERGE_FIELD_3))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_3");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, true))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(
            "There are the following errors in this section: Mail merge field %s is not valid, Remove '??' from the clause text"
                .formatted("MAIL_MERGE_FIELD_3")));
  }

  @Test
  void validateMailMergeFields_whenHasManualMailMergeFieldsAndMultipleInvalidMailMergeField() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ??MAIL_MERGE_FIELD_2??
        ((MAIL_MERGE_FIELD_3))
        ((MAIL_MERGE_FIELD_4))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_3");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_4");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, true))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(
            "There are the following errors in this section: %s, %s"
                .formatted(
                    MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE
                        .formatted(StringUtil.formatStringList(List.of("MAIL_MERGE_FIELD_3", "MAIL_MERGE_FIELD_4"))),
                    MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE)));
  }

  @Test
  void validateMailMergeFields_whenHasManualMailMergeFields() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ??MAIL_MERGE_FIELD_2??
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, true))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "<p>??Optional text on a single line??</p>",
      "<p>??Optional text with <b>fully covered formatting</b> inside it??</p>",
      "<p><b>??Optional text entirely within formatting??</b></p>",
      "<p>??Optional text either side of a <br/> line break??</p>",
      "<p>??Optional text with an unpaired marker</p>",
      "<p>??Optional text?? followed by ??more optional text??</p>"
  })
  void validateMailMergeFields_whenNoMalformedManualMailMergeFields(String text) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.valid());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      // optional text split over two paragraphs
      "<p>??Optional text starting on one line</p><p>and ending on another??</p>",
      // formatting starting outside the optional text and ending inside it
      "<p><b>Attn: ??Company</b> responsible person title??</p>",
      // formatting starting inside the optional text and ending outside it
      "<p>??To wh<b>om?? it may concern</b></p>",
      // optional text split over two list items
      "<ul><li>??Optional text starting in one list item</li><li>and ending in another??</li></ul>",
      // complete block elements within the optional text: these parse without errors, so only the
      // inline-elements-only check catches them
      "??Optional text containing a <p>whole paragraph</p> inside??",
      "??Optional text containing a <div>whole div</div> inside??"
  })
  void validateMailMergeFields_whenManualMailMergeFieldsAreMalformed(String text) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(MALFORMED_MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE));
  }

  @Test
  void validateMailMergeFields_whenMalformedManualMailMergeFieldsAndInvalidMailMergeField() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = "<p>((MAIL_MERGE_FIELD_1)) ??Optional text starting on one line</p><p>and ending on another??</p>";

    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(
            "There are the following errors in this section: %s, %s"
                .formatted(
                    SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE.formatted("MAIL_MERGE_FIELD_1"),
                    MALFORMED_MANUAL_MAIL_MERGE_FIELD_ERROR_MESSAGE)));
  }

  @Test
  void validateMailMergeFields_whenDoNotIncludeManualMailMergeFields() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ??MAIL_MERGE_FIELD_2??
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text, false))
        .isEqualTo(DocumentMailMergeValidationResult.valid());
  }

  @Test
  void getApplicableDocumentMailMergeField_mailMergeFieldNotFound() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentMailMergeField = mock(DocumentMailMergeField.class);

    when(documentMailMergeFields.stream()).thenReturn(Stream.of(documentMailMergeField));

    when(documentMailMergeField.getMnemonic()).thenReturn("OTHER_MNEMONIC");

    assertThat(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic)).isEmpty();
  }

  @Test
  void getApplicableDocumentMailMergeField_mailMergeFieldFoundAndIsNotApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentMailMergeField = mock(DocumentMailMergeField.class);

    when(documentMailMergeFields.stream()).thenReturn(Stream.of(documentMailMergeField));

    when(documentMailMergeField.getMnemonic()).thenReturn(mnemonic);
    when(documentMailMergeField.isApplicable(documentTemplateDto)).thenReturn(false);

    assertThat(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic)).isEmpty();
  }

  @Test
  void getApplicableDocumentMailMergeField_mailMergeFieldFoundAndIsApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentMailMergeField = mock(DocumentMailMergeField.class);

    when(documentMailMergeFields.stream()).thenReturn(Stream.of(documentMailMergeField));

    when(documentMailMergeField.getMnemonic()).thenReturn(mnemonic);
    when(documentMailMergeField.isApplicable(documentTemplateDto)).thenReturn(true);

    assertThat(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic))
        .contains(documentMailMergeField);
  }
}
