package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentTemplateSectionConditionService {

  private final List<DocumentTemplateSectionCondition> documentTemplateSectionConditions;

  @Autowired
  DocumentTemplateSectionConditionService(List<DocumentTemplateSectionCondition> documentTemplateSectionConditions) {
    this.documentTemplateSectionConditions = documentTemplateSectionConditions;
  }

  /**
   * Gets a list of document template section conditions that are applicable to a given document template DTO.
   *
   * @param documentTemplateDto the document template DTO
   * @return the list of document template section conditions
   */
  public List<DocumentTemplateSectionCondition> getApplicableDocumentTemplateSectionConditions(
      DocumentTemplateDto documentTemplateDto
  ) {
    return documentTemplateSectionConditions.stream()
        .filter(documentTemplateSectionCondition -> documentTemplateSectionCondition.isApplicable(documentTemplateDto))
        .toList();
  }

  /**
   * Gets an applicable document template section condition for a given document template DTO with a given mnemonic or throws
   * an exception if not found.
   *
   * @param documentTemplateDto the document template DTO
   * @param mnemonic the mnemonic
   * @return the document template section condition
   * @throws IllegalStateException if the document template section condition is not found
   */
  public DocumentTemplateSectionCondition getApplicableDocumentTemplateSectionConditionOrThrow(
      DocumentTemplateDto documentTemplateDto,
      String mnemonic
  ) {
    return getApplicableDocumentTemplateSectionCondition(documentTemplateDto, mnemonic).orElseThrow(() ->
        new IllegalStateException("Unable to find applicable document section condition %s".formatted(mnemonic))
    );
  }

  /**
   * Gets an applicable document template section condition for a given document template DTO with a given mnemonic.
   *
   * @param documentTemplateDto the document template DTO
   * @param mnemonic the mnemonic
   * @return an optional containing the document template section condition if found
   */
  public Optional<DocumentTemplateSectionCondition> getApplicableDocumentTemplateSectionCondition(
      DocumentTemplateDto documentTemplateDto,
      String mnemonic
  ) {
    return documentTemplateSectionConditions.stream()
        .filter(documentTemplateSectionCondition -> documentTemplateSectionCondition.getMnemonic().equals(mnemonic))
        .filter(documentTemplateSectionCondition -> documentTemplateSectionCondition.isApplicable(documentTemplateDto))
        .findFirst();
  }

  /**
   * Gets a Map of applicable document template condition mnemonic's to the condition title's for a given document template DTO
   * that can be used for displaying the conditions in an FDS select component.
   *
   * @param documentTemplateDto the document template dto
   * @return the map of conditions
   */
  public Map<String, String> getConditionsFdsSelectMap(DocumentTemplateDto documentTemplateDto) {
    return getApplicableDocumentTemplateSectionConditions(documentTemplateDto)
        .stream()
        .collect(
            Collectors.toMap(
                DocumentTemplateSectionCondition::getMnemonic,
                DocumentTemplateSectionCondition::getTitle
            )
        );
  }
}
