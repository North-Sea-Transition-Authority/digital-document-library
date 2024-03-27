package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
class DocumentInstanceSectionControllerHelperServiceTest {

  @Mock
  private DocumentInstanceSectionService documentInstanceSectionService;

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  @Spy
  private DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;

  private final DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter = new TestDocumentMailMergeFieldFormatter();

  @Test
  void getDocumentInstanceSectionsSummaryView() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls> urlsFunction = documentInstanceSectionDto -> null;

    var topLevelDocumentInstanceSectionDtos = List.of(DocumentInstanceSectionDtoTestUtil.builder().build());
    var topLevelDocumentInstanceSectionSummaryViews = List.of(mock(DocumentInstanceSectionSummaryView.class));

    when(documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto))
        .thenReturn(topLevelDocumentInstanceSectionDtos);

    doReturn(topLevelDocumentInstanceSectionSummaryViews)
        .when(documentInstanceSectionControllerHelperService)
        .getSiblingDocumentInstanceSectionSummaryViews(
            null,
            topLevelDocumentInstanceSectionDtos,
            urlsFunction,
            documentMailMergeFieldFormatter
        );

    assertThat(
         documentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
             documentInstanceDto,
             urlsFunction,
             documentMailMergeFieldFormatter
         )
    ).isEqualTo(new DocumentInstanceSectionsSummaryView(topLevelDocumentInstanceSectionSummaryViews, List.of()));
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

    var resolvedSiblingDocumentInstanceSectionDto1 = ResolvedDocumentInstanceSectionTestUtil.newBuilder().withResolvedContent("Test content 1").build();
    var resolvedSiblingDocumentInstanceSectionDto2 = ResolvedDocumentInstanceSectionTestUtil.newBuilder().withResolvedContent("Test content 2").build();
    var resolvedSiblingDocumentInstanceSectionDto2Child1 = ResolvedDocumentInstanceSectionTestUtil.newBuilder().withResolvedContent("Test content 3").build();
    var resolvedSiblingDocumentInstanceSectionDto2Child1Child1 = ResolvedDocumentInstanceSectionTestUtil.newBuilder().withResolvedContent("Test content 4").build();
    var resolvedSiblingDocumentInstanceSectionDto2Child2 = ResolvedDocumentInstanceSectionTestUtil.newBuilder().withResolvedContent("Test content 5").build();
    var resolvedSiblingDocumentInstanceSectionDto2Child3 = ResolvedDocumentInstanceSectionTestUtil.newBuilder().withResolvedContent("Test content 6").build();

    var urlsByDocumentInstanceSectionDto = Map.of(
        siblingDocumentInstanceSectionDto1, siblingDocumentInstanceSectionDto1Urls,
        siblingDocumentInstanceSectionDto2, siblingDocumentInstanceSectionDto2Urls,
        siblingDocumentInstanceSectionDto2Child1, siblingDocumentInstanceSectionDto2Child1Urls,
        siblingDocumentInstanceSectionDto2Child1Child1, siblingDocumentInstanceSectionDto2Child1Child1Urls,
        siblingDocumentInstanceSectionDto2Child2, siblingDocumentInstanceSectionDto2Child2Urls,
        siblingDocumentInstanceSectionDto2Child3, siblingDocumentInstanceSectionDto2Child3Urls
    );
    Function<DocumentInstanceSectionDto, DocumentInstanceSectionUrls> urlsFunction = urlsByDocumentInstanceSectionDto::get;

    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto1, documentMailMergeFieldFormatter))
        .thenReturn(resolvedSiblingDocumentInstanceSectionDto1);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2, documentMailMergeFieldFormatter))
        .thenReturn(resolvedSiblingDocumentInstanceSectionDto2);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2Child1, documentMailMergeFieldFormatter))
        .thenReturn(resolvedSiblingDocumentInstanceSectionDto2Child1);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2Child1Child1, documentMailMergeFieldFormatter))
        .thenReturn(resolvedSiblingDocumentInstanceSectionDto2Child1Child1);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2Child2, documentMailMergeFieldFormatter))
        .thenReturn(resolvedSiblingDocumentInstanceSectionDto2Child2);
    when(documentMailMergeFieldService.resolveMailMergeFields(siblingDocumentInstanceSectionDto2Child3, documentMailMergeFieldFormatter))
        .thenReturn(resolvedSiblingDocumentInstanceSectionDto2Child3);

    assertThat(
        documentInstanceSectionControllerHelperService.getSiblingDocumentInstanceSectionSummaryViews(
            parentSectionNumberString,
            siblingDocumentInstanceSectionDtos,
            urlsFunction,
            documentMailMergeFieldFormatter
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
  void createDocumentInstanceSection() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var parentDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var form = DocumentInstanceSectionFormTestUtil.builder().build();
    int displayOrder = 1;

    documentInstanceSectionControllerHelperService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form,
        displayOrder
    );

    verify(documentInstanceSectionService).createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore(),
        displayOrder
    );
  }

  @Test
  void editDocumentInstanceSection() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var form = DocumentInstanceSectionFormTestUtil.builder().build();

    documentInstanceSectionControllerHelperService.editDocumentInstanceSection(documentInstanceSectionDto, form);

    verify(documentInstanceSectionService).editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore()
    );
  }
}
