# Digital Document Library

The Digital Document Library provides APIs for creating PDF documents.

## Using the library

### Setup steps

#### 1. Add the starter dependency to your project

Add the following dependency to your `build.gradle`:

```gradle
implementation 'uk.co.fivium:digital-document-library-spring-boot-starter:version'
```

#### 2. Setup Envers

Your application will need to include a `org.hibernate.envers.RevisionEntity` which uses a table with the name
`audit_revisions`.

### Components

The library provides APIs for two key document components, document templates and document instances.

### Document Templates

A document template is a global template that contains many document template sections. 

The `DocumentTemplateService` provides API methods for document templates and the `DocumentTemplateSectionService` provides
API methods for document template sections.

To create a document template, call `DocumentTemplateService#createDocumentTemplate`. Similarly, to create a document template
section, call `DocumentTemplateSectionService#createDocumentTemplateSection`.

### Document Instances

A document instance is an instance of a document template and contains many document instance sections. A document instance
and it's sections can be edited independently of the document template it was created from. Document instances end up becoming
your documents and can be rendered into a PDF.

Document instances are scoped to a consumers domain concept, such as an application. As an example, you may have a consent
document template and then create a document instance of the template per application for that application's consent. When you
create a document instance you will provide an item reference and an item type which form a unique key for the document instance.
The item reference will normally be something unique per instance of the consumer domain concept such as an application ID
and the item type will be a string representing the consumer domain concept (e.g. `APPLICATION`).

The `DocumentInstanceService` provides API methods for document instances and the `DocumentInstanceSectionService` provides
API methods for document instance sections.

The API methods for document instances work the same way as document templates in methods named instance instead of template.
For example, to create a document instance call `DocumentInstanceService#createDocumentInstance`.

When you create a document instance the library will copy all the sections from the document template to the document instance.

### Document Template Section Conditions

A document template section can have an optional condition set on it which determines whether it should be included in a document
instance or not.

To define a new document template section condition, implement `DocumentTemplateSectionCondition` and add `@Component` to it so
that it is picked up by Spring's dependency injection. You do not need to register the condition, this will happen automatically.

For example:

```java
@Component
class GasWillBeInjectedCondition implements DocumentTemplateSectionCondition {

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final ApplicationFlagService applicationFlagService;

  @Autowired
  GasWillBeInjectedCondition(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      ApplicationFlagService applicationFlagService
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.applicationFlagService = applicationFlagService;
  }

  @Override
  public String getMnemonic() {
    return "GAS_WILL_BE_INJECTED";
  }

  @Override
  public String getTitle() {
    return "Gas will be injected";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return documentTemplateType == DocumentTemplateType.FIELD_PRODUCTION_CONSENT;
  }

  @Override
  public boolean evaluate(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);
    return applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED)
        .orElse(false);
  }
}
```

The mnemonic is used for storing the condition against a document template in the database.

The title can be shown on a page to allow a user to select the condition. 

The `isApplicable` method is used to determine whether a condition is applicable to a document template. You should only allow
users to select applicable conditions on a document template.

The `evaluate` method controls whether the section should be included a document instance or not.

The `DocumentTemplateSectionConditionService` provides API methods for document template section conditions.

### Mail Merge Fields

The library supports mail merge fields which can be used to dynamically insert data into the document. 

To define a new mail merge field, implement `DocumentMailMergeField` and add `@Component` to it so that it is picked up by
Spring's dependency injection. You do not need to register the condition, this will happen automatically.

For example:

```java
@Order(DocumentMailMergeFieldDisplayOrders.REGULATOR_NAME)
@Component
class RegulatorNameMailMergeField implements DocumentMailMergeField {

  static final String DESCRIPTION = "The regulator's name.";

  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  RegulatorNameMailMergeField(CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties) {
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  @Override
  public String getMnemonic() {
    return "REGULATOR_NAME";
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    return DocumentMailMergeFieldResolveResult.success(customerBrandingConfigurationProperties.name());
  }
}
```

The mnemonic is used to identify the mail merge field in a document template/section's content. Users can use it using the
`(())` syntax, for example for the above mail merge field the user would enter `((REGULATOR_NAME))`.

The description can be shown on a page explaining what the mail merge field does.

The `isApplicable` method is used to determine whether a mail merge field is applicable to a document template. You should only
allow users to use applicable mail merge fields in a document template/instance section's content.

The `resolve` method resolves the value of the mail merge field and returns a `DocumentMailMergeFieldResolveResult`. If you 
resolve the value successfully you should call `DocumentMailMergeFieldResolveResult#success` with the value resolved.
Resolved mail merge field values are escaped by default. If you want to return an unescaped result such as HTML from a mail merge
field, call `DocumentMailMergeFieldResolveResult#successNoEsc` instead.

If you cannot resolve the mail merge field and do not want to throw an exception, you can call `DocumentMailMergeFieldResolveResult#error`
with an error message to show on the page.

The `DocumentMailMergeFieldService` provides API methods for document mail merge fields.

Mail merge fields are only resolvable on a document instance, not a document template.

### Bootstrapping Document Templates

You will likely want to create your initial document templates when your application starts. The recommended way to do this is to
listen for the `ApplicationReadyEvent` and call the library service methods to create your initial document templates if no
templates exist yet.

For example:

```java
@EventListener(ApplicationReadyEvent.class)
void onApplicationReadyEvent() {
  if (!documentTemplateService.getDocumentTemplateDtos().isEmpty()) {
    LOGGER.info("Found existing document templates, not creating initial document templates");
    return;
  }
  
  LOGGER.info("Creating initial document templates");
  
  createFieldProductionConsentDocumentTemplate();
}

void createFieldProductionConsentDocumentTemplate() {
  var fieldProductionConsentDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
      DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic(),
      "Field Production Consent",
      "Document template used for creating Field Production Consent documents",
      "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
      1
  );

  documentTemplateSectionService.createDocumentTemplateSection(
      fieldProductionConsentDocumentTemplateDto,
      null,
      "Header",
      """
      <p>Date: ((ISSUE_DATE))</p>\
      <p>\
      <strong>\
      PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)<br/>\
      ((CONSENT_LENGTH_UPPER_CASE))  DEVELOPMENT AND PRODUCTION CONSENT\
      </strong>\
      <p>\
      <p>To: Licensees of Petroleum Production Licences ((LICENCE_REFERENCE_LIST)) (listed in schedule 2 hereto).</p>\
      """,
      null,
      false,
      false,
      1
  );
  ...
}
```

### Document Template / Document Instance section content sanitisation

When you create/edit a document template/instance section, the content will be run through the jsoup sanitiser. This is so users 
are not able to include elements such as `<script>` tags in rich text section content.

You can customise the sanitiser by providing your own `Safelist`. For example:

```java
@Configuration
public class DocumentBeanConfiguration {

  @Bean
  Safelist fieldConsentsDocumentSafelist() {
    return Safelist.basic()
        .addAttributes("p", "style")
        .addTags("s");
  }
}
```

### Document Template / Document Instance views

The library provides various views that you can use to show your document template/instance on a web page and for rendering your
PDFs. You can get these views via the `DocumentTemplateViewService`, `DocumentTemplateSectionViewService`,
`DocumentInstanceViewService` and `DocumentInstanceSectionViewService`.

Mail merge fields will automatically be resolved in the document instance views, you do not need to manually resolve them.

As an example, a controller endpoint that shows a list of all document templates using the `DocumentTemplateViewService` may look
like the following:

```java
@GetMapping
public ModelAndView getDocumentTemplates() {
  var documentTemplateSummaryViews = documentTemplateViewService.getDocumentTemplateSummaryViews(
      documentTemplateDto -> ReverseRouter.route(on(DocumentTemplateController.class)
          .getViewDocumentTemplate(documentTemplateDto.id()))
  );

  return new ModelAndView("fcs/document/template/documentTemplates")
      .addObject("documentTemplateSummaryViews", documentTemplateSummaryViews);
}
```

Similarly, a controller endpoint that shows all sections in a specific document template using the
`DocumentTemplateSectionViewService` may look like the following:

```java
@GetMapping("/{documentTemplateId}")
public ModelAndView getViewDocumentTemplate(@PathVariable UUID documentTemplateId) {
  var documentTemplateDto = documentTemplateService.getDocumentTemplateDtoOrThrow(documentTemplateId);

  var topLevelDocumentTemplateSectionSummaryViews = documentTemplateSectionViewService.getTopLevelDocumentTemplateSectionSummaryViews(
      documentTemplateDto,
      this::getDocumentTemplateSectionUrls
  );

  return new ModelAndView("fcs/document/template/viewDocumentTemplate")
      .addObject("pageTitle", documentTemplateDto.title())
      .addObject("topLevelDocumentTemplateSectionSummaryViews", topLevelDocumentTemplateSectionSummaryViews);
}

private DocumentTemplateSectionUrls getDocumentTemplateSectionUrls(DocumentTemplateSectionDto documentTemplateSectionDto) {
  var documentTemplateSectionId = documentTemplateSectionDto.id();

  return new DocumentTemplateSectionUrls(
      ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getAddDocumentTemplateSectionBefore(documentTemplateSectionId)),
      ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getAddDocumentTemplateSectionAfter(documentTemplateSectionId)),
      ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getAddDocumentTemplateSubsection(documentTemplateSectionId)),
      ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getEditDocumentTemplateSection(documentTemplateSectionId)),
      ReverseRouter.route(on(DocumentTemplateSectionController.class)
          .getRemoveDocumentTemplateSection(documentTemplateSectionId))
  );
}
```

### Rendering PDFs

Document instances can be rendered into a PDF to create your documents. The document instance's document template Freemarker
template will then be rendered and the resulting HTML will become the contents of your PDF.

To render a document instance into a PDF, call `DocumentInstanceService#renderPdf` with the `DocumentInstanceDto` you want to
render and a template model that will be available in the Freemarker template.

For example:
```java
Map<String, Object> templateModel = Map.of(
    "documentInstanceSectionsSummaryView", documentInstanceSectionsSummaryView,
    "isPreview", isPreview,
    "applicationReference", applicationService.generateApplicationReference(applicationVersion),
    "customerBrandingConfigurationProperties", customerBrandingConfigurationProperties
);

var pdfRenderResult = documentInstanceService.renderPdf(documentInstanceDto, templateModel);
```

You will want to get a `DocumentInstanceSectionsSummaryView` from the `DocumentInstanceSectionViewService` which will contain
the views for all of your document instance's sections and then pass that into the template model. Your Freemarker template
should then loop through these views and include them in the document content. 

You will want to `?no_esc` a section's content when including it in the PDF. This is so you can support rich text section content
and mail merge fields that resolve to HTML. As explained above, all section content is sanitised and all mail merge fields are
escaped unless you specifically opt out, so you do not need to worry about potential security concerns.

Example document template Freemarker template:
```html
<html>
<head>
  <link rel="stylesheet" href="classpath:///document-assets/all.css"/>
</head>
<body>
  <table class="header">
    <tbody>
      <tr>
        <td style="font-size: 10pt;">Application ref: ${applicationReference}</td>
        <td>
          <img src="classpath:///document-assets/nsta-logo-landscape-black.png" alt="" style="max-height: 20px; float: right;"/>
        </td>
      </tr>
    </tbody>
  </table>
  <#if isPreview>
    <div class="watermark">
      PREVIEW DOCUMENT
    </div>
  </#if>
  <table class="footer">
    <tbody>
      <tr>
        <td class="page-number"></td>
        <td>
          ${customerBrandingConfigurationProperties.name()} is the business name of the ${customerBrandingConfigurationProperties.legalName()}.
          ${customerBrandingConfigurationProperties.legalName()} is a limited company registered in England and Wales with
          registered number ${customerBrandingConfigurationProperties.registeredNumber()} and VAT registered number
          ${customerBrandingConfigurationProperties.vatNumber()}. Our registered office is at ${customerBrandingConfigurationProperties.address()}.
        </td>
      </tr>
    </tbody>
  </table>
  <#list documentInstanceSectionsSummaryView.topLevelDocumentInstanceSectionSummaryViews() as documentInstanceSectionSummaryView>
    <@sectionContentTable documentInstanceSectionSummaryView=documentInstanceSectionSummaryView/>
  </#list>
</body>
</html>

<#macro sectionContentTable documentInstanceSectionSummaryView>
  <#assign sectionNumber = documentInstanceSectionSummaryView.sectionNumber()!>
  <#assign hasPageBreakBefore = documentInstanceSectionSummaryView.hasPageBreakBefore()>
  <#assign content = documentInstanceSectionSummaryView.content()!>
  <#assign children = documentInstanceSectionSummaryView.children()>

  <#if hasPageBreakBefore>
    <div style="page-break-after: always;"></div>
  </#if>

  <table>
    <tbody>
    <tr>
      <td style="vertical-align: top;">
        <#if sectionNumber?has_content>
          ${sectionNumber}
        </#if>
      </td>
      <td style="vertical-align: top;">
        ${content?no_esc}
      </td>
    </tr>
    <tr>
      <td></td>
      <td>
        <#list children as child>
          <@sectionContentTable documentInstanceSectionSummaryView=child/>
        </#list>
      </td>
    </tr>
    </tbody>
  </table>
</#macro>
```

## Development setup

### Pre-requisites

- Java 21
