package uk.co.fivium.digitaldocumentlibrary.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import uk.co.fivium.digitaldocumentlibrary.mvc.ReverseRouter;

public record DocumentInstanceSummaryView(
    UUID documentInstanceId,
    String title,
    String description,
    String viewUrl
) {

  static DocumentInstanceSummaryView from(
      DocumentInstanceDto documentInstanceDto,
      Class<? extends DocumentInstanceController> documentInstanceControllerClass
  ) {
    return new DocumentInstanceSummaryView(
        documentInstanceDto.id(),
        documentInstanceDto.title(),
        documentInstanceDto.description(),
        ReverseRouter.route(on(documentInstanceControllerClass).getViewDocumentInstance(documentInstanceDto.id()))
    );
  }
}
