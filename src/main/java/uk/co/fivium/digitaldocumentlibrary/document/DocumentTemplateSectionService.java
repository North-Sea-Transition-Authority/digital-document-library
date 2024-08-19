package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentTemplateSectionService {

  private final DocumentTemplateSectionRepository documentTemplateSectionRepository;
  private final DocumentTemplateService documentTemplateService;
  private final ContentSanitisationService contentSanitisationService;

  @Autowired
  DocumentTemplateSectionService(
      DocumentTemplateSectionRepository documentTemplateSectionRepository,
      DocumentTemplateService documentTemplateService,
      ContentSanitisationService contentSanitisationService
  ) {
    this.documentTemplateSectionRepository = documentTemplateSectionRepository;
    this.documentTemplateService = documentTemplateService;
    this.contentSanitisationService = contentSanitisationService;
  }

  /**
   * Creates a document template section with values from a form.
   *
   * @param documentTemplateDto the document template DTO to create the section in
   * @param parentDto an optional parent section of the section to create - if the section should be a top level section pass null
   * @param form a form to create the section from
   * @param displayOrder the display order of the section. If a sibling section has the same display order, the sibling section's
   *                     display order will be incremented by 1, and if that is the same as another sibling's display order, that
   *                     sibling's display order will be incremented by 1, and so on recursively
   * @return the document template section DTO created
   */
  @Transactional
  public DocumentTemplateSectionDto createDocumentTemplateSection(
      DocumentTemplateDto documentTemplateDto,
      @Nullable DocumentTemplateSectionDto parentDto,
      DocumentTemplateSectionForm form,
      int displayOrder
  ) {
    return createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        form.getTitle(),
        form.getContent(),
        form.getConditionMnemonic(),
        form.getNumbered(),
        form.getHasPageBreakBefore(),
        displayOrder
    );
  }

  /**
   * Creates a document template section.
   *
   * @param documentTemplateDto the document template DTO to create the section in
   * @param parentDto an optional parent section of the section to create - if the section should be a top level section pass null
   * @param title a title for the section
   * @param content the content of the section
   * @param conditionMnemonic an optional mnemonic of a document template section condition - if you do not want the section to
   *                          be conditional pass null. If a condition mnemonic is set, the condition will be evaluated when a
   *                          document instance is created from the template and the section will only be included in the instance
   *                          if the condition evaluates to true
   * @param numbered true if the section should be numbered
   * @param hasPageBreakBefore true if the section should have a page break before it in rendered PDFs
   * @param displayOrder the display order of the section. If a sibling section has the same display order, the sibling section's
   *                     display order will be incremented by 1, and if that is the same as another sibling's display order, that
   *                     sibling's display order will be incremented by 1, and so on recursively
   * @return the document template section DTO created
   */
  @Transactional
  public DocumentTemplateSectionDto createDocumentTemplateSection(
      DocumentTemplateDto documentTemplateDto,
      @Nullable DocumentTemplateSectionDto parentDto,
      String title,
      String content,
      String conditionMnemonic,
      boolean numbered,
      boolean hasPageBreakBefore,
      int displayOrder
  ) {
    var documentTemplate = documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id());

    var documentTemplateSection = new DocumentTemplateSection();

    documentTemplateSection.setDocumentTemplate(documentTemplate);
    if (parentDto != null) {
      documentTemplateSection.setParent(getDocumentTemplateSectionOrThrow(parentDto.id()));
    }
    documentTemplateSection.setTitle(title);
    documentTemplateSection.setContent(contentSanitisationService.getSanitisedContent(content));
    documentTemplateSection.setConditionMnemonic(conditionMnemonic);
    documentTemplateSection.setNumbered(numbered);
    documentTemplateSection.setHasPageBreakBefore(hasPageBreakBefore);
    documentTemplateSection.setDisplayOrder(displayOrder);

    var documentTemplateSectionsToSave = new ArrayList<DocumentTemplateSection>();

    documentTemplateSectionsToSave.add(documentTemplateSection);

    // If a sibling with the same display order exists, shift its display order up by 1 and any following siblings
    var siblingsWithEqualOrGreaterDisplayOrder =
        documentTemplateSectionRepository.findAllByDocumentTemplateIdAndParentIdAndDisplayOrderGreaterThanEqual(
            documentTemplate.getId(),
            parentDto != null ? parentDto.id() : null,
            displayOrder
        )
            .stream()
            .collect(Collectors.toMap(DocumentTemplateSection::getDisplayOrder, Function.identity()));

    for (int i = displayOrder; siblingsWithEqualOrGreaterDisplayOrder.containsKey(i); i++) {
      var siblingToShift = siblingsWithEqualOrGreaterDisplayOrder.get(i);

      siblingToShift.setDisplayOrder(i + 1);

      documentTemplateSectionsToSave.add(siblingToShift);
    }

    documentTemplateSectionRepository.saveAll(documentTemplateSectionsToSave);

    return DocumentTemplateSectionDto.from(documentTemplateSection, List.of());
  }

  /**
   * Edits a document template section with values from a form.
   *
   * @param documentTemplateSectionDto the document template section DTO to edit
   * @param form the form to update the section with
   */
  @Transactional
  public void editDocumentTemplateSection(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentTemplateSectionForm form
  ) {
    editDocumentTemplateSection(
        documentTemplateSectionDto,
        form.getTitle(),
        form.getContent(),
        form.getConditionMnemonic(),
        form.getNumbered(),
        form.getHasPageBreakBefore()
    );
  }

  /**
   * Edits a document template section.
   *
   * @param documentTemplateSectionDto the document template section DTO to edit
   * @param title a title for the section
   * @param content the content of the section
   * @param conditionMnemonic an optional mnemonic of a document template section condition - if you do not want the section to
   *                          be conditional pass null. If a condition mnemonic is set, the condition will be evaluated when a
   *                          document instance is created from the template and the section will only be included in the instance
   *                          if the condition evaluates to true
   * @param numbered true if the section should be numbered
   * @param hasPageBreakBefore true if the section should have a page break before it in rendered PDFs
   */
  @Transactional
  public void editDocumentTemplateSection(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      String title,
      String content,
      String conditionMnemonic,
      boolean numbered,
      boolean hasPageBreakBefore
  ) {
    var documentTemplateSection = getDocumentTemplateSectionOrThrow(documentTemplateSectionDto.id());

    documentTemplateSection.setTitle(title);
    documentTemplateSection.setContent(contentSanitisationService.getSanitisedContent(content));
    documentTemplateSection.setConditionMnemonic(conditionMnemonic);
    documentTemplateSection.setNumbered(numbered);
    documentTemplateSection.setHasPageBreakBefore(hasPageBreakBefore);

    documentTemplateSectionRepository.save(documentTemplateSection);
  }

  /**
   * Deletes a document template section.
   * <br>
   * This will also recursively delete any descendant sections which have the section being deleted as a parent, and any
   * sections which have that section as a parent, etc.
   *
   * @param documentTemplateSectionDto the document template section DTO to delete
   */
  @Transactional
  public void deleteDocumentTemplateSection(DocumentTemplateSectionDto documentTemplateSectionDto) {
    var idsToDelete = new ArrayList<UUID>();

    var documentTemplateSectionId = documentTemplateSectionDto.id();
    idsToDelete.add(documentTemplateSectionId);

    var descendantSectionIds = documentTemplateSectionDto.descendants().stream()
        .map(DocumentTemplateSectionDto::id)
        .toList();
    idsToDelete.addAll(descendantSectionIds);

    documentTemplateSectionRepository.deleteAllById(idsToDelete);
  }

  /**
   * Gets a document template section DTO by a document template section ID or throws an exception if not found.
   *
   * @param documentTemplateSectionId the ID of the document template section
   * @return the document template section DTO
   * @throws DocumentTemplateSectionNotFoundException if the document template section is not found
   */
  public DocumentTemplateSectionDto getDocumentTemplateSectionDtoOrThrow(UUID documentTemplateSectionId) {
    var documentTemplateSection = getDocumentTemplateSectionOrThrow(documentTemplateSectionId);
    var allDocumentTemplateSections = documentTemplateSectionRepository.findAllByDocumentTemplateId(
        documentTemplateSection.getDocumentTemplate().getId()
    );

    return getDocumentTemplateSectionDto(documentTemplateSection, allDocumentTemplateSections);
  }

  DocumentTemplateSectionDto getDocumentTemplateSectionDto(
      DocumentTemplateSection documentTemplateSection,
      List<DocumentTemplateSection> allDocumentTemplateSections
  ) {
    var childrenDtos = allDocumentTemplateSections.stream()
        .filter(section -> section.getParent() != null
            && section.getParent().getId().equals(documentTemplateSection.getId()))
        .map(child -> getDocumentTemplateSectionDto(child, allDocumentTemplateSections))
        .toList();

    return DocumentTemplateSectionDto.from(documentTemplateSection, childrenDtos);
  }

  DocumentTemplateSection getDocumentTemplateSectionOrThrow(UUID documentTemplateSectionId) {
    return documentTemplateSectionRepository.findById(documentTemplateSectionId)
        .orElseThrow(() ->
            new DocumentTemplateSectionNotFoundException(
                "Unable to find document template section %s".formatted(documentTemplateSectionId)
            )
        );
  }

  /**
   * Gets all top level document template section DTOs with a null parent in a given document template.
   *
   * @param documentTemplateDto the document template DTO
   * @return a list of the top level document template section DTOs
   */
  public List<DocumentTemplateSectionDto> getTopLevelDocumentTemplateSectionDtos(
      DocumentTemplateDto documentTemplateDto
  ) {
    var allDocumentTemplateSections =
        documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplateDto.id());

    return allDocumentTemplateSections.stream()
        .filter(documentTemplateSection -> documentTemplateSection.getParent() == null)
        .map(documentTemplateSection -> getDocumentTemplateSectionDto(documentTemplateSection, allDocumentTemplateSections))
        .toList();
  }

  List<DocumentTemplateSection> getDocumentTemplateSections(DocumentTemplate documentTemplate) {
    return documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplate.getId());
  }
}
