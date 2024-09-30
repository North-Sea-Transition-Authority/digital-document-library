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
public class DocumentInstanceService {

  private final DocumentInstanceRepository documentInstanceRepository;
  private final DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService;
  private final DocumentTemplateService documentTemplateService;
  private final FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;
  private final DocumentInstanceSectionRepository documentInstanceSectionRepository;

  @Autowired
  DocumentInstanceService(
      DocumentInstanceRepository documentInstanceRepository,
      DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService,
      DocumentTemplateService documentTemplateService,
      FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService,
      DocumentInstanceSectionRepository documentInstanceSectionRepository) {
    this.documentInstanceRepository = documentInstanceRepository;
    this.documentInstanceSectionTemplateCopyingService = documentInstanceSectionTemplateCopyingService;
    this.documentTemplateService = documentTemplateService;
    this.freeMarkerTemplateRenderingService = freeMarkerTemplateRenderingService;
    this.documentInstanceSectionRepository = documentInstanceSectionRepository;
  }

  /**
   * Creates a document instance from a document template.
   * <br>
   * All sections from the template will be copied to the document instance.
   *
   * @param itemReference       the item reference (e.g. the application ID)
   * @param itemType            the item type (e.g. APPLICATION)
   * @param title               a title for the document instance (e.g. Field Production Consent)
   * @param description         a description for the template (e.g. Production Consent document for this application)
   * @param documentTemplateDto the document template DTO to create this document instance from
   * @return the document instance DTO created
   */
  @Transactional
  public DocumentInstanceDto createDocumentInstance(
      String itemReference,
      String itemType,
      String title,
      String description,
      DocumentTemplateDto documentTemplateDto
  ) {
    var documentTemplate = documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id());

    var documentInstance = new DocumentInstance();

    documentInstance.setItemReference(itemReference);
    documentInstance.setItemType(itemType);
    documentInstance.setTitle(title);
    documentInstance.setDescription(description);
    documentInstance.setDocumentTemplate(documentTemplate);

    documentInstanceRepository.save(documentInstance);

    documentInstanceSectionTemplateCopyingService.copyDocumentTemplateSectionsToDocumentInstance(documentInstance);

    return DocumentInstanceDto.from(documentInstance);
  }

  /**
   * Edits a document instance.
   * @param documentInstanceDto the document instance DTO to edit
   * @param title a title for the document instance
   * @param description a description for the document instance
   */
  @Transactional
  public void editDocumentInstance(DocumentInstanceDto documentInstanceDto,
                                   String title,
                                   String description) {
    var documentInstance = getDocumentInstanceOrThrow(documentInstanceDto.id());

    documentInstance.setTitle(title);
    documentInstance.setDescription(description);

    documentInstanceRepository.save(documentInstance);
  }

  /**
   * Deletes a document instance, along with all of its sections.
   * @param documentInstanceDto the document instance DTO to delete
   */
  @Transactional
  public void deleteDocumentInstance(DocumentInstanceDto documentInstanceDto) {
    documentInstanceSectionRepository.deleteAllByDocumentInstanceId(documentInstanceDto.id());
    documentInstanceRepository.deleteById(documentInstanceDto.id());
  }

  /**
   * Gets all document instance DTOs with a given item reference.
   *
   * @param itemReference the item reference
   * @return a list of the document instance DTOs
   */
  public List<DocumentInstanceDto> getDocumentInstanceDtosByItemReference(String itemReference) {
    return documentInstanceRepository.findAllByItemReference(itemReference).stream().map(DocumentInstanceDto::from).toList();
  }

  /**
   * Gets all document instance DTOs with a given item reference.
   *
   * @param itemReferences the item references
   * @return a list of the document instance DTOs
   */
  public List<DocumentInstanceDto> getDocumentInstanceDtosByItemReferences(List<String> itemReferences) {
    return documentInstanceRepository.findAllByItemReferenceIn(itemReferences).stream().map(DocumentInstanceDto::from).toList();
  }

  /**
   * Gets a document instance DTO by an item reference, item type and document template DTO.
   *
   * @param itemReference       the item reference
   * @param itemType            the item type
   * @param documentTemplateDto the document template DTO
   * @return an optional containing the document instance DTO if found
   */
  public Optional<DocumentInstanceDto> getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
      String itemReference,
      String itemType,
      DocumentTemplateDto documentTemplateDto
  ) {
    return documentInstanceRepository
        .findByItemReferenceAndItemTypeAndDocumentTemplate_Id(itemReference, itemType, documentTemplateDto.id())
        .map(DocumentInstanceDto::from);
  }

  /**
   * Gets a document instance DTO by a document instance ID or throws an exception if not found.
   *
   * @param documentInstanceId the ID of the document instance
   * @return the document instance DTO
   * @throws DocumentInstanceNotFoundException if the document instance is not found
   */
  public DocumentInstanceDto getDocumentInstanceDtoOrThrow(UUID documentInstanceId) {
    return DocumentInstanceDto.from(getDocumentInstanceOrThrow(documentInstanceId));
  }

  DocumentInstance getDocumentInstanceOrThrow(UUID documentInstanceId) {
    return documentInstanceRepository.findById(documentInstanceId)
        .orElseThrow(() ->
            new DocumentInstanceNotFoundException("Unable to find document instance %s".formatted(documentInstanceId))
        );
  }

  /**
   * Renders a document instance into a PDF using the document instance's document template Freemarker template.
   *
   * @param documentInstanceDto the document instance DTO
   * @param templateModel       a Map of objects to be made available in the Freemarker template context
   * @return a PdfRenderResult containing the rendered PDF content bytes and the HTML used to render the PDF.
   */
  public PdfRenderResult renderPdf(DocumentInstanceDto documentInstanceDto, Map<String, Object> templateModel) {
    var documentInstanceId = documentInstanceDto.id();

    var model = new HashMap<>(templateModel);
    model.put("documentInstanceDto", documentInstanceDto);

    try {
      var pdfHtml = freeMarkerTemplateRenderingService.renderTemplate(
          documentInstanceDto.documentTemplateDto().documentInstancePdfTemplatePath(),
          model
      );

      var pdfContent = PdfRenderUtil.renderPdfFromHtml(pdfHtml);

      return new PdfRenderResult(pdfContent, pdfHtml);
    } catch (Exception exception) {
      throw new RuntimeException(
          "Exception rendering PDF for document instance: %s".formatted(documentInstanceId),
          exception
      );
    }
  }

  /**
   * Deletes all of a document instance's sections and recreates them by copying all the sections from the document instance's
   * document template to the document instance.
   *
   * @param documentInstanceDto the document instance DTO
   */
  @Transactional
  public void reloadDocumentInstance(DocumentInstanceDto documentInstanceDto) {
    var documentInstance = getDocumentInstanceOrThrow(documentInstanceDto.id());

    documentInstanceSectionTemplateCopyingService.reloadDocumentInstanceSectionsFromDocumentTemplate(documentInstance);
  }
}
