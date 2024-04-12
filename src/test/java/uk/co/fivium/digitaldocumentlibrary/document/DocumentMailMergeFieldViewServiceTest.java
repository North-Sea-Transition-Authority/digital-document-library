package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentMailMergeFieldViewServiceTest {

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  private DocumentMailMergeFieldViewService documentMailMergeFieldViewService;

  @Test
  void getApplicableDocumentMailMergeFieldMnemonics() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var applicableMailMergeField = DocumentMailMergeFieldTestUtil.builder()
        .withMnemonic("TEST_MNEMONIC_1")
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto))
        .thenReturn(List.of(applicableMailMergeField));

    assertThat(documentMailMergeFieldViewService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .containsExactly(DocumentMailMergeFieldView.from(applicableMailMergeField));
  }
}
