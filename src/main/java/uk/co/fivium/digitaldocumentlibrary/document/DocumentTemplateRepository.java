package uk.co.fivium.digitaldocumentlibrary.document;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTemplateRepository extends ListCrudRepository<DocumentTemplate, UUID> {

  Optional<DocumentTemplate> findByMnemonic(String mnemonic);

}
