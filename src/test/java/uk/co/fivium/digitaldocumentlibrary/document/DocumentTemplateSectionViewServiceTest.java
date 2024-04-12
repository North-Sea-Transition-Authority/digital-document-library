package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateSectionViewServiceTest {

  @Mock
  private DocumentTemplateSectionService documentTemplateSectionService;

  @Mock
  private DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @InjectMocks
  @Spy
  private DocumentTemplateSectionViewService documentTemplateSectionViewService;

  @Test
  void getTopLevelDocumentTemplateSectionSummaryViews() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    Function<DocumentTemplateSectionDto, DocumentTemplateSectionUrls> urlsFunction = documentTemplateSectionDto -> null;

    var topLevelDocumentTemplateSectionDtos = List.of(DocumentTemplateSectionDtoTestUtil.builder().build());
    var topLevelDocumentTemplateSectionSummaryViews = List.of(mock(DocumentTemplateSectionSummaryView.class));

    when(documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto))
        .thenReturn(topLevelDocumentTemplateSectionDtos);

    doReturn(topLevelDocumentTemplateSectionSummaryViews)
        .when(documentTemplateSectionViewService)
        .getSiblingDocumentTemplateSectionSummaryViews(
            null,
            topLevelDocumentTemplateSectionDtos,
            urlsFunction
        );

    assertThat(
        documentTemplateSectionViewService.getTopLevelDocumentTemplateSectionSummaryViews(documentTemplateDto, urlsFunction)
    ).isEqualTo(topLevelDocumentTemplateSectionSummaryViews);
  }

  @Test
  void getSiblingDocumentTemplateSectionSummaryViews() {
    var parentSectionNumberString = "1";

    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var conditionMnemonic1 = "TEST_CONDITION_MNEMONIC_1";
    var conditionMnemonic2 = "TEST_CONDITION_MNEMONIC_2";

    var condition1 = DocumentTemplateSectionConditionTestUtil.builder().withTitle("Test title 1").build();
    var condition2 = DocumentTemplateSectionConditionTestUtil.builder().withTitle("Test title 2").build();

    var siblingDocumentTemplateSectionDto1 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withNumbered(false)
            .withDisplayOrder(1)
            .build();
    var siblingDocumentTemplateSectionDto1Urls =
        DocumentTemplateSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentTemplateSectionDto1").build();

    var siblingDocumentTemplateSectionDto2Child1Child1 = DocumentTemplateSectionDtoTestUtil.builder().build();
    var siblingDocumentTemplateSectionDto2Child1Child1Urls =
        DocumentTemplateSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentTemplateSectionDto2Child1Child1").build();

    var siblingDocumentTemplateSectionDto2Child1 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDocumentTemplateDto(documentTemplateDto)
            .withConditionMnemonic(conditionMnemonic2)
            .withDisplayOrder(1)
            .withChildren(List.of(siblingDocumentTemplateSectionDto2Child1Child1))
            .build();
    var siblingDocumentTemplateSectionDto2Child1Urls =
        DocumentTemplateSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentTemplateSectionDto2Child1").build();

    var siblingDocumentTemplateSectionDto2Child2 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withNumbered(false)
            .withDisplayOrder(2)
            .build();
    var siblingDocumentTemplateSectionDto2Child2Urls =
        DocumentTemplateSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentTemplateSectionDto2Child2").build();

    var siblingDocumentTemplateSectionDto2Child3 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDisplayOrder(3)
            .build();
    var siblingDocumentTemplateSectionDto2Child3Urls =
        DocumentTemplateSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentTemplateSectionDto2Child3").build();

    var siblingDocumentTemplateSectionDto2 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDocumentTemplateDto(documentTemplateDto)
            .withConditionMnemonic(conditionMnemonic1)
            .withDisplayOrder(2)
            .withChildren(
                List.of(
                    siblingDocumentTemplateSectionDto2Child2,
                    siblingDocumentTemplateSectionDto2Child3,
                    siblingDocumentTemplateSectionDto2Child1
                )
            )
            .build();
    var siblingDocumentTemplateSectionDto2Urls =
        DocumentTemplateSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentTemplateSectionDto2").build();

    var siblingDocumentTemplateSectionDtos =
        List.of(siblingDocumentTemplateSectionDto1, siblingDocumentTemplateSectionDto2);

    var urlsByDocumentTemplateSectionDto = Map.of(
        siblingDocumentTemplateSectionDto1, siblingDocumentTemplateSectionDto1Urls,
        siblingDocumentTemplateSectionDto2, siblingDocumentTemplateSectionDto2Urls,
        siblingDocumentTemplateSectionDto2Child1, siblingDocumentTemplateSectionDto2Child1Urls,
        siblingDocumentTemplateSectionDto2Child1Child1, siblingDocumentTemplateSectionDto2Child1Child1Urls,
        siblingDocumentTemplateSectionDto2Child2, siblingDocumentTemplateSectionDto2Child2Urls,
        siblingDocumentTemplateSectionDto2Child3, siblingDocumentTemplateSectionDto2Child3Urls
    );
    Function<DocumentTemplateSectionDto, DocumentTemplateSectionUrls> urlsFunction = urlsByDocumentTemplateSectionDto::get;

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateDto,
            conditionMnemonic1
        )
    ).thenReturn(condition1);
    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateDto,
            conditionMnemonic2
        )
    ).thenReturn(condition2);

    assertThat(
        documentTemplateSectionViewService.getSiblingDocumentTemplateSectionSummaryViews(
            parentSectionNumberString,
            siblingDocumentTemplateSectionDtos,
            urlsFunction
        )
    ).containsExactly(
        DocumentTemplateSectionSummaryView.from(
            null,
            null,
            siblingDocumentTemplateSectionDto1,
            siblingDocumentTemplateSectionDto1Urls,
            List.of()
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.1",
            condition1.getTitle(),
            siblingDocumentTemplateSectionDto2,
            siblingDocumentTemplateSectionDto2Urls,
            List.of(
                DocumentTemplateSectionSummaryView.from(
                    "1.1.1",
                    condition2.getTitle(),
                    siblingDocumentTemplateSectionDto2Child1,
                    siblingDocumentTemplateSectionDto2Child1Urls,
                    List.of(
                        DocumentTemplateSectionSummaryView.from(
                            "1.1.1.1",
                            null,
                            siblingDocumentTemplateSectionDto2Child1Child1,
                            siblingDocumentTemplateSectionDto2Child1Child1Urls,
                            List.of()
                        )
                    )
                ),
                DocumentTemplateSectionSummaryView.from(
                    null,
                    null,
                    siblingDocumentTemplateSectionDto2Child2,
                    siblingDocumentTemplateSectionDto2Child2Urls,
                    List.of()
                ),
                DocumentTemplateSectionSummaryView.from(
                    "1.1.2",
                    null,
                    siblingDocumentTemplateSectionDto2Child3,
                    siblingDocumentTemplateSectionDto2Child3Urls,
                    List.of()
                )
            )
        )
    );
  }
}
