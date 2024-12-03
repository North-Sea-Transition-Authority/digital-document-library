package uk.co.fivium.digitaldocumentlibrary.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceSectionViewServiceTest {

  @Mock
  private DocumentInstanceSectionService documentInstanceSectionService;

  @InjectMocks
  @Spy
  private DocumentInstanceSectionViewService documentInstanceSectionViewService;

  private final DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter = new TestDocumentMailMergeFieldFormatter();

  @Test
  void getDocumentInstanceSectionsSummaryView() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls> urlsFunction = documentInstanceSectionDto -> null;

    var topLevelDocumentInstanceSectionDtos = List.of(DocumentInstanceSectionDtoTestUtil.builder().build());

    var topLevelDocumentInstanceSectionSummaryViews = List.of(
        DocumentInstanceSectionSummaryViewTestUtil.newBuilder().build(),
        DocumentInstanceSectionSummaryViewTestUtil.newBuilder().build()
    );

    when(documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto))
        .thenReturn(topLevelDocumentInstanceSectionDtos);

    doReturn(topLevelDocumentInstanceSectionSummaryViews)
        .when(documentInstanceSectionViewService)
        .getSiblingDocumentInstanceSectionSummaryViews(
            isNull(),
            eq(topLevelDocumentInstanceSectionDtos),
            eq(urlsFunction),
            any(DocumentSectionMailMergeFieldResolver.class)
        );

    assertThat(
        documentInstanceSectionViewService.getDocumentInstanceSectionsSummaryView(
            documentInstanceDto,
            urlsFunction,
            documentMailMergeFieldFormatter
        )
    ).isEqualTo(DocumentInstanceSectionsSummaryView.from(topLevelDocumentInstanceSectionSummaryViews));
  }

  @Test
  void getSiblingDocumentInstanceSectionSummaryViews() {
    var parentSectionNumberString = "1";

    var siblingDocumentInstanceSectionDto1 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withNumbered(false)
            .withDisplayOrder(1)
            .build();
    var siblingDocumentInstanceSectionDto1Urls =
        DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentInstanceSectionDto1").build();

    var siblingDocumentInstanceSectionDto2Child1Child1 = DocumentInstanceSectionDtoTestUtil.builder().build();
    var siblingDocumentInstanceSectionDto2Child1Child1Urls =
        DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentInstanceSectionDto2Child1Child1").build();

    var siblingDocumentInstanceSectionDto2Child1 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(1)
            .withChildren(List.of(siblingDocumentInstanceSectionDto2Child1Child1))
            .build();
    var siblingDocumentInstanceSectionDto2Child1Urls =
        DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentInstanceSectionDto2Child1").build();

    var siblingDocumentInstanceSectionDto2Child2 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withNumbered(false)
            .withDisplayOrder(2)
            .build();
    var siblingDocumentInstanceSectionDto2Child2Urls =
        DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentInstanceSectionDto2Child2").build();

    var siblingDocumentInstanceSectionDto2Child3 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(3)
            .build();
    var siblingDocumentInstanceSectionDto2Child3Urls =
        DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentInstanceSectionDto2Child3").build();

    var siblingDocumentInstanceSectionDto2 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(2)
            .withChildren(
                List.of(
                    siblingDocumentInstanceSectionDto2Child2,
                    siblingDocumentInstanceSectionDto2Child3,
                    siblingDocumentInstanceSectionDto2Child1
                )
            )
            .build();
    var siblingDocumentInstanceSectionDto2Urls =
        DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-siblingDocumentInstanceSectionDto2").build();

    var siblingDocumentInstanceSectionDtos =
        List.of(siblingDocumentInstanceSectionDto1, siblingDocumentInstanceSectionDto2);

    var resolvedSiblingDocumentInstanceSectionDto1 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 1").build();
    var resolvedSiblingDocumentInstanceSectionDto2 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 2").build();
    var resolvedSiblingDocumentInstanceSectionDto2Child1 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 3").build();
    var resolvedSiblingDocumentInstanceSectionDto2Child1Child1 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 4").build();
    var resolvedSiblingDocumentInstanceSectionDto2Child2 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 5").build();
    var resolvedSiblingDocumentInstanceSectionDto2Child3 = ResolvedDocumentSectionTestUtil.newBuilder().withResolvedContent("Test content 6").build();

    var urlsByDocumentInstanceSectionDto = Map.of(
        siblingDocumentInstanceSectionDto1, siblingDocumentInstanceSectionDto1Urls,
        siblingDocumentInstanceSectionDto2, siblingDocumentInstanceSectionDto2Urls,
        siblingDocumentInstanceSectionDto2Child1, siblingDocumentInstanceSectionDto2Child1Urls,
        siblingDocumentInstanceSectionDto2Child1Child1, siblingDocumentInstanceSectionDto2Child1Child1Urls,
        siblingDocumentInstanceSectionDto2Child2, siblingDocumentInstanceSectionDto2Child2Urls,
        siblingDocumentInstanceSectionDto2Child3, siblingDocumentInstanceSectionDto2Child3Urls
    );
    Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls> urlsFunction = urlsByDocumentInstanceSectionDto::get;

    var documentSectionMailMergeResolver = mock(DocumentSectionMailMergeFieldResolver.class);

    when(documentSectionMailMergeResolver.resolve(siblingDocumentInstanceSectionDto1)).thenReturn(resolvedSiblingDocumentInstanceSectionDto1);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentInstanceSectionDto2)).thenReturn(resolvedSiblingDocumentInstanceSectionDto2);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentInstanceSectionDto2Child1)).thenReturn(resolvedSiblingDocumentInstanceSectionDto2Child1);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentInstanceSectionDto2Child1Child1)).thenReturn(resolvedSiblingDocumentInstanceSectionDto2Child1Child1);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentInstanceSectionDto2Child2)).thenReturn(resolvedSiblingDocumentInstanceSectionDto2Child2);
    when(documentSectionMailMergeResolver.resolve(siblingDocumentInstanceSectionDto2Child3)).thenReturn(resolvedSiblingDocumentInstanceSectionDto2Child3);

    assertThat(
        documentInstanceSectionViewService.getSiblingDocumentInstanceSectionSummaryViews(
            parentSectionNumberString,
            siblingDocumentInstanceSectionDtos,
            urlsFunction,
            documentSectionMailMergeResolver
        )
    ).containsExactly(
        DocumentInstanceSectionSummaryView.from(
            null,
            siblingDocumentInstanceSectionDto1,
            resolvedSiblingDocumentInstanceSectionDto1,
            siblingDocumentInstanceSectionDto1Urls,
            List.of()
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.1",
            siblingDocumentInstanceSectionDto2,
            resolvedSiblingDocumentInstanceSectionDto2,
            siblingDocumentInstanceSectionDto2Urls,
            List.of(
                DocumentInstanceSectionSummaryView.from(
                    "1.1.1",
                    siblingDocumentInstanceSectionDto2Child1,
                    resolvedSiblingDocumentInstanceSectionDto2Child1,
                    siblingDocumentInstanceSectionDto2Child1Urls,
                    List.of(
                        DocumentInstanceSectionSummaryView.from(
                            "1.1.1.1",
                            siblingDocumentInstanceSectionDto2Child1Child1,
                            resolvedSiblingDocumentInstanceSectionDto2Child1Child1,
                            siblingDocumentInstanceSectionDto2Child1Child1Urls,
                            List.of()
                        )
                    )
                ),
                DocumentInstanceSectionSummaryView.from(
                    null,
                    siblingDocumentInstanceSectionDto2Child2,
                    resolvedSiblingDocumentInstanceSectionDto2Child2,
                    siblingDocumentInstanceSectionDto2Child2Urls,
                    List.of()
                ),
                DocumentInstanceSectionSummaryView.from(
                    "1.1.2",
                    siblingDocumentInstanceSectionDto2Child3,
                    resolvedSiblingDocumentInstanceSectionDto2Child3,
                    siblingDocumentInstanceSectionDto2Child3Urls,
                    List.of()
                )
            )
        )
    );
  }

  @Test
  void getDocumentInstanceSectionSummaryView() {
    var documentSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var errorMessage = "There are errors";
    var resolvedDocumentWithErrors = ResolvedDocumentMailMergeFieldTestUtil.newBuilder()
        .withDocumentMailMergeFieldResolveResult(DocumentMailMergeFieldResolveResult.error(errorMessage))
        .build();

    var resolvedSectionDto = ResolvedDocumentSectionTestUtil
        .newBuilder()
        .withResolvedContent(documentSectionDto.content())
        .withResolvedDocumentMailMergeField(List.of(resolvedDocumentWithErrors))
        .build();

    var documentSectionMailMergeFieldResolver = mock(DocumentSectionMailMergeFieldResolver.class);
    doReturn(documentSectionMailMergeFieldResolver).when(documentInstanceSectionViewService)
        .newResolver(documentMailMergeFieldFormatter);
    when(documentSectionMailMergeFieldResolver.resolve(documentSectionDto)).thenReturn(resolvedSectionDto);

    assertThat(
        documentInstanceSectionViewService.getDocumentInstanceSectionErrorMessages(
            documentSectionDto,
            documentMailMergeFieldFormatter
        )
    ).containsExactly(errorMessage);
  }

  @Test
  void getDocumentInstanceSectionsSummaryViewsForDocumentInstances() {
    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder().build();
    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder().build();

    Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls> urlsFunction = documentInstanceSectionDto -> null;

    var topLevelDocumentInstanceSectionDto1 = DocumentInstanceSectionDtoTestUtil.builder().build();
    var topLevelDocumentInstanceSectionDto2 = DocumentInstanceSectionDtoTestUtil.builder().build();

    var topLevelDocumentInstanceSectionSummaryView1 = List.of(DocumentInstanceSectionSummaryViewTestUtil.newBuilder().build());
    var topLevelDocumentInstanceSectionSummaryView2 = List.of(DocumentInstanceSectionSummaryViewTestUtil.newBuilder().build());

    when(documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtosForDocumentInstanceDtos(
        List.of(documentInstanceDto1, documentInstanceDto2)))
        .thenReturn(Map.of(
            documentInstanceDto1, List.of(topLevelDocumentInstanceSectionDto1),
            documentInstanceDto2, List.of(topLevelDocumentInstanceSectionDto2)
        ));

    doReturn(topLevelDocumentInstanceSectionSummaryView1)
        .when(documentInstanceSectionViewService)
        .getSiblingDocumentInstanceSectionSummaryViews(
            isNull(),
            eq(List.of(topLevelDocumentInstanceSectionDto1)),
            eq(urlsFunction),
            any(DocumentSectionMailMergeFieldResolver.class)
        );

    doReturn(topLevelDocumentInstanceSectionSummaryView2)
        .when(documentInstanceSectionViewService)
        .getSiblingDocumentInstanceSectionSummaryViews(
            isNull(),
            eq(List.of(topLevelDocumentInstanceSectionDto2)),
            eq(urlsFunction),
            any(DocumentSectionMailMergeFieldResolver.class)
        );

    assertThat(
        documentInstanceSectionViewService.getDocumentInstanceSectionsSummaryViewsForDocumentInstances(
            List.of(documentInstanceDto1, documentInstanceDto2),
            urlsFunction,
            documentMailMergeFieldFormatter
        )
    ).usingRecursiveComparison().isEqualTo(Map.of(
        documentInstanceDto2, DocumentInstanceSectionsSummaryView.from(topLevelDocumentInstanceSectionSummaryView2),
        documentInstanceDto1, DocumentInstanceSectionsSummaryView.from(topLevelDocumentInstanceSectionSummaryView1)
    ));
  }
}
