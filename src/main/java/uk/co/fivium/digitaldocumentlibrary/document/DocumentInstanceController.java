package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.UUID;
import org.springframework.web.servlet.ModelAndView;

public interface DocumentInstanceController {

  ModelAndView getViewDocumentInstance(UUID documentInstanceId);
}
