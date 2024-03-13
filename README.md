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

## Development setup

### Pre-requisites

- Java 17
