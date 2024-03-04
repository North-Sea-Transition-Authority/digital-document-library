package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentInstanceDtoTest {

  @Test
  void from() {
    var documentInstance = DocumentInstanceTestUtil.builder().build();

    assertThat(DocumentInstanceDto.from(documentInstance)).isEqualTo(
        new DocumentInstanceDto(
            documentInstance.getId(),
            documentInstance.getItemReference(),
            documentInstance.getItemType(),
            documentInstance.getTitle(),
            documentInstance.getDescription(),
            DocumentTemplateDto.from(documentInstance.getDocumentTemplate())
        )
    );
  }
}
