package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceControllerHelperServiceTest {

  @InjectMocks
  private DocumentInstanceControllerHelperService documentInstanceControllerHelperService;

  @Test
  void getDocumentInstanceSummaryViews() {
    var documentTemplateDto1 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(1).build();
    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto1).build();
    var documentInstanceDto1ViewUrl = "test-view-url-1";

    var documentTemplateDto2 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(2).build();
    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto2).build();
    var documentInstanceDto2ViewUrl = "test-view-url-2";

    var documentTemplateDto3 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(3).build();
    var documentInstanceDto3 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto3).build();
    var documentInstanceDto3ViewUrl = "test-view-url-3";

    var documentInstanceDtos = List.of(documentInstanceDto2, documentInstanceDto1, documentInstanceDto3);

    var viewUrlsByDocumentInstanceDto = Map.of(
        documentInstanceDto1, documentInstanceDto1ViewUrl,
        documentInstanceDto2, documentInstanceDto2ViewUrl,
        documentInstanceDto3, documentInstanceDto3ViewUrl
    );
    Function<DocumentInstanceDto, String> viewUrlFunction = viewUrlsByDocumentInstanceDto::get;

    assertThat(documentInstanceControllerHelperService.getDocumentInstanceSummaryViews(documentInstanceDtos, viewUrlFunction))
        .containsExactly(
            DocumentInstanceSummaryView.from(documentInstanceDto1, documentInstanceDto1ViewUrl),
            DocumentInstanceSummaryView.from(documentInstanceDto2, documentInstanceDto2ViewUrl),
            DocumentInstanceSummaryView.from(documentInstanceDto3, documentInstanceDto3ViewUrl)
        );
  }
}
