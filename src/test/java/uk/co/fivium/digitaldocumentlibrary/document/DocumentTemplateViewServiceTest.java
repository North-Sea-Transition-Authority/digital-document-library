package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateViewServiceTest {

  @Mock
  private DocumentTemplateService documentTemplateService;

  @InjectMocks
  private DocumentTemplateViewService documentTemplateViewService;

  @Test
  void getDocumentTemplateSummaryViews() {
    var documentTemplateDto1 = DocumentTemplateDtoTestUtil.builder()
        .withDisplayOrder(1)
        .build();
    var documentTemplateDto1ViewUrl = "test-view-url-1";

    var documentTemplateDto2 = DocumentTemplateDtoTestUtil.builder()
        .withDisplayOrder(2)
        .build();
    var documentTemplateDto2ViewUrl = "test-view-url-2";

    var documentTemplateDto3 = DocumentTemplateDtoTestUtil.builder()
        .withDisplayOrder(3)
        .build();
    var documentTemplateDto3ViewUrl = "test-view-url-3";

    var viewUrlsByDocumentTemplateDto = Map.of(
        documentTemplateDto1, documentTemplateDto1ViewUrl,
        documentTemplateDto2, documentTemplateDto2ViewUrl,
        documentTemplateDto3, documentTemplateDto3ViewUrl
    );
    Function<DocumentTemplateDto, String> viewUrlFunction = viewUrlsByDocumentTemplateDto::get;

    when(documentTemplateService.getDocumentTemplateDtos())
        .thenReturn(List.of(documentTemplateDto2, documentTemplateDto1, documentTemplateDto3));

    assertThat(documentTemplateViewService.getDocumentTemplateSummaryViews(viewUrlFunction)).containsExactly(
        DocumentTemplateSummaryView.from(documentTemplateDto1, documentTemplateDto1ViewUrl),
        DocumentTemplateSummaryView.from(documentTemplateDto2, documentTemplateDto2ViewUrl),
        DocumentTemplateSummaryView.from(documentTemplateDto3, documentTemplateDto3ViewUrl)
    );
  }
}
