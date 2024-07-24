package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentSectionMailMergeFieldResolverTest {

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  private final DocumentMailMergeFieldFormatter documentMailMergeFieldFormatter = new TestDocumentMailMergeFieldFormatter();

  private DocumentSectionMailMergeFieldResolver resolver;

  @BeforeEach
  void setUp() {
    this.resolver = new DocumentSectionMailMergeFieldResolver(
        documentMailMergeFieldService,
        documentMailMergeFieldFormatter
    );
  }

  @Test
  void resolve_allMailMergeFieldsValid() {
    var mailMergeField1Mnemonic = "MAIL_MERGE_FIELD_1";
    var mailMergeField2Mnemonic = "MAIL_MERGE_FIELD_2";

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withContent(
            """
            Example text
            
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            (((MAIL_MERGE_FIELD_2)))
            ((((MAIL_MERGE_FIELD_2))))
            (((((MAIL_MERGE_FIELD_2)))))
            (Example text in brackets)
            """
        )
        .build();

    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField1ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue("Resolved mail merge field 1")
        .build();

    var documentMailMergeField2 = mock(DocumentMailMergeField.class);
    var documentMailMergeField2ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue("Resolved mail merge field 2")
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField1Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField1));

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField2Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField2));

    when(documentMailMergeField1.resolve(documentInstanceDto)).thenReturn(documentMailMergeField1ResolveResult);
    when(documentMailMergeField2.resolve(documentInstanceDto)).thenReturn(documentMailMergeField2ResolveResult);

    assertThat(resolver.resolve(documentInstanceSectionDto))
        .isEqualTo(ResolvedDocumentSectionTestUtil.newBuilder()
            .withResolvedContent(
                """
                Example text
                        
                Resolved mail merge field 1 (success)
                Resolved mail merge field 2 (success)
                (Resolved mail merge field 2 (success))
                ((Resolved mail merge field 2 (success)))
                (((Resolved mail merge field 2 (success))))
                (Example text in brackets)
                """
            )
            .withResolvedDocumentMailMergeField(List.of(
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult)
            ))
            .build());
  }

  @Test
  void resolve_singleResolvePerUniqueMailMergeField() {
    var mailMergeField1Mnemonic = "MAIL_MERGE_FIELD_1";
    var mailMergeField2Mnemonic = "MAIL_MERGE_FIELD_2";

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withContent(
            """
            Example text
            
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            """
        )
        .build();

    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField1ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue("Resolved mail merge field 1")
        .build();

    var documentMailMergeField2 = mock(DocumentMailMergeField.class);
    var documentMailMergeField2ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue("Resolved mail merge field 2")
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField1Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField1));

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField2Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField2));

    when(documentMailMergeField1.resolve(documentInstanceDto)).thenReturn(documentMailMergeField1ResolveResult);
    when(documentMailMergeField2.resolve(documentInstanceDto)).thenReturn(documentMailMergeField2ResolveResult);

    assertThat(resolver.resolve(documentInstanceSectionDto))
        .isEqualTo(ResolvedDocumentSectionTestUtil.newBuilder()
            .withResolvedContent(
                """
                Example text
                        
                Resolved mail merge field 1 (success)
                Resolved mail merge field 2 (success)
                Resolved mail merge field 1 (success)
                Resolved mail merge field 2 (success)
                Resolved mail merge field 1 (success)
                Resolved mail merge field 2 (success)
                """
            )
            .withResolvedDocumentMailMergeField(List.of(
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult)
            ))
            .build());

    // it should only be resolved once and reused in later resolve attempts
    verify(documentMailMergeField1, times(1)).resolve(documentInstanceDto);
    verify(documentMailMergeField2, times(1)).resolve(documentInstanceDto);
  }

  @Test
  void resolve_invalidMailMergeField() {
    var mailMergeField1Mnemonic = "MAIL_MERGE_FIELD_1";

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withContent(
            """
            Example text
            
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            """
        )
        .build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField1ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue("Resolved mail merge field 1")
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField1Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField1));

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2"))
        .thenReturn(Optional.empty());

    when(documentMailMergeField1.resolve(documentInstanceDto)).thenReturn(documentMailMergeField1ResolveResult);

    assertThat(resolver.resolve(documentInstanceSectionDto))
        .isEqualTo(ResolvedDocumentSectionTestUtil.newBuilder()
            .withResolvedContent(
                """
                Example text
                        
                Resolved mail merge field 1 (success)
                ((MAIL_MERGE_FIELD_2)) (error)
                """
            )
            .withResolvedDocumentMailMergeField(List.of(
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult)
            ))
            .build()
        );
  }

  @Test
  void resolve_template_allMailMergeFieldsValid() {
    var mailMergeField1Mnemonic = "MAIL_MERGE_FIELD_1";
    var mailMergeField2Mnemonic = "MAIL_MERGE_FIELD_2";

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withContent(
            """
            Example text
            
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            (((MAIL_MERGE_FIELD_2)))
            ((((MAIL_MERGE_FIELD_2))))
            (((((MAIL_MERGE_FIELD_2)))))
            (Example text in brackets)
            """
        )
        .build();

    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField1ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue(mailMergeField1Mnemonic)
        .build();

    var documentMailMergeField2 = mock(DocumentMailMergeField.class);
    var documentMailMergeField2ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue(mailMergeField2Mnemonic)
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField1Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField1));

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField2Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField2));

    when(documentMailMergeField1.resolve(documentTemplateDto)).thenReturn(documentMailMergeField1ResolveResult);
    when(documentMailMergeField2.resolve(documentTemplateDto)).thenReturn(documentMailMergeField2ResolveResult);

    assertThat(resolver.resolve(documentTemplateSectionDto))
        .isEqualTo(ResolvedDocumentSectionTestUtil.newBuilder()
            .withResolvedContent(
                """
                Example text
                        
                MAIL_MERGE_FIELD_1 (success)
                MAIL_MERGE_FIELD_2 (success)
                (MAIL_MERGE_FIELD_2 (success))
                ((MAIL_MERGE_FIELD_2 (success)))
                (((MAIL_MERGE_FIELD_2 (success))))
                (Example text in brackets)
                """
            )
            .withResolvedDocumentMailMergeField(List.of(
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult)
            ))
            .build());
  }

  @Test
  void resolve_template_singleResolvePerUniqueMailMergeField() {
    var mailMergeField1Mnemonic = "MAIL_MERGE_FIELD_1";
    var mailMergeField2Mnemonic = "MAIL_MERGE_FIELD_2";

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withContent(
            """
            Example text
            
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            """
        )
        .build();

    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField1ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue(mailMergeField1Mnemonic)
        .build();

    var documentMailMergeField2 = mock(DocumentMailMergeField.class);
    var documentMailMergeField2ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue(mailMergeField2Mnemonic)
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField1Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField1));

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField2Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField2));

    when(documentMailMergeField1.resolve(documentTemplateDto)).thenReturn(documentMailMergeField1ResolveResult);
    when(documentMailMergeField2.resolve(documentTemplateDto)).thenReturn(documentMailMergeField2ResolveResult);

    assertThat(resolver.resolve(documentTemplateSectionDto))
        .isEqualTo(ResolvedDocumentSectionTestUtil.newBuilder()
            .withResolvedContent(
                """
                Example text
                        
                MAIL_MERGE_FIELD_1 (success)
                MAIL_MERGE_FIELD_2 (success)
                MAIL_MERGE_FIELD_1 (success)
                MAIL_MERGE_FIELD_2 (success)
                MAIL_MERGE_FIELD_1 (success)
                MAIL_MERGE_FIELD_2 (success)
                """
            )
            .withResolvedDocumentMailMergeField(List.of(
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult),
                new ResolvedDocumentMailMergeField(documentMailMergeField2, documentMailMergeField2ResolveResult)
            ))
            .build());

    // it should only be resolved once and reused in later resolve attempts
    verify(documentMailMergeField1, times(1)).resolve(documentTemplateDto);
    verify(documentMailMergeField2, times(1)).resolve(documentTemplateDto);
  }

  @Test
  void resolve_template_invalidMailMergeField() {
    var mailMergeField1Mnemonic = "MAIL_MERGE_FIELD_1";

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withContent(
            """
            Example text
            
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            """
        )
        .build();

    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField1ResolveResult = DocumentMailMergeFieldResolveResultTestUtil.newBuilder()
        .withResolvedValue(mailMergeField1Mnemonic)
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mailMergeField1Mnemonic))
        .thenReturn(Optional.of(documentMailMergeField1));

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2"))
        .thenReturn(Optional.empty());

    when(documentMailMergeField1.resolve(documentTemplateDto)).thenReturn(documentMailMergeField1ResolveResult);

    assertThat(resolver.resolve(documentTemplateSectionDto))
        .isEqualTo(ResolvedDocumentSectionTestUtil.newBuilder()
            .withResolvedContent(
                """
                Example text
                        
                MAIL_MERGE_FIELD_1 (success)
                ((MAIL_MERGE_FIELD_2)) (error)
                """
            )
            .withResolvedDocumentMailMergeField(List.of(
                new ResolvedDocumentMailMergeField(documentMailMergeField1, documentMailMergeField1ResolveResult)
            ))
            .build()
        );
  }
}