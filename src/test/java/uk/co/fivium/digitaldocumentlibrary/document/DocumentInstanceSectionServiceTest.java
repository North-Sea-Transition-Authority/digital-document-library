package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceSectionServiceTest {

  @Mock
  private DocumentInstanceSectionRepository documentInstanceSectionRepository;

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private ContentSanitisationService contentSanitisationService;

  @InjectMocks
  @Spy
  private DocumentInstanceSectionService documentInstanceSectionService;

  @Captor
  private ArgumentCaptor<List<DocumentInstanceSection>> documentInstanceSectionListCaptor;

  @Test
  void createDocumentInstanceSection_withForm() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var parentDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var form = DocumentInstanceSectionFormTestUtil.builder().build();
    var displayOrder = 1;

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    doReturn(documentInstanceSectionDto)
        .when(documentInstanceSectionService)
        .createDocumentInstanceSection(
            documentInstanceDto,
            parentDto,
            form.title(),
            form.content(),
            form.numbered(),
            form.hasPageBreakBefore(),
            displayOrder
        );

    assertThat(documentInstanceSectionService.createDocumentInstanceSection(documentInstanceDto, parentDto, form, displayOrder))
        .isEqualTo(documentInstanceSectionDto);
  }

  @Test
  void createDocumentInstanceSection_withParams_nullParent() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var title = "Test title";
    var content = "Test content";
    var sanitisedContent = "Sanitised test content";
    var numbered = true;
    var hasPageBreakBefore = false;
    var displayOrder = 1;

    var documentInstance = DocumentInstanceTestUtil.builder().build();

    var existingSibling1 = DocumentInstanceSectionTestUtil.builder()
        .withDisplayOrder(displayOrder)
        .build();
    var existingSibling2 = DocumentInstanceSectionTestUtil.builder()
        .withDisplayOrder(displayOrder + 1)
        .build();
    var existingSibling3 = DocumentInstanceSectionTestUtil.builder()
        .withDisplayOrder(displayOrder + 3)
        .build();

    when(documentInstanceService.getDocumentInstanceOrThrow(documentInstanceDto.id())).thenReturn(documentInstance);
    when(documentInstanceSectionRepository.findAllByDocumentInstanceIdAndParentIdAndDisplayOrderGreaterThanEqual(documentInstance.getId(), null, displayOrder))
        .thenReturn(List.of(existingSibling1, existingSibling2, existingSibling3));
    when(contentSanitisationService.getSanitisedContent(content)).thenReturn(sanitisedContent);

    var documentInstanceSectionDto = documentInstanceSectionService.createDocumentInstanceSection(
        documentInstanceDto,
        null,
        title,
        content,
        numbered,
        hasPageBreakBefore,
        displayOrder
    );

    assertThat(existingSibling1.getDisplayOrder()).isEqualTo(displayOrder + 1);
    assertThat(existingSibling2.getDisplayOrder()).isEqualTo(displayOrder + 2);

    verify(documentInstanceSectionRepository).saveAll(documentInstanceSectionListCaptor.capture());

    var savedDocumentInstanceSections = documentInstanceSectionListCaptor.getValue();

    assertThat(savedDocumentInstanceSections).hasSize(3);

    var newDocumentInstanceSection = savedDocumentInstanceSections.get(0);

    assertThat(newDocumentInstanceSection)
        .extracting(
            DocumentInstanceSection::getDocumentInstance,
            DocumentInstanceSection::getParent,
            DocumentInstanceSection::getTitle,
            DocumentInstanceSection::getContent,
            DocumentInstanceSection::isNumbered,
            DocumentInstanceSection::hasPageBreakBefore,
            DocumentInstanceSection::getDisplayOrder
        )
        .containsExactly(
            documentInstance,
            null,
            title,
            sanitisedContent,
            numbered,
            hasPageBreakBefore,
            displayOrder
        );

    assertThat(savedDocumentInstanceSections.get(1)).isEqualTo(existingSibling1);
    assertThat(savedDocumentInstanceSections.get(2)).isEqualTo(existingSibling2);

    assertThat(documentInstanceSectionDto).isEqualTo(DocumentInstanceSectionDto.from(newDocumentInstanceSection, List.of()));
  }

  @Test
  void createDocumentInstanceSection_withParams_nonNullParent() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var parentDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var parentDtoId = parentDto.id();

    var title = "Test title";
    var content = "Test content";
    var sanitisedContent = "Sanitised test content";
    var numbered = true;
    var hasPageBreakBefore = false;
    var displayOrder = 1;

    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var parent = DocumentInstanceSectionTestUtil.builder().build();

    var existingSibling1 = DocumentInstanceSectionTestUtil.builder()
        .withDisplayOrder(displayOrder)
        .build();
    var existingSibling2 = DocumentInstanceSectionTestUtil.builder()
        .withDisplayOrder(displayOrder + 1)
        .build();
    var existingSibling3 = DocumentInstanceSectionTestUtil.builder()
        .withDisplayOrder(displayOrder + 3)
        .build();

    when(documentInstanceService.getDocumentInstanceOrThrow(documentInstanceDto.id())).thenReturn(documentInstance);
    doReturn(parent).when(documentInstanceSectionService).getDocumentInstanceSectionOrThrow(parentDtoId);
    when(documentInstanceSectionRepository.findAllByDocumentInstanceIdAndParentIdAndDisplayOrderGreaterThanEqual(documentInstance.getId(), parentDtoId, displayOrder))
        .thenReturn(List.of(existingSibling1, existingSibling2, existingSibling3));
    when(contentSanitisationService.getSanitisedContent(content)).thenReturn(sanitisedContent);

    var documentInstanceSectionDto = documentInstanceSectionService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        title,
        content,
        numbered,
        hasPageBreakBefore,
        displayOrder
    );

    assertThat(existingSibling1.getDisplayOrder()).isEqualTo(displayOrder + 1);
    assertThat(existingSibling2.getDisplayOrder()).isEqualTo(displayOrder + 2);

    verify(documentInstanceSectionRepository).saveAll(documentInstanceSectionListCaptor.capture());

    var savedDocumentInstanceSections = documentInstanceSectionListCaptor.getValue();

    assertThat(savedDocumentInstanceSections).hasSize(3);

    var newDocumentInstanceSection = savedDocumentInstanceSections.get(0);

    assertThat(newDocumentInstanceSection)
        .extracting(
            DocumentInstanceSection::getDocumentInstance,
            DocumentInstanceSection::getParent,
            DocumentInstanceSection::getTitle,
            DocumentInstanceSection::getContent,
            DocumentInstanceSection::isNumbered,
            DocumentInstanceSection::hasPageBreakBefore,
            DocumentInstanceSection::getDisplayOrder
        )
        .containsExactly(
            documentInstance,
            parent,
            title,
            sanitisedContent,
            numbered,
            hasPageBreakBefore,
            displayOrder
        );

    assertThat(savedDocumentInstanceSections.get(1)).isEqualTo(existingSibling1);
    assertThat(savedDocumentInstanceSections.get(2)).isEqualTo(existingSibling2);

    assertThat(documentInstanceSectionDto).isEqualTo(DocumentInstanceSectionDto.from(newDocumentInstanceSection, List.of()));
  }

  @Test
  void editDocumentInstanceSection_withForm() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var form = DocumentInstanceSectionFormTestUtil.builder().build();

    doNothing().when(documentInstanceSectionService).editDocumentInstanceSection(any(), any(), any(), anyBoolean(), anyBoolean());

    documentInstanceSectionService.editDocumentInstanceSection(documentInstanceSectionDto, form);

    verify(documentInstanceSectionService).editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore()
    );
  }

  @Test
  void editDocumentInstanceSection_withParams() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var title = "Test edited title";
    var content = "Test edited content";
    var sanitisedContent = "Sanitised test edited content";
    var numbered = true;
    var hasPageBreakBefore = false;

    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();

    doReturn(documentInstanceSection)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionOrThrow(documentInstanceSectionDto.id());
    when(contentSanitisationService.getSanitisedContent(content)).thenReturn(sanitisedContent);

    documentInstanceSectionService.editDocumentInstanceSection(documentInstanceSectionDto, title, content, numbered, hasPageBreakBefore);

    assertThat(documentInstanceSection)
        .extracting(
            DocumentInstanceSection::getTitle,
            DocumentInstanceSection::getContent,
            DocumentInstanceSection::isNumbered,
            DocumentInstanceSection::hasPageBreakBefore
        )
        .containsExactly(
            title,
            sanitisedContent,
            numbered,
            hasPageBreakBefore
        );

    verify(documentInstanceSectionRepository).save(documentInstanceSection);
  }

  @Test
  void deleteDocumentInstanceSection() {
    var documentInstanceSectionDtoChild1Child1 = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceSectionDtoChild1 = DocumentInstanceSectionDtoTestUtil.builder()
        .withChildren(List.of(documentInstanceSectionDtoChild1Child1))
        .build();
    var documentInstanceSectionDtoChild2 = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withChildren(List.of(documentInstanceSectionDtoChild1, documentInstanceSectionDtoChild2))
        .build();

    documentInstanceSectionService.deleteDocumentInstanceSection(documentInstanceSectionDto);

    verify(documentInstanceSectionRepository).deleteAllById(
        List.of(
            documentInstanceSectionDto.id(),
            documentInstanceSectionDtoChild1.id(),
            documentInstanceSectionDtoChild1Child1.id(),
            documentInstanceSectionDtoChild2.id()
        )
    );
  }

  @Test
  void getDocumentInstanceSectionDtoOrThrow() {
    var documentInstanceSectionId = UUID.randomUUID();

    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();

    var allDocumentInstanceSections = List.of(documentInstanceSection);

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    doReturn(documentInstanceSection)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionOrThrow(documentInstanceSectionId);
    when(documentInstanceSectionRepository.findAllByDocumentInstanceId(
        documentInstanceSection.getDocumentInstance().getId())
    ).thenReturn(allDocumentInstanceSections);
    doReturn(documentInstanceSectionDto)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionDto(documentInstanceSection, allDocumentInstanceSections);

    assertThat(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId))
        .isEqualTo(documentInstanceSectionDto);
  }

  @Test
  void getDocumentInstanceSectionDto() {
    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();

    var documentInstanceSectionChild1 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection)
        .build();
    var documentInstanceSectionChild1Child1 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSectionChild1)
        .build();
    var documentInstanceSectionChild2 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection)
        .build();

    var allDocumentInstanceSections = List.of(
        documentInstanceSection,
        documentInstanceSectionChild1,
        documentInstanceSectionChild1Child1,
        documentInstanceSectionChild2
    );

    assertThat(
        documentInstanceSectionService.getDocumentInstanceSectionDto(
            documentInstanceSection,
            allDocumentInstanceSections
        )
    ).isEqualTo(
        DocumentInstanceSectionDto.from(
            documentInstanceSection,
            List.of(
                DocumentInstanceSectionDto.from(
                    documentInstanceSectionChild1,
                    List.of(
                        DocumentInstanceSectionDto.from(
                            documentInstanceSectionChild1Child1,
                            List.of()
                        )
                    )
                ),
                DocumentInstanceSectionDto.from(
                    documentInstanceSectionChild2,
                    List.of()
                )
            )
        )
    );
  }

  @Test
  void getDocumentInstanceSectionOrThrow_documentInstanceSectionDoesNotExist() {
    var documentInstanceSectionId = UUID.randomUUID();

    when(documentInstanceSectionRepository.findById(documentInstanceSectionId)).thenReturn(Optional.empty());

    assertThatThrownBy(
        () -> documentInstanceSectionService.getDocumentInstanceSectionOrThrow(documentInstanceSectionId)
    ).isInstanceOf(DocumentInstanceSectionNotFoundException.class);
  }

  @Test
  void getDocumentInstanceSectionOrThrow_documentInstanceSectionExists() {
    var documentInstanceSectionId = UUID.randomUUID();

    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();

    when(documentInstanceSectionRepository.findById(documentInstanceSectionId))
        .thenReturn(Optional.of(documentInstanceSection));

    assertThat(documentInstanceSectionService.getDocumentInstanceSectionOrThrow(documentInstanceSectionId))
        .isEqualTo(documentInstanceSection);
  }

  @Test
  void getTopLevelDocumentInstanceSectionDtos() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentInstanceSection1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSection2 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSection3 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection2)
        .build();
    var documentInstanceSection4 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection3)
        .build();
    var documentInstanceSection5 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection2)
        .build();

    var allDocumentInstanceSections = List.of(
        documentInstanceSection1,
        documentInstanceSection2,
        documentInstanceSection3,
        documentInstanceSection4,
        documentInstanceSection5
    );

    var documentInstanceSectionDto1 = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceSectionDto2 = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(documentInstanceSectionRepository.findAllByDocumentInstanceId(documentInstanceDto.id()))
        .thenReturn(allDocumentInstanceSections);
    doReturn(documentInstanceSectionDto1)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionDto(documentInstanceSection1, allDocumentInstanceSections);
    doReturn(documentInstanceSectionDto2)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionDto(documentInstanceSection2, allDocumentInstanceSections);

    assertThat(documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto))
        .containsExactly(
            documentInstanceSectionDto1,
            documentInstanceSectionDto2
        );
  }

  @Test
  void getTopLevelDocumentInstanceSectionDtosForDocumentInstanceDtos() {
    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder()
        .withTitle("title 1").build();
    var documentInstanceSection1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSection2 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection1)
        .build();

    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder()
        .withTitle("title 2").build();
    var documentInstanceSection3 = DocumentInstanceSectionTestUtil.builder().build();

    var topLevelDocumentInstanceSectionDto1 = DocumentInstanceSectionDtoTestUtil.builder()
        .withDocumentInstanceDto(documentInstanceDto1)
        .build();
    var topLevelDocumentInstanceSectionDto3 = DocumentInstanceSectionDtoTestUtil.builder()
        .withDocumentInstanceDto(documentInstanceDto2)
        .build();

    var allDocumentInstanceSections = List.of(
        documentInstanceSection1,
        documentInstanceSection2,
        documentInstanceSection3
    );

    var documentInstanceIds = List.of(documentInstanceDto1.id(), documentInstanceDto2.id());

    when(documentInstanceSectionRepository.findAllByDocumentInstanceIdIn(documentInstanceIds))
        .thenReturn(allDocumentInstanceSections);

    doReturn(topLevelDocumentInstanceSectionDto1)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionDto(documentInstanceSection1, allDocumentInstanceSections);
    doReturn(topLevelDocumentInstanceSectionDto3)
        .when(documentInstanceSectionService)
        .getDocumentInstanceSectionDto(documentInstanceSection3, allDocumentInstanceSections);

    assertThat(documentInstanceSectionService
        .getTopLevelDocumentInstanceSectionDtosForDocumentInstanceDtos(
            List.of(documentInstanceDto1, documentInstanceDto2)))
        .isEqualTo(
            Map.of(
                documentInstanceDto1, List.of(topLevelDocumentInstanceSectionDto1),
                documentInstanceDto2, List.of(topLevelDocumentInstanceSectionDto3)
            ));
  }
}
