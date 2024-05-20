package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTemplateSectionRepository extends ListCrudRepository<DocumentTemplateSection, UUID> {

  List<DocumentTemplateSection> findAllByDocumentTemplateId(UUID documentTemplateId);

  List<DocumentTemplateSection> findAllByDocumentTemplateIdAndParentIdAndDisplayOrderGreaterThanEqual(
      UUID documentTemplateId,
      UUID parentId,
      int displayOrder
  );
}
