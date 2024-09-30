package uk.co.fivium.digitaldocumentlibrary.document;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceServiceTest {

  @Mock
  private DocumentInstanceRepository documentInstanceRepository;

  @Mock
  private DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService;

  @Mock
  private DocumentTemplateService documentTemplateService;

  @Mock
  private FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;

  @Mock
  private DocumentInstanceSectionRepository documentInstanceSectionRepository;

  @InjectMocks
  @Spy
  private DocumentInstanceService documentInstanceService;

  private static final MockedStatic<PdfRenderUtil> PDF_RENDER_UTILS_MOCKED_STATIC = Mockito.mockStatic(PdfRenderUtil.class);

  @AfterAll
  public static void tearDown() {
    PDF_RENDER_UTILS_MOCKED_STATIC.close();
  }

  @Test
  void createDocumentInstance() {
    var itemReference = "TEST_ITEM_REFERENCE";
    var itemType = "TEST_ITEM_TYPE";
    var title = "Test title";
    var description = "Test description";
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    when(documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id())).thenReturn(documentTemplate);

    var documentInstanceDto = documentInstanceService.createDocumentInstance(
        itemReference,
        itemType,
        title,
        description,
        documentTemplateDto
    );

    var documentInstanceCaptor = ArgumentCaptor.forClass(DocumentInstance.class);

    verify(documentInstanceRepository).save(documentInstanceCaptor.capture());

    var documentInstance = documentInstanceCaptor.getValue();

    assertThat(documentInstance)
        .extracting(
            DocumentInstance::getItemReference,
            DocumentInstance::getItemType,
            DocumentInstance::getTitle,
            DocumentInstance::getDescription,
            DocumentInstance::getDocumentTemplate
        )
        .containsExactly(
            itemReference,
            itemType,
            title,
            description,
            documentTemplate
        );

    verify(documentInstanceSectionTemplateCopyingService)
        .copyDocumentTemplateSectionsToDocumentInstance(documentInstance);

    assertThat(documentInstanceDto).isEqualTo(DocumentInstanceDto.from(documentInstance));
  }

  @Test
  void editDocumentInstance() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var title = "Test edited title";
    var description = "Test edited description";

    var documentInstance = DocumentInstanceTestUtil.builder().build();

    doReturn(documentInstance)
        .when(documentInstanceService)
        .getDocumentInstanceOrThrow(documentInstanceDto.id());

    documentInstanceService.editDocumentInstance(documentInstanceDto, title, description);

    assertThat(documentInstance)
        .extracting(
            DocumentInstance::getTitle,
            DocumentInstance::getDescription
        )
        .containsExactly(
            title,
            description
        );

    verify(documentInstanceRepository).save(documentInstance);
  }

  @Test
  void deleteDocumentInstance() {
    var documentInstance = DocumentInstanceDtoTestUtil.builder().build();
    documentInstanceService.deleteDocumentInstance(documentInstance);
    verify(documentInstanceSectionRepository).deleteAllByDocumentInstanceId(documentInstance.id());
    verify(documentInstanceRepository).deleteById(documentInstance.id());
  }

  @Test
  void getDocumentInstanceDtosByItemReference() {
    var itemReference = "itemReference";
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var documentInstanceDto = DocumentInstanceDto.from(documentInstance);

    when(documentInstanceRepository.findAllByItemReference(itemReference)).thenReturn(List.of(documentInstance));

    assertThat(documentInstanceService.getDocumentInstanceDtosByItemReference(itemReference))
        .containsExactly(documentInstanceDto);
  }

  @Test
  void getDocumentInstanceDtosByItemReference_doesNotExist() {
    var itemReference = "itemReference";
    when(documentInstanceRepository.findAllByItemReference(itemReference)).thenReturn(Collections.emptyList());

    assertThat(documentInstanceService.getDocumentInstanceDtosByItemReference(itemReference)).isEmpty();
  }

  @Test
  void getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto() {
    var itemReference = "itemReference";
    var itemType = "itemType";
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var documentInstanceDto = DocumentInstanceDto.from(documentInstance);
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(documentInstanceRepository.findByItemReferenceAndItemTypeAndDocumentTemplate_Id(itemReference, itemType, documentTemplateDto.id()))
        .thenReturn(Optional.of(documentInstance));

    assertThat(documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(itemReference, itemType, documentTemplateDto))
        .contains(documentInstanceDto);
  }

  @Test
  void getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto_doesNotExist() {
    var itemReference = "itemReference";
    var itemType = "itemType";
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(documentInstanceRepository.findByItemReferenceAndItemTypeAndDocumentTemplate_Id(itemReference, itemType, documentTemplateDto.id())).thenReturn(Optional.empty());

    assertThat(documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(itemReference, itemType, documentTemplateDto)).isEmpty();
  }

  @Test
  void getDocumentInstanceDtoOrThrow() {
    var documentInstanceId = UUID.randomUUID();

    var documentInstance = DocumentInstanceTestUtil.builder().build();

    doReturn(documentInstance).when(documentInstanceService).getDocumentInstanceOrThrow(documentInstanceId);

    assertThat(documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId))
        .isEqualTo(DocumentInstanceDto.from(documentInstance));
  }

  @Test
  void getDocumentInstanceOrThrow_documentInstanceDoesNotExist() {
    var documentInstanceId = UUID.randomUUID();

    when(documentInstanceRepository.findById(documentInstanceId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentInstanceService.getDocumentInstanceOrThrow(documentInstanceId))
        .isInstanceOf(DocumentInstanceNotFoundException.class);
  }

  @Test
  void getDocumentInstanceOrThrow_documentInstanceExists() {
    var documentInstanceId = UUID.randomUUID();

    var documentInstance = DocumentInstanceTestUtil.builder().build();

    when(documentInstanceRepository.findById(documentInstanceId)).thenReturn(Optional.of(documentInstance));

    assertThat(documentInstanceService.getDocumentInstanceOrThrow(documentInstanceId)).isEqualTo(documentInstance);
  }

  @Test
  void renderPdf() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    Map<String, Object> templateModel = Map.of("test-model-key", "test-model-value");

    var expectedModel = new HashMap<>(templateModel);
    expectedModel.put("documentInstanceDto", documentInstanceDto);

    var html = "<html></html>";

    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    when(
        freeMarkerTemplateRenderingService.renderTemplate(
            documentInstanceDto.documentTemplateDto().documentInstancePdfTemplatePath(),
            expectedModel
        )
    ).thenReturn(html);

    when(PdfRenderUtil.renderPdfFromHtml(html)).thenReturn(byteArrayResource);

    assertThat(documentInstanceService.renderPdf(documentInstanceDto, templateModel))
        .isEqualTo(new PdfRenderResult(byteArrayResource, html));
  }

  @Test
  void reloadDocumentInstance() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentInstance = DocumentInstanceTestUtil.builder().build();

    doReturn(documentInstance).when(documentInstanceService).getDocumentInstanceOrThrow(documentInstanceDto.id());

    documentInstanceService.reloadDocumentInstance(documentInstanceDto);

    verify(documentInstanceSectionTemplateCopyingService)
        .reloadDocumentInstanceSectionsFromDocumentTemplate(documentInstance);
  }
}
