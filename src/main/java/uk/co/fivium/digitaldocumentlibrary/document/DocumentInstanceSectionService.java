package uk.co.fivium.digitaldocumentlibrary.document;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentInstanceSectionService {

  private final DocumentInstanceSectionRepository documentInstanceSectionRepository;
  private final DocumentInstanceService documentInstanceService;
  private final ContentSanitisationService contentSanitisationService;

  @Autowired
  DocumentInstanceSectionService(
      DocumentInstanceSectionRepository documentInstanceSectionRepository,
      DocumentInstanceService documentInstanceService,
      ContentSanitisationService contentSanitisationService
  ) {
    this.documentInstanceSectionRepository = documentInstanceSectionRepository;
    this.documentInstanceService = documentInstanceService;
    this.contentSanitisationService = contentSanitisationService;
  }

  /**
   * Creates a document instance section with values from a form.
   *
   * @param documentInstanceDto the document instance DTO to create the section in
   * @param parentDto an optional parent section of the section to create - if the section should be a top level section pass null
   * @param form a form to create the section from
   * @param displayOrder the display order of the section. If a sibling section has the same display order, the sibling section's
   *                     display order will be incremented by 1, and if that is the same as another sibling's display order, that
   *                     sibling's display order will be incremented by 1, and so on recursively
   * @return the document instance section DTO created
   */
  @Transactional
  public DocumentInstanceSectionDto createDocumentInstanceSection(
      DocumentInstanceDto documentInstanceDto,
      @Nullable DocumentInstanceSectionDto parentDto,
      DocumentInstanceSectionForm form,
      int displayOrder
  ) {
    return createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore(),
        displayOrder
    );
  }

  /**
   * Creates a document instance section.
   *
   * @param documentInstanceDto the document instance DTO to create the section in
   * @param parentDto an optional parent section of the section to create - if the section should be a top level section pass null
   * @param title a title for the section
   * @param content the content of the section
   * @param numbered true if the section should be numbered
   * @param hasPageBreakBefore true if the section should have a page break before it in rendered PDFs
   * @param displayOrder the display order of the section. If a sibling section has the same display order, the sibling section's
   *                     display order will be incremented by 1, and if that is the same as another sibling's display order, that
   *                     sibling's display order will be incremented by 1, and so on recursively
   * @return the document instance section DTO created
   */
  @Transactional
  public DocumentInstanceSectionDto createDocumentInstanceSection(
      DocumentInstanceDto documentInstanceDto,
      @Nullable DocumentInstanceSectionDto parentDto,
      String title,
      String content,
      boolean numbered,
      boolean hasPageBreakBefore,
      int displayOrder
  ) {
    var documentInstance = documentInstanceService.getDocumentInstanceOrThrow(documentInstanceDto.id());

    var documentInstanceSection = new DocumentInstanceSection();

    documentInstanceSection.setDocumentInstance(documentInstance);
    if (parentDto != null) {
      documentInstanceSection.setParent(getDocumentInstanceSectionOrThrow(parentDto.id()));
    }
    documentInstanceSection.setTitle(title);
    documentInstanceSection.setContent(contentSanitisationService.getSanitisedContent(content));
    documentInstanceSection.setNumbered(numbered);
    documentInstanceSection.setHasPageBreakBefore(hasPageBreakBefore);
    documentInstanceSection.setDisplayOrder(displayOrder);

    var documentInstanceSectionsToSave = new ArrayList<DocumentInstanceSection>();

    documentInstanceSectionsToSave.add(documentInstanceSection);

    // If a sibling with the same display order exists, shift its display order up by 1 and any following siblings
    var siblingsWithEqualOrGreaterDisplayOrder =
        documentInstanceSectionRepository.findAllByDocumentInstanceIdAndParentIdAndDisplayOrderGreaterThanEqual(
                documentInstance.getId(),
                parentDto != null ? parentDto.id() : null,
                displayOrder
            )
            .stream()
            .collect(Collectors.toMap(DocumentInstanceSection::getDisplayOrder, Function.identity()));

    for (int i = displayOrder; siblingsWithEqualOrGreaterDisplayOrder.containsKey(i); i++) {
      var siblingToShift = siblingsWithEqualOrGreaterDisplayOrder.get(i);

      siblingToShift.setDisplayOrder(i + 1);

      documentInstanceSectionsToSave.add(siblingToShift);
    }

    documentInstanceSectionRepository.saveAll(documentInstanceSectionsToSave);

    return DocumentInstanceSectionDto.from(documentInstanceSection, List.of());
  }

  /**
   * Edits a document instance section with values from a form.
   *
   * @param documentInstanceSectionDto the document instance section DTO to edit
   * @param form the form to update the section with
   */
  @Transactional
  public void editDocumentInstanceSection(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore()
    );
  }

  /**
   * Edits a document instance section.
   *
   * @param documentInstanceSectionDto the document instance section DTO to edit
   * @param title a title for the section
   * @param content the content of the section
   * @param numbered true if the section should be numbered
   * @param hasPageBreakBefore true if the section should have a page break before it in rendered PDFs
   */
  @Transactional
  public void editDocumentInstanceSection(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      String title,
      String content,
      boolean numbered,
      boolean hasPageBreakBefore
  ) {
    var documentInstanceSection = getDocumentInstanceSectionOrThrow(documentInstanceSectionDto.id());

    documentInstanceSection.setTitle(title);
    documentInstanceSection.setContent(contentSanitisationService.getSanitisedContent(content));
    documentInstanceSection.setNumbered(numbered);
    documentInstanceSection.setHasPageBreakBefore(hasPageBreakBefore);

    documentInstanceSectionRepository.save(documentInstanceSection);
  }

  /**
   * Deletes a document instance section.
   * <br>
   * This will also recursively delete any descendant sections which have the section being deleted as a parent, and any
   * sections which have that section as a parent, etc.
   *
   * @param documentInstanceSectionDto the document instance section DTO to delete
   */
  @Transactional
  public void deleteDocumentInstanceSection(DocumentInstanceSectionDto documentInstanceSectionDto) {
    var idsToDelete = new ArrayList<UUID>();

    var documentInstanceSectionId = documentInstanceSectionDto.id();
    idsToDelete.add(documentInstanceSectionId);

    var descendantSectionIds = documentInstanceSectionDto.descendants().stream()
        .map(DocumentInstanceSectionDto::id)
        .toList();
    idsToDelete.addAll(descendantSectionIds);

    documentInstanceSectionRepository.deleteAllById(idsToDelete);
  }

  /**
   * Gets a document instance section DTO by a document instance section ID or throws an exception if not found.
   *
   * @param documentInstanceSectionId the ID of the document instance section
   * @return the document instance section DTO
   * @throws DocumentInstanceSectionNotFoundException if the document instance section is not found
   */
  public DocumentInstanceSectionDto getDocumentInstanceSectionDtoOrThrow(UUID documentInstanceSectionId) {
    var documentInstanceSection = getDocumentInstanceSectionOrThrow(documentInstanceSectionId);
    var allDocumentInstanceSections = documentInstanceSectionRepository.findAllByDocumentInstanceId(
        documentInstanceSection.getDocumentInstance().getId()
    );

    return getDocumentInstanceSectionDto(documentInstanceSection, allDocumentInstanceSections);
  }

  DocumentInstanceSectionDto getDocumentInstanceSectionDto(
      DocumentInstanceSection documentInstanceSection,
      List<DocumentInstanceSection> allDocumentInstanceSections
  ) {
    var childrenDtos = allDocumentInstanceSections.stream()
        .filter(section -> section.getParent() != null
            && section.getParent().getId().equals(documentInstanceSection.getId()))
        .map(child -> getDocumentInstanceSectionDto(child, allDocumentInstanceSections))
        .toList();

    return DocumentInstanceSectionDto.from(documentInstanceSection, childrenDtos);
  }

  DocumentInstanceSection getDocumentInstanceSectionOrThrow(UUID documentInstanceSectionId) {
    return documentInstanceSectionRepository.findById(documentInstanceSectionId)
        .orElseThrow(() ->
            new DocumentInstanceSectionNotFoundException(
                "Unable to find document instance section %s".formatted(documentInstanceSectionId)
            )
        );
  }

  /**
   * Gets all top level document instance section DTOs with a null parent in a given document instance.
   *
   * @param documentInstanceDto the document instance DTO
   * @return a list of the top level document instance section DTOs
   */
  public List<DocumentInstanceSectionDto> getTopLevelDocumentInstanceSectionDtos(
      DocumentInstanceDto documentInstanceDto
  ) {
    var allDocumentInstanceSections =
        documentInstanceSectionRepository.findAllByDocumentInstanceId(documentInstanceDto.id());

    return allDocumentInstanceSections.stream()
        .filter(documentInstanceSection -> documentInstanceSection.getParent() == null)
        .map(documentInstanceSection -> getDocumentInstanceSectionDto(documentInstanceSection, allDocumentInstanceSections))
        .toList();
  }

  /**
   * Gets all top level document instance section DTOs with a null parent, mapped to their document instance DTO.
   *
   * @param documentInstanceDtos the document instance DTOs
   * @return a map of each document instance DTO to its top level document instance section DTOs
   */
  Map<DocumentInstanceDto, List<DocumentInstanceSectionDto>> getTopLevelDocumentInstanceSectionDtosForDocumentInstanceDtos(
      List<DocumentInstanceDto> documentInstanceDtos
  ) {
    var documentInstanceDtoIds = documentInstanceDtos.stream()
        .map(DocumentInstanceDto::id)
        .toList();
    var allDocumentInstanceSections = documentInstanceSectionRepository.findAllByDocumentInstanceIdIn(documentInstanceDtoIds);

    return allDocumentInstanceSections.stream()
        .filter(documentInstanceSection -> Objects.isNull(documentInstanceSection.getParent()))
        .map(documentInstanceSection -> getDocumentInstanceSectionDto(documentInstanceSection, allDocumentInstanceSections))
        .collect(Collectors.groupingBy(DocumentInstanceSectionDto::documentInstanceDto));
  }
}
