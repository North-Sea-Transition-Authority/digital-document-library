package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class DocumentTemplateService {

  private final DocumentTemplateRepository documentTemplateRepository;
  private final FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;

  @Autowired
  DocumentTemplateService(DocumentTemplateRepository documentTemplateRepository,
                          FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService) {
    this.documentTemplateRepository = documentTemplateRepository;
    this.freeMarkerTemplateRenderingService = freeMarkerTemplateRenderingService;
  }

  /**
   * Creates a document template.
   *
   * @param mnemonic a unique mnemonic for the template (e.g. FIELD_PRODUCTION_CONSENT)
   * @param title a title for the template (e.g. Field Production Consent)
   * @param description a description for the template (e.g. Document template used for creating Field Production Consent
   *                    documents)
   * @param documentInstancePdfTemplatePath a Freemarker template path used for rendering document instances created from the
   *                                        template into a PDF
   * @param displayOrder the display order for the template
   * @return the document template DTO created
   */
  @Transactional
  public DocumentTemplateDto createDocumentTemplate(
      String mnemonic,
      String title,
      String description,
      String documentInstancePdfTemplatePath,
      int displayOrder
  ) {
    var documentTemplate = new DocumentTemplate();

    documentTemplate.setMnemonic(mnemonic);
    documentTemplate.setTitle(title);
    documentTemplate.setDescription(description);
    documentTemplate.setDocumentInstancePdfTemplatePath(documentInstancePdfTemplatePath);
    documentTemplate.setDisplayOrder(displayOrder);

    documentTemplateRepository.save(documentTemplate);

    return DocumentTemplateDto.from(documentTemplate);
  }

  /**
   * Edits a document template.
   * @param documentTemplateDto the document template DTO to edit
   * @param title a title for the document template
   * @param description a description for the document template
   */
  @Transactional
  public void editDocumentTemplate(DocumentTemplateDto documentTemplateDto,
                                   String title,
                                   String description) {
    var documentTemplate = getDocumentTemplateOrThrow(documentTemplateDto.id());

    documentTemplate.setTitle(title);
    documentTemplate.setDescription(description);

    documentTemplateRepository.save(documentTemplate);
  }

  /**
   * Gets a document template DTO by a document template ID or throws an exception if not found.
   *
   * @param documentTemplateId the ID of the document template
   * @return the document template DTO
   * @throws DocumentTemplateNotFoundException if the document template is not found
   */
  public DocumentTemplateDto getDocumentTemplateDtoOrThrow(UUID documentTemplateId) {
    return DocumentTemplateDto.from(getDocumentTemplateOrThrow(documentTemplateId));
  }

  DocumentTemplate getDocumentTemplateOrThrow(UUID documentTemplateId) {
    return documentTemplateRepository.findById(documentTemplateId)
        .orElseThrow(() ->
            new DocumentTemplateNotFoundException("Unable to find document template %s".formatted(documentTemplateId))
        );
  }

  /**
   * Gets all document template DTOs.
   *
   * @return a list of the document template DTOs
   */
  public List<DocumentTemplateDto> getDocumentTemplateDtos() {
    return documentTemplateRepository.findAll().stream()
        .map(DocumentTemplateDto::from)
        .toList();
  }

  /**
   * Gets a document template DTO by a mnemonic.
   *
   * @param mnemonic the mnemonic of the document template
   * @return an optional containing the document template DTO if found
   */
  public Optional<DocumentTemplateDto> getDocumentTemplateDtoByMnemonic(String mnemonic) {
    return documentTemplateRepository.findByMnemonic(mnemonic).map(DocumentTemplateDto::from);
  }

  /**
   * Gets a document template DTO by a mnemonic or throws an exception if not found.
   *
   * @return the document template DTO
   * @throws DocumentTemplateNotFoundException if the document template is not found
   */
  public DocumentTemplateDto getDocumentTemplateDtoByMnemonicOrThrow(String mnemonic) {
    return getDocumentTemplateDtoByMnemonic(mnemonic)
        .orElseThrow(() ->
            new DocumentTemplateNotFoundException("Unable to find document template with mnemonic [%s]".formatted(mnemonic))
        );
  }

  /**
   * Renders a document template into a PDF using the document template's Freemarker template.
   *
   * @param documentTemplateDto the document template DTO
   * @param templateModel a Map of objects to be made available in the Freemarker template context
   * @return a PdfRenderResult containing the rendered PDF content bytes and the HTML used to render the PDF.
   */
  public PdfRenderResult renderPdf(DocumentTemplateDto documentTemplateDto, Map<String, Object> templateModel) {
    var documentTemplateId = documentTemplateDto.id();
    var model = new HashMap<>(templateModel);
    model.put("documentTemplateDto", documentTemplateDto);

    try {
      var pdfHtml = freeMarkerTemplateRenderingService.renderTemplate(
              documentTemplateDto.documentInstancePdfTemplatePath(),
              model
      );
      var pdfContent = PdfRenderUtil.renderPdfFromHtml(pdfHtml);
      return new PdfRenderResult(pdfContent, pdfHtml);

    } catch (Exception exception) {
      throw new RuntimeException("Exception rendering PDF for document template: %s".formatted(documentTemplateId), exception);
    }
  }
}
