package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateServiceTest {

  @Mock
  private DocumentTemplateRepository documentTemplateRepository;

  @Mock
  private FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;

  @InjectMocks
  @Spy
  private DocumentTemplateService documentTemplateService;

  private static final MockedStatic<PdfRenderUtil> PDF_RENDER_UTILS_MOCKED_STATIC = Mockito.mockStatic(PdfRenderUtil.class);

  @AfterAll
  public static void tearDown() {
    PDF_RENDER_UTILS_MOCKED_STATIC.close();
  }
  @Test
  void createDocumentTemplate() {
    var mnemonic = "TEST_MNEMONIC";
    var title = "Test title";
    var description = "Test description";
    var documentInstancePdfTemplatePath = "test/document/instance/pdf/template/path";
    var displayOrder = 1;

    var documentTemplateDto =
        documentTemplateService.createDocumentTemplate(mnemonic, title, description, documentInstancePdfTemplatePath, displayOrder);

    var documentTemplateCaptor = ArgumentCaptor.forClass(DocumentTemplate.class);

    verify(documentTemplateRepository).save(documentTemplateCaptor.capture());

    var documentTemplate = documentTemplateCaptor.getValue();

    assertThat(documentTemplate)
        .extracting(
            DocumentTemplate::getMnemonic,
            DocumentTemplate::getTitle,
            DocumentTemplate::getDescription,
            DocumentTemplate::getDocumentInstancePdfTemplatePath,
            DocumentTemplate::getDisplayOrder
        ).containsExactly(
            mnemonic,
            title,
            description,
            documentInstancePdfTemplatePath,
            displayOrder
        );

    assertThat(documentTemplateDto).isEqualTo(DocumentTemplateDto.from(documentTemplate));
  }

  @Test
  void getDocumentTemplateDtoOrThrow() {
    var documentTemplateId = UUID.randomUUID();

    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    doReturn(documentTemplate).when(documentTemplateService).getDocumentTemplateOrThrow(documentTemplateId);

    assertThat(documentTemplateService.getDocumentTemplateDtoOrThrow(documentTemplateId))
        .isEqualTo(DocumentTemplateDto.from(documentTemplate));
  }

  @Test
  void getDocumentTemplateOrThrow_documentTemplateDoesNotExist() {
    var documentTemplateId = UUID.randomUUID();

    when(documentTemplateRepository.findById(documentTemplateId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentTemplateService.getDocumentTemplateOrThrow(documentTemplateId))
        .isInstanceOf(DocumentTemplateNotFoundException.class);
  }

  @Test
  void getDocumentTemplateOrThrow_documentTemplateExists() {
    var documentTemplateId = UUID.randomUUID();

    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    when(documentTemplateRepository.findById(documentTemplateId)).thenReturn(Optional.of(documentTemplate));

    assertThat(documentTemplateService.getDocumentTemplateOrThrow(documentTemplateId)).isEqualTo(documentTemplate);
  }

  @Test
  void getDocumentTemplateDtos() {
    var documentTemplate1 = DocumentTemplateTestUtil.builder().build();
    var documentTemplate2 = DocumentTemplateTestUtil.builder().build();

    when(documentTemplateRepository.findAll()).thenReturn(List.of(documentTemplate1, documentTemplate2));

    assertThat(documentTemplateService.getDocumentTemplateDtos()).containsExactly(
        DocumentTemplateDto.from(documentTemplate1),
        DocumentTemplateDto.from(documentTemplate2)
    );
  }

  @Test
  void getDocumentTemplateDtoByMnemonicOrThrow() {
    var mnemonic = "mnemonic";
    var documentTemplate = DocumentTemplateTestUtil.builder().build();

    when(documentTemplateRepository.findByMnemonic(mnemonic)).thenReturn(Optional.of(documentTemplate));

    assertThat(documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(mnemonic))
        .isEqualTo(DocumentTemplateDto.from(documentTemplate));
  }

  @Test
  void getDocumentTemplateDtoByMnemonicOrThrow_whenNotFound() {
    var mnemonic = "mnemonic";

    when(documentTemplateRepository.findByMnemonic(mnemonic)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(mnemonic))
        .isInstanceOf(DocumentTemplateNotFoundException.class)
        .hasMessage("Unable to find document template with mnemonic [mnemonic]");
  }

  @Test
  void renderPdf() throws Exception {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    Map<String, Object> templateModel = Map.of("test-model-key", "test-model-value", "documentTemplateDto", documentTemplateDto);

    var html = "<html></html>";
    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    when(freeMarkerTemplateRenderingService.renderTemplate(documentTemplateDto.documentInstancePdfTemplatePath(), new HashMap<>(templateModel)))
            .thenReturn(html);

    Mockito.when(PdfRenderUtil.renderPdfFromHtml(html)).thenReturn(byteArrayResource);
    assertThat(documentTemplateService.renderPdf(documentTemplateDto, templateModel))
            .isEqualTo(new PdfRenderResult(byteArrayResource, html));
  }
}
