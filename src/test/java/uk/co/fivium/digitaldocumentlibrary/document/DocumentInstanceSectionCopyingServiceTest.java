package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceSectionCopyingServiceTest {

  @Mock
  private DocumentInstanceSectionRepository documentInstanceSectionRepository;

  @Mock
  private DocumentTemplateSectionService documentTemplateSectionService;

  @Mock
  private DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @InjectMocks
  @Spy
  private DocumentInstanceSectionCopyingService documentInstanceSectionCopyingService;

  @Test
  void copyDocumentTemplateSectionsToDocumentInstance() {
    var documentInstance = DocumentInstanceTestUtil.builder().build();

    var documentTemplateSection1 = DocumentTemplateSectionTestUtil.builder().build();
    var documentTemplateSection2 = DocumentTemplateSectionTestUtil.builder().build();
    var documentTemplateSection2Child1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection2)
        .build();

    var documentTemplateSections = List.of(
        documentTemplateSection1,
        documentTemplateSection2,
        documentTemplateSection2Child1
    );

    var documentInstanceSection1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSection2 = DocumentInstanceSectionTestUtil.builder().build();

    when(documentTemplateSectionService.getDocumentTemplateSections(documentInstance.getDocumentTemplate()))
        .thenReturn(documentTemplateSections);

    doReturn(List.of(documentInstanceSection1))
        .when(documentInstanceSectionCopyingService)
        .tryCopyDocumentTemplateSectionAndChildren(
            documentTemplateSection1,
            documentInstance,
            null,
            documentTemplateSections
        );
    doReturn(List.of(documentInstanceSection2))
        .when(documentInstanceSectionCopyingService)
        .tryCopyDocumentTemplateSectionAndChildren(
            documentTemplateSection2,
            documentInstance,
            null,
            documentTemplateSections
        );

    documentInstanceSectionCopyingService.copyDocumentTemplateSectionsToDocumentInstance(documentInstance);

    verify(documentInstanceSectionRepository).saveAll(List.of(documentInstanceSection1, documentInstanceSection2));
  }

  @Test
  void copyDocumentInstanceSectionsToDocumentInstance() {
    var newDocumentInstance = DocumentInstanceTestUtil.builder().build();

    var oldDocumentInstanceDto = DocumentInstanceDto.from(DocumentInstanceTestUtil.builder().build());
    var documentInstanceSection1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSection2 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSection2Child1 = DocumentInstanceSectionTestUtil.builder()
        .withParent(documentInstanceSection2)
        .build();
    var documentInstanceSections = List.of(documentInstanceSection1, documentInstanceSection2, documentInstanceSection2Child1);

    when(documentInstanceSectionRepository.findAllByDocumentInstanceId(oldDocumentInstanceDto.id()))
        .thenReturn(documentInstanceSections);

    doReturn(List.of(documentInstanceSection1))
        .when(documentInstanceSectionCopyingService)
        .tryCopyDocumentInstanceSectionAndChildren(
            documentInstanceSection1,
            newDocumentInstance,
            null,
            documentInstanceSections
        );
    doReturn(List.of(documentInstanceSection2, documentInstanceSection2Child1))
        .when(documentInstanceSectionCopyingService)
        .tryCopyDocumentInstanceSectionAndChildren(
            documentInstanceSection2,
            newDocumentInstance,
            null,
            documentInstanceSections
        );

    documentInstanceSectionCopyingService.copyDocumentInstanceSectionsToDocumentInstance(newDocumentInstance, oldDocumentInstanceDto);

    verify(documentInstanceSectionRepository)
        .saveAll(List.of(documentInstanceSection1, documentInstanceSection2, documentInstanceSection2Child1));
  }

  @Test
  void tryCopyDocumentTemplateSectionAndChildren_conditionMnemonicNotNullAndConditionEvaluatesToFalse() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder()
        .withConditionMnemonic(conditionMnemonic)
        .build();
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var parent = DocumentInstanceSectionTestUtil.builder().build();

    var documentTemplateSectionChild1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .build();
    var documentTemplateSectionChild1Child1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSectionChild1)
        .build();
    var documentTemplateSectionChild2 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .build();

    var allDocumentTemplateSections = List.of(
        documentTemplateSection,
        documentTemplateSectionChild1,
        documentTemplateSectionChild1Child1,
        documentTemplateSectionChild2
    );

    var condition = mock(DocumentTemplateSectionCondition.class);

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            DocumentTemplateDto.from(documentInstance.getDocumentTemplate()),
            conditionMnemonic
        )
    ).thenReturn(condition);
    when(condition.evaluate(DocumentInstanceDto.from(documentInstance))).thenReturn(false);

    assertThat(documentInstanceSectionCopyingService.tryCopyDocumentTemplateSectionAndChildren(
        documentTemplateSection,
        documentInstance,
        parent,
        allDocumentTemplateSections
    )).isEmpty();
  }

  @Test
  void tryCopyDocumentTemplateSectionAndChildren_conditionMnemonicNotNullAndConditionEvaluatesToTrue() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder()
        .withConditionMnemonic(conditionMnemonic)
        .build();
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var parent = DocumentInstanceSectionTestUtil.builder().build();

    var documentTemplateSectionChild1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .build();
    var documentTemplateSectionChild1Child1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSectionChild1)
        .build();
    var documentTemplateSectionChild2 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .build();

    var allDocumentTemplateSections = List.of(
        documentTemplateSection,
        documentTemplateSectionChild1,
        documentTemplateSectionChild1Child1,
        documentTemplateSectionChild2
    );

    var condition = mock(DocumentTemplateSectionCondition.class);

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            DocumentTemplateDto.from(documentInstance.getDocumentTemplate()),
            conditionMnemonic
        )
    ).thenReturn(condition);
    when(condition.evaluate(DocumentInstanceDto.from(documentInstance))).thenReturn(true);

    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSectionChild1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSectionChild1Child1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSectionChild2 = DocumentInstanceSectionTestUtil.builder().build();

    doReturn(documentInstanceSection)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSection, documentInstance, parent);
    doReturn(documentInstanceSectionChild1)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSectionChild1, documentInstance, documentInstanceSection);
    doReturn(documentInstanceSectionChild1Child1)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSectionChild1Child1, documentInstance, documentInstanceSectionChild1);
    doReturn(documentInstanceSectionChild2)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSectionChild2, documentInstance, documentInstanceSection);

    assertThat(documentInstanceSectionCopyingService.tryCopyDocumentTemplateSectionAndChildren(
        documentTemplateSection,
        documentInstance,
        parent,
        allDocumentTemplateSections
    )).containsExactly(
        documentInstanceSection,
        documentInstanceSectionChild1,
        documentInstanceSectionChild1Child1,
        documentInstanceSectionChild2
    );
  }

  @Test
  void tryCopyDocumentTemplateSectionAndChildren_conditionMnemonicNull() {
    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder()
        .withConditionMnemonic(null)
        .build();
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var parent = DocumentInstanceSectionTestUtil.builder().build();

    var documentTemplateSectionChild1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .build();
    var documentTemplateSectionChild1Child1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSectionChild1)
        .build();
    var documentTemplateSectionChild2 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .build();

    var allDocumentTemplateSections = List.of(
        documentTemplateSection,
        documentTemplateSectionChild1,
        documentTemplateSectionChild1Child1,
        documentTemplateSectionChild2
    );

    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSectionChild1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSectionChild1Child1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSectionChild2 = DocumentInstanceSectionTestUtil.builder().build();

    doReturn(documentInstanceSection)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSection, documentInstance, parent);
    doReturn(documentInstanceSectionChild1)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSectionChild1, documentInstance, documentInstanceSection);
    doReturn(documentInstanceSectionChild1Child1)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSectionChild1Child1, documentInstance, documentInstanceSectionChild1);
    doReturn(documentInstanceSectionChild2)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSectionChild2, documentInstance, documentInstanceSection);

    assertThat(documentInstanceSectionCopyingService.tryCopyDocumentTemplateSectionAndChildren(
        documentTemplateSection,
        documentInstance,
        parent,
        allDocumentTemplateSections
    )).containsExactly(
        documentInstanceSection,
        documentInstanceSectionChild1,
        documentInstanceSectionChild1Child1,
        documentInstanceSectionChild2
    );
  }

  @Test
  void tryCopyDocumentTemplateSectionAndChildren_childSectionsHaveConditions() {
    var trueConditionMnemonic = "TRUE_CONDITION_MNEMONIC";
    var falseConditionMnemonic = "FALSE_CONDITION_MNEMONIC";

    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder()
        .withConditionMnemonic(null)
        .build();
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var parent = DocumentInstanceSectionTestUtil.builder().build();

    var documentTemplateSectionChild1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .withConditionMnemonic(null)
        .build();
    var documentTemplateSectionChild1Child1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSectionChild1)
        .withConditionMnemonic(falseConditionMnemonic)
        .build();
    var documentTemplateSectionChild1Child1Child1 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSectionChild1Child1)
        .withConditionMnemonic(null)
        .build();
    var documentTemplateSectionChild2 = DocumentTemplateSectionTestUtil.builder()
        .withParent(documentTemplateSection)
        .withConditionMnemonic(trueConditionMnemonic)
        .build();

    var allDocumentTemplateSections = List.of(
        documentTemplateSection,
        documentTemplateSectionChild1,
        documentTemplateSectionChild1Child1,
        documentTemplateSectionChild1Child1Child1,
        documentTemplateSectionChild2
    );

    var documentTemplateDto = DocumentTemplateDto.from(documentInstance.getDocumentTemplate());

    var trueCondition = mock(DocumentTemplateSectionCondition.class);
    var falseCondition = mock(DocumentTemplateSectionCondition.class);

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateDto,
            trueConditionMnemonic
        )
    ).thenReturn(trueCondition);
    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateDto,
            falseConditionMnemonic
        )
    ).thenReturn(falseCondition);
    when(trueCondition.evaluate(DocumentInstanceDto.from(documentInstance))).thenReturn(true);
    when(falseCondition.evaluate(DocumentInstanceDto.from(documentInstance))).thenReturn(false);

    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSectionChild1 = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstanceSectionChild2 = DocumentInstanceSectionTestUtil.builder().build();

    doReturn(documentInstanceSection)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSection, documentInstance, parent);
    doReturn(documentInstanceSectionChild1)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSectionChild1, documentInstance, documentInstanceSection);
    doReturn(documentInstanceSectionChild2)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSection(documentTemplateSectionChild2, documentInstance, documentInstanceSection);

    assertThat(documentInstanceSectionCopyingService.tryCopyDocumentTemplateSectionAndChildren(
        documentTemplateSection,
        documentInstance,
        parent,
        allDocumentTemplateSections
    )).containsExactly(
        documentInstanceSection,
        documentInstanceSectionChild1,
        documentInstanceSectionChild2
    );
  }

  @Test
  void tryCopyDocumentInstanceSectionAndChildren() {
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder()
        .build();

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
        documentInstanceSection, documentInstanceSectionChild1, documentInstanceSectionChild1Child1, documentInstanceSectionChild2
    );

    var newDocumentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();
    var newDocumentInstanceSectionChild1 = DocumentInstanceSectionTestUtil.builder().build();
    var newDocumentInstanceSectionChild1Child1 = DocumentInstanceSectionTestUtil.builder().build();
    var newDocumentInstanceSectionChild2 = DocumentInstanceSectionTestUtil.builder().build();

    doReturn(newDocumentInstanceSection)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSectionFromInstance(documentInstanceSection, documentInstance, null);
    doReturn(newDocumentInstanceSectionChild1)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSectionFromInstance(documentInstanceSectionChild1, documentInstance, newDocumentInstanceSection);
    doReturn(newDocumentInstanceSectionChild1Child1)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSectionFromInstance(documentInstanceSectionChild1Child1, documentInstance, newDocumentInstanceSectionChild1);
    doReturn(newDocumentInstanceSectionChild2)
        .when(documentInstanceSectionCopyingService)
        .newDocumentInstanceSectionFromInstance(documentInstanceSectionChild2, documentInstance, newDocumentInstanceSection);

    assertThat(documentInstanceSectionCopyingService.tryCopyDocumentInstanceSectionAndChildren(
        documentInstanceSection,
        documentInstance,
        null,
        allDocumentInstanceSections
    )).containsExactly(
        newDocumentInstanceSection,
        newDocumentInstanceSectionChild1,
        newDocumentInstanceSectionChild1Child1,
        newDocumentInstanceSectionChild2
    );
  }

  @Test
  void newDocumentInstanceSection() {
    var documentTemplateSection = DocumentTemplateSectionTestUtil.builder().build();
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var parent = DocumentInstanceSectionTestUtil.builder().build();

    assertThat(documentInstanceSectionCopyingService.newDocumentInstanceSection(
        documentTemplateSection,
        documentInstance,
        parent
    )).extracting(
        DocumentInstanceSection::getDocumentInstance,
        DocumentInstanceSection::getCreatedFromDocumentTemplateSection,
        DocumentInstanceSection::getParent,
        DocumentInstanceSection::getTitle,
        DocumentInstanceSection::getContent,
        DocumentInstanceSection::isNumbered,
        DocumentInstanceSection::hasPageBreakBefore,
        DocumentInstanceSection::getDisplayOrder
    ).containsExactly(
        documentInstance,
        documentTemplateSection,
        parent,
        documentTemplateSection.getTitle(),
        documentTemplateSection.getContent(),
        documentTemplateSection.isNumbered(),
        documentTemplateSection.hasPageBreakBefore(),
        documentTemplateSection.getDisplayOrder()
    );
  }

  @Test
  void newDocumentInstanceSectionFromInstance() {
    var documentInstanceSection = DocumentInstanceSectionTestUtil.builder().build();
    var documentInstance = DocumentInstanceTestUtil.builder().build();
    var parent = DocumentInstanceSectionTestUtil.builder().build();

    assertThat(documentInstanceSectionCopyingService.newDocumentInstanceSectionFromInstance(
        documentInstanceSection,
        documentInstance,
        parent
    )).extracting(
        DocumentInstanceSection::getDocumentInstance,
        DocumentInstanceSection::getCreatedFromDocumentTemplateSection,
        DocumentInstanceSection::getParent,
        DocumentInstanceSection::getTitle,
        DocumentInstanceSection::getContent,
        DocumentInstanceSection::isNumbered,
        DocumentInstanceSection::hasPageBreakBefore,
        DocumentInstanceSection::getDisplayOrder
    ).containsExactly(
        documentInstance,
        documentInstanceSection.getCreatedFromDocumentTemplateSection(),
        parent,
        documentInstanceSection.getTitle(),
        documentInstanceSection.getContent(),
        documentInstanceSection.isNumbered(),
        documentInstanceSection.hasPageBreakBefore(),
        documentInstanceSection.getDisplayOrder()
    );
  }

  @Test
  void reloadDocumentInstanceSectionsFromDocumentTemplate() {
    var documentInstance = DocumentInstanceTestUtil.builder().build();

    doNothing()
        .when(documentInstanceSectionCopyingService)
        .copyDocumentTemplateSectionsToDocumentInstance(any());

    documentInstanceSectionCopyingService.reloadDocumentInstanceSectionsFromDocumentTemplate(documentInstance);

    verify(documentInstanceSectionRepository).deleteAllByDocumentInstanceId(documentInstance.getId());

    verify(documentInstanceSectionCopyingService)
        .copyDocumentTemplateSectionsToDocumentInstance(documentInstance);
  }
}
