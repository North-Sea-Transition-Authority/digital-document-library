package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class DocumentInstanceSectionCopyingService {

  private final DocumentInstanceSectionRepository documentInstanceSectionRepository;
  private final DocumentTemplateSectionService documentTemplateSectionService;
  private final DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Autowired
  DocumentInstanceSectionCopyingService(
      DocumentInstanceSectionRepository documentInstanceSectionRepository,
      DocumentTemplateSectionService documentTemplateSectionService,
      DocumentTemplateSectionConditionService documentTemplateSectionConditionService) {
    this.documentInstanceSectionRepository = documentInstanceSectionRepository;
    this.documentTemplateSectionService = documentTemplateSectionService;
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
  }

  void copyDocumentTemplateSectionsToDocumentInstance(DocumentInstance documentInstance) {
    var allDocumentTemplateSections =
        documentTemplateSectionService.getDocumentTemplateSections(documentInstance.getDocumentTemplate());
    var copiedDocumentInstanceSections = allDocumentTemplateSections.stream()
        .filter(section -> section.getParent() == null)
        .flatMap(child ->
            tryCopyDocumentTemplateSectionAndChildren(
                child,
                documentInstance,
                null,
                allDocumentTemplateSections
            ).stream()
        )
        .toList();

    documentInstanceSectionRepository.saveAll(copiedDocumentInstanceSections);
  }

  void copyDocumentInstanceSectionsToDocumentInstance(DocumentInstance newDocumentInstance,
                                                      DocumentInstanceDto oldDocumentInstanceDto) {
    var allDocumentInstanceSections = documentInstanceSectionRepository.findAllByDocumentInstanceId(oldDocumentInstanceDto.id());
    var copiedDocumentInstanceSections = allDocumentInstanceSections.stream()
        .filter(section -> section.getParent() == null)
        .flatMap(child ->
            tryCopyDocumentInstanceSectionAndChildren(
                child,
                newDocumentInstance,
                null,
                allDocumentInstanceSections
            ).stream()
        )
        .toList();
    documentInstanceSectionRepository.saveAll(copiedDocumentInstanceSections);
  }

  List<DocumentInstanceSection> tryCopyDocumentTemplateSectionAndChildren(
      DocumentTemplateSection documentTemplateSection,
      DocumentInstance documentInstance,
      DocumentInstanceSection parent,
      List<DocumentTemplateSection> allDocumentTemplateSections
  ) {
    var conditionMnemonic = documentTemplateSection.getConditionMnemonic();
    if (conditionMnemonic != null) {
      var condition = documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
          DocumentTemplateDto.from(documentInstance.getDocumentTemplate()),
          conditionMnemonic
      );
      if (!condition.evaluate(DocumentInstanceDto.from(documentInstance))) {
        return List.of();
      }
    }

    var documentInstanceSection = newDocumentInstanceSection(documentTemplateSection, documentInstance, parent);

    var copiedChildren = allDocumentTemplateSections.stream()
        .filter(section -> section.getParent() != null
            && section.getParent().getId().equals(documentTemplateSection.getId()))
        .flatMap(child ->
            tryCopyDocumentTemplateSectionAndChildren(
                child,
                documentInstance,
                documentInstanceSection,
                allDocumentTemplateSections
            ).stream()
        );

    return Stream.concat(Stream.of(documentInstanceSection), copiedChildren).toList();
  }

  List<DocumentInstanceSection> tryCopyDocumentInstanceSectionAndChildren(
      DocumentInstanceSection documentInstanceSection,
      DocumentInstance documentInstance,
      DocumentInstanceSection parent,
      List<DocumentInstanceSection> allDocumentInstanceSections
  ) {

    var newDocumentInstanceSection = newDocumentInstanceSectionFromInstance(documentInstanceSection, documentInstance, parent);

    var copiedChildren = allDocumentInstanceSections.stream()
        .filter(section -> section.getParent() != null
            && section.getParent().getId().equals(documentInstanceSection.getId()))
        .flatMap(child ->
            tryCopyDocumentInstanceSectionAndChildren(
                child,
                documentInstance,
                newDocumentInstanceSection,
                allDocumentInstanceSections
            ).stream()
        );

    return Stream.concat(Stream.of(newDocumentInstanceSection), copiedChildren).toList();
  }

  DocumentInstanceSection newDocumentInstanceSection(
      DocumentTemplateSection documentTemplateSection,
      DocumentInstance documentInstance,
      DocumentInstanceSection parent
  ) {
    var documentInstanceSection = new DocumentInstanceSection();

    documentInstanceSection.setDocumentInstance(documentInstance);
    documentInstanceSection.setCreatedFromDocumentTemplateSection(documentTemplateSection);
    documentInstanceSection.setParent(parent);
    documentInstanceSection.setTitle(documentTemplateSection.getTitle());
    documentInstanceSection.setContent(documentTemplateSection.getContent());
    documentInstanceSection.setNumbered(documentTemplateSection.isNumbered());
    documentInstanceSection.setHasPageBreakBefore(documentTemplateSection.hasPageBreakBefore());
    documentInstanceSection.setDisplayOrder(documentTemplateSection.getDisplayOrder());

    return documentInstanceSection;
  }

  DocumentInstanceSection newDocumentInstanceSectionFromInstance(
      DocumentInstanceSection documentInstanceSection,
      DocumentInstance documentInstance,
      DocumentInstanceSection parent
  ) {
    var newDocumentInstanceSection = new DocumentInstanceSection();

    newDocumentInstanceSection.setDocumentInstance(documentInstance);
    newDocumentInstanceSection.setCreatedFromDocumentTemplateSection(
        documentInstanceSection.getCreatedFromDocumentTemplateSection()
    );
    newDocumentInstanceSection.setParent(parent);
    newDocumentInstanceSection.setTitle(documentInstanceSection.getTitle());
    newDocumentInstanceSection.setContent(documentInstanceSection.getContent());
    newDocumentInstanceSection.setNumbered(documentInstanceSection.isNumbered());
    newDocumentInstanceSection.setHasPageBreakBefore(documentInstanceSection.hasPageBreakBefore());
    newDocumentInstanceSection.setDisplayOrder(documentInstanceSection.getDisplayOrder());

    newDocumentInstanceSection.setContent(documentInstanceSection.getContent());
    return newDocumentInstanceSection;
  }

  void reloadDocumentInstanceSectionsFromDocumentTemplate(DocumentInstance documentInstance) {
    documentInstanceSectionRepository.deleteAllByDocumentInstanceId(documentInstance.getId());

    copyDocumentTemplateSectionsToDocumentInstance(documentInstance);
  }
}
