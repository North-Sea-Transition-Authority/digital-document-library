package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentInstanceSectionRepository extends ListCrudRepository<DocumentInstanceSection, UUID> {

  List<DocumentInstanceSection> findAllByDocumentInstanceId(UUID documentInstanceId);

  List<DocumentInstanceSection> findAllByParent_IdAndDisplayOrderGreaterThanEqual(UUID parentId, int displayOrder);

  void deleteAllByDocumentInstanceId(UUID documentInstanceId);
}
