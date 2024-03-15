package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentInstanceSummaryViewTest {

  @Test
  void from() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var viewUrl = "test-view-url";

    assertThat(DocumentInstanceSummaryView.from(documentInstanceDto, viewUrl)).isEqualTo(
        new DocumentInstanceSummaryView(
            documentInstanceDto.id(),
            documentInstanceDto.title(),
            documentInstanceDto.description(),
            viewUrl
        )
    );
  }
}
