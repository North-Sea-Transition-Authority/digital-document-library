package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentTemplateSectionFormTest {

  @Test
  void empty() {
    assertThat(DocumentTemplateSectionForm.empty())
        .extracting(
            DocumentTemplateSectionForm::getTitle,
            DocumentTemplateSectionForm::getContent,
            DocumentTemplateSectionForm::getConditionMnemonic,
            DocumentTemplateSectionForm::getNumbered,
            DocumentTemplateSectionForm::getHasPageBreakBefore)
        .containsExactly(null, null, null, null, null);
  }

  @Test
  void from() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    assertThat(DocumentTemplateSectionForm.from(documentTemplateSectionDto))
        .extracting(
            DocumentTemplateSectionForm::getTitle,
            DocumentTemplateSectionForm::getContent,
            DocumentTemplateSectionForm::getConditionMnemonic,
            DocumentTemplateSectionForm::getNumbered,
            DocumentTemplateSectionForm::getHasPageBreakBefore)
        .containsExactly(
            documentTemplateSectionDto.title(),
            documentTemplateSectionDto.content(),
            documentTemplateSectionDto.conditionMnemonic(),
            documentTemplateSectionDto.numbered(),
            documentTemplateSectionDto.hasPageBreakBefore()
        );
  }
}
