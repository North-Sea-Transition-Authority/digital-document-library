package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
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

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  @Spy
  private DocumentTemplateSectionViewService documentTemplateSectionViewService;

  private final DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter = new TestDocumentMailMergeFieldFormatter();

  @Test
  void getDocumentTemplateSectionsSummaryView() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    Function<DocumentTemplateSectionDto, DocumentTemplateSectionUrls> urlsFunction = documentTemplateSectionDto -> null;

    var topLevelDocumentSectionDtos = List.of(DocumentTemplateSectionDtoTestUtil.builder().build());

    var topLevelDocumentSectionSummaryViews = List.of(
        DocumentTemplateSectionSummaryViewTestUtil.newBuilder().build(),
        DocumentTemplateSectionSummaryViewTestUtil.newBuilder().build()
    );

    when(documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto))
        .thenReturn(topLevelDocumentSectionDtos);

    doReturn(topLevelDocumentSectionSummaryViews)
        .when(documentTemplateSectionViewService)
        .getSiblingDocumentTemplateSectionSummaryViews(
            isNull(),
            eq(topLevelDocumentSectionDtos),
            eq(urlsFunction),
            any(DocumentSectionMailMergeFieldResolver.class));

    assertThat(
        documentTemplateSectionViewService.getDocumentTemplateSectionsSummaryView(
            documentTemplateDto,
            urlsFunction,
            documentMailMergeFieldFormatter
        )
    ).isEqualTo(DocumentTemplateSectionsSummaryView.from(topLevelDocumentSectionSummaryViews));
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

    var documentSectionMailMergeResolver = mock(DocumentSectionMailMergeFieldResolver.class);

    var resolvedSiblingDocumentSectionDto1 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 1").build();
    var resolvedSiblingDocumentSectionDto2 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 2").build();
    var resolvedSiblingDocumentSectionDto2Child1 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 3").build();
    var resolvedSiblingDocumentSectionDto2Child1Child1 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 4").build();
    var resolvedSiblingDocumentSectionDto2Child2 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 5").build();
    var resolvedSiblingDocumentSectionDto2Child3 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 6").build();

    when(documentSectionMailMergeResolver.resolve(siblingDocumentTemplateSectionDto1)).thenReturn(resolvedSiblingDocumentSectionDto1);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentTemplateSectionDto2)).thenReturn(resolvedSiblingDocumentSectionDto2);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentTemplateSectionDto2Child1)).thenReturn(resolvedSiblingDocumentSectionDto2Child1);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentTemplateSectionDto2Child1Child1)).thenReturn(resolvedSiblingDocumentSectionDto2Child1Child1);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentTemplateSectionDto2Child2)).thenReturn(resolvedSiblingDocumentSectionDto2Child2);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentTemplateSectionDto2Child3)).thenReturn(resolvedSiblingDocumentSectionDto2Child3);

    assertThat(
        documentTemplateSectionViewService.getSiblingDocumentTemplateSectionSummaryViews(
            parentSectionNumberString,
            siblingDocumentTemplateSectionDtos,
            urlsFunction,
            documentSectionMailMergeResolver)
    ).containsExactly(
        DocumentTemplateSectionSummaryView.from(
            null,
            null,
            siblingDocumentTemplateSectionDto1,
            resolvedSiblingDocumentSectionDto1,
            siblingDocumentTemplateSectionDto1Urls,
            List.of()),
        DocumentTemplateSectionSummaryView.from(
            "1.1",
            condition1.getTitle(),
            siblingDocumentTemplateSectionDto2,
            resolvedSiblingDocumentSectionDto2,
            siblingDocumentTemplateSectionDto2Urls,
            List.of(
                DocumentTemplateSectionSummaryView.from(
                    "1.1.1",
                    condition2.getTitle(),
                    siblingDocumentTemplateSectionDto2Child1,
                    resolvedSiblingDocumentSectionDto2Child1,
                    siblingDocumentTemplateSectionDto2Child1Urls,
                    List.of(
                        DocumentTemplateSectionSummaryView.from(
                            "1.1.1.1",
                            null,
                            siblingDocumentTemplateSectionDto2Child1Child1,
                            resolvedSiblingDocumentSectionDto2Child1Child1,
                            siblingDocumentTemplateSectionDto2Child1Child1Urls,
                            List.of())
                    )),
                DocumentTemplateSectionSummaryView.from(
                    null,
                    null,
                    siblingDocumentTemplateSectionDto2Child2,
                    resolvedSiblingDocumentSectionDto2Child2,
                    siblingDocumentTemplateSectionDto2Child2Urls,
                    List.of()),
                DocumentTemplateSectionSummaryView.from(
                    "1.1.2",
                    null,
                    siblingDocumentTemplateSectionDto2Child3,
                    resolvedSiblingDocumentSectionDto2Child3,
                    siblingDocumentTemplateSectionDto2Child3Urls,
                    List.of())
            ))
    );
  }
}
