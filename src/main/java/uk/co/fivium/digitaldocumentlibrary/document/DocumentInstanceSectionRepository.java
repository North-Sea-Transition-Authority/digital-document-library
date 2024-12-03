package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentInstanceSectionRepository extends ListCrudRepository<DocumentInstanceSection, UUID> {

  List<DocumentInstanceSection> findAllByDocumentInstanceId(UUID documentInstanceId);

  List<DocumentInstanceSection> findAllByDocumentInstanceIdAndParentIdAndDisplayOrderGreaterThanEqual(
      UUID documentInstanceId,
      UUID parentId,
      int displayOrder
  );

  List<DocumentInstanceSection> findAllByDocumentInstanceIdIn(List<UUID> documentInstanceIds);

  void deleteAllByDocumentInstanceId(UUID documentInstanceId);
}
