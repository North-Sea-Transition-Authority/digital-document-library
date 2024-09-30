package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentInstanceRepository extends ListCrudRepository<DocumentInstance, UUID> {

  List<DocumentInstance> findAllByItemReference(String itemReference);

  List<DocumentInstance> findAllByItemReferenceIn(List<String> itemReference);

  Optional<DocumentInstance> findByItemReferenceAndItemTypeAndDocumentTemplate_Id(
      String itemReference,
      String itemType,
      UUID documentTemplateId
  );

}
