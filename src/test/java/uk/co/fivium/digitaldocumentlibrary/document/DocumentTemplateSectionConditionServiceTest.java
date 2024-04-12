package uk.co.fivium.digitaldocumentlibrary.document;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateSectionConditionServiceTest {

  @Mock
  private List<DocumentTemplateSectionCondition> documentTemplateSectionConditions;

  @InjectMocks
  @Spy
  private DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Test
  void getApplicableDocumentTemplateSectionConditions() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplateSectionCondition1 = mock(DocumentTemplateSectionCondition.class);
    var documentTemplateSectionCondition2 = mock(DocumentTemplateSectionCondition.class);
    var documentTemplateSectionCondition3 = mock(DocumentTemplateSectionCondition.class);

    when(documentTemplateSectionCondition1.isApplicable(documentTemplateDto)).thenReturn(true);
    when(documentTemplateSectionCondition2.isApplicable(documentTemplateDto)).thenReturn(true);
    when(documentTemplateSectionCondition3.isApplicable(documentTemplateDto)).thenReturn(false);

    when(documentTemplateSectionConditions.stream()).thenReturn(
        Stream.of(
            documentTemplateSectionCondition1,
            documentTemplateSectionCondition2,
            documentTemplateSectionCondition3
        )
    );

    assertThat(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditions(documentTemplateDto)
    ).containsExactly(documentTemplateSectionCondition1, documentTemplateSectionCondition2);
  }

  @Test
  void getApplicableDocumentTemplateSectionConditionOrThrow_applicableDocumentTemplateSectionConditionNotFound() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    doReturn(Optional.empty())
        .when(documentTemplateSectionConditionService)
        .getApplicableDocumentTemplateSectionCondition(documentTemplateDto, mnemonic);

    assertThatThrownBy(
        () -> documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateDto,
            mnemonic
        )
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void getApplicableDocumentTemplateSectionConditionOrThrow_applicableDocumentTemplateSectionConditionFound() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var condition = DocumentTemplateSectionConditionTestUtil.builder()
        .withMnemonic(mnemonic)
        .build();

    doReturn(Optional.of(condition))
        .when(documentTemplateSectionConditionService)
        .getApplicableDocumentTemplateSectionCondition(documentTemplateDto, mnemonic);

    assertThat(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateDto,
            mnemonic
        )
    ).isEqualTo(condition);
  }

  @Test
  void getApplicableDocumentTemplateSectionCondition_documentTemplateSectionConditionNotFound() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentTemplateSectionCondition = mock(DocumentTemplateSectionCondition.class);

    when(documentTemplateSectionConditions.stream()).thenReturn(Stream.of(documentTemplateSectionCondition));

    when(documentTemplateSectionCondition.getMnemonic()).thenReturn("OTHER_MNEMONIC");

    assertThat(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionCondition(
            documentTemplateDto,
            mnemonic
        )
    ).isEmpty();
  }

  @Test
  void getApplicableDocumentTemplateSectionCondition_documentTemplateSectionConditionFoundAndIsNotApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentTemplateSectionCondition = mock(DocumentTemplateSectionCondition.class);

    when(documentTemplateSectionConditions.stream()).thenReturn(Stream.of(documentTemplateSectionCondition));

    when(documentTemplateSectionCondition.getMnemonic()).thenReturn(mnemonic);
    when(documentTemplateSectionCondition.isApplicable(documentTemplateDto)).thenReturn(false);

    assertThat(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionCondition(
            documentTemplateDto,
            mnemonic
        )
    ).isEmpty();
  }

  @Test
  void getApplicableDocumentTemplateSectionCondition_documentTemplateSectionConditionFoundAndIsApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentTemplateSectionCondition = mock(DocumentTemplateSectionCondition.class);

    when(documentTemplateSectionConditions.stream()).thenReturn(Stream.of(documentTemplateSectionCondition));

    when(documentTemplateSectionCondition.getMnemonic()).thenReturn(mnemonic);
    when(documentTemplateSectionCondition.isApplicable(documentTemplateDto)).thenReturn(true);

    assertThat(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionCondition(
            documentTemplateDto,
            mnemonic
        )
    ).contains(documentTemplateSectionCondition);
  }

  @Test
  void getConditionsFdsSelectMap() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplateSectionCondition1 = DocumentTemplateSectionConditionTestUtil.builder()
        .withMnemonic("TEST_MNEMONIC_1")
        .withTitle("Test title 1")
        .build();
    var documentTemplateSectionCondition2 = DocumentTemplateSectionConditionTestUtil.builder()
        .withMnemonic("TEST_MNEMONIC_2")
        .withTitle("Test title 2")
        .build();

    when(documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditions(documentTemplateDto))
        .thenReturn(List.of(documentTemplateSectionCondition1, documentTemplateSectionCondition2));

    assertThat(documentTemplateSectionConditionService.getConditionsFdsSelectMap(documentTemplateDto))
        .containsOnly(
            entry(documentTemplateSectionCondition1.getMnemonic(), documentTemplateSectionCondition1.getTitle()),
            entry(documentTemplateSectionCondition2.getMnemonic(), documentTemplateSectionCondition2.getTitle())
        );
  }
}
