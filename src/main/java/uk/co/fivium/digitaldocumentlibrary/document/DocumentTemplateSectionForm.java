package uk.co.fivium.digitaldocumentlibrary.document;

public class DocumentTemplateSectionForm {

  private String title;
  private String content;
  private String conditionMnemonic;
  private Boolean numbered;
  private Boolean hasPageBreakBefore;

  public static DocumentTemplateSectionForm empty() {
    return new DocumentTemplateSectionForm();
  }

  public static DocumentTemplateSectionForm from(DocumentTemplateSectionDto documentTemplateSectionDto) {
    var form = new DocumentTemplateSectionForm();
    form.setTitle(documentTemplateSectionDto.title());
    form.setContent(documentTemplateSectionDto.content());
    form.setConditionMnemonic(documentTemplateSectionDto.conditionMnemonic());
    form.setNumbered(documentTemplateSectionDto.numbered());
    form.setHasPageBreakBefore(documentTemplateSectionDto.hasPageBreakBefore());
    return form;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getConditionMnemonic() {
    return conditionMnemonic;
  }

  public void setConditionMnemonic(String conditionMnemonic) {
    this.conditionMnemonic = conditionMnemonic;
  }

  public Boolean getNumbered() {
    return numbered;
  }

  public void setNumbered(Boolean numbered) {
    this.numbered = numbered;
  }

  public Boolean getHasPageBreakBefore() {
    return hasPageBreakBefore;
  }

  public void setHasPageBreakBefore(Boolean hasPageBreakBefore) {
    this.hasPageBreakBefore = hasPageBreakBefore;
  }
}
