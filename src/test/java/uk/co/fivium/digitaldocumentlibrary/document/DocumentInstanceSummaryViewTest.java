package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.fivium.digitaldocumentlibrary.mvc.ReverseRouter;

class DocumentInstanceSummaryViewTest {

  @Test
  void from() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    assertThat(DocumentInstanceSummaryView.from(documentInstanceDto, TestDocumentInstanceController.class)).isEqualTo(
        new DocumentInstanceSummaryView(
            documentInstanceDto.id(),
            documentInstanceDto.title(),
            documentInstanceDto.description(),
            ReverseRouter.route(on(TestDocumentInstanceController.class).getViewDocumentInstance(documentInstanceDto.id()))
        )
    );
  }
}
