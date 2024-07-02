package uk.co.fivium.digitaldocumentlibrary.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentMailMergeFieldUtilTest {

  @Test
  void getMnemonicFromMailMergeFieldText() {
    assertThat(DocumentMailMergeFieldUtil.getMnemonicFromMailMergeFieldText("((MAIL_MERGE_FIELD_1))")).isEqualTo("MAIL_MERGE_FIELD_1");
  }
}