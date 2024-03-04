CREATE TABLE document_library_document_templates_aud(
  rev SERIAL
, revtype NUMERIC
, id UUID
, mnemonic TEXT
, title TEXT
, description TEXT
, template_path TEXT
, display_order INTEGER
, CONSTRAINT doc_lib_doc_templates_aud_pk PRIMARY KEY (rev, id)
, CONSTRAINT doc_lib_doc_templates_aud_rev_fk FOREIGN KEY (rev) REFERENCES audit_revisions (rev)
);

CREATE INDEX doc_lib_doc_templates_aud_rev_idx ON document_library_document_templates_aud (rev);

CREATE TABLE document_library_document_template_sections_aud(
  rev SERIAL
, revtype NUMERIC
, id UUID
, document_template_id UUID
, parent_id UUID
, title TEXT
, content TEXT
, condition_mnemonic TEXT
, numbered BOOLEAN
, has_page_break_before BOOLEAN
, display_order INTEGER
, CONSTRAINT doc_lib_doc_template_sections_aud_pk PRIMARY KEY (rev, id)
, CONSTRAINT doc_lib_doc_template_sections_aud_rev_fk FOREIGN KEY (rev) REFERENCES audit_revisions (rev)
);

CREATE INDEX doc_lib_doc_template_sections_aud_rev_idx ON document_library_document_template_sections_aud (rev);

CREATE TABLE document_library_document_instances_aud(
  rev SERIAL
, revtype NUMERIC
, id UUID
, item_reference TEXT
, item_type TEXT
, title TEXT
, description TEXT
, document_template_id UUID
, CONSTRAINT doc_lib_doc_instances_aud_pk PRIMARY KEY (rev, id)
, CONSTRAINT doc_lib_doc_instances_aud_rev_fk FOREIGN KEY (rev) REFERENCES audit_revisions (rev)
);

CREATE INDEX doc_lib_doc_instances_aud_rev_idx ON document_library_document_instances_aud (rev);

CREATE TABLE document_library_document_instance_sections_aud(
  rev SERIAL
, revtype NUMERIC
, id UUID
, document_instance_id UUID
, created_from_document_template_section_id UUID
, parent_id UUID
, title TEXT
, content TEXT
, numbered BOOLEAN
, has_page_break_before BOOLEAN
, display_order INTEGER
, CONSTRAINT doc_lib_doc_instance_sections_aud_pk PRIMARY KEY (rev, id)
, CONSTRAINT doc_lib_doc_instance_sections_aud_rev_fk FOREIGN KEY (rev) REFERENCES audit_revisions (rev)
);

CREATE INDEX doc_lib_doc_instance_sections_aud_rev_idx ON document_library_document_instance_sections_aud (rev);
