package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentTemplateSummaryViewTest {

  @Test
  void from() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var viewUrl = "test-view-url";

    assertThat(DocumentTemplateSummaryView.from(documentTemplateDto, viewUrl))
        .isEqualTo(new DocumentTemplateSummaryView(documentTemplateDto.title(), documentTemplateDto.description(), viewUrl));
  }
}
