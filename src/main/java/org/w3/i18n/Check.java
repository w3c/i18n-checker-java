/* 
 * i18n-checker: https://github.com/w3c/i18n-checker
 *
 * Copyright © 2013 World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University, Beihang). All Rights Reserved. This work is distributed
 * under the W3C® Software License [1] in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */
package org.w3.i18n;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code Check} object represents a stateful process of performing i18n
 * checks on a {@code DocumentResource}.
 *
 * @author Joseph J Short
 */
class Check {

    private final DocumentResource documentResource;
    private final ParsedDocument parsedDocument;
    private final List<Assertion> assertions;

    public Check(DocumentResource documentResource) {
        // Use the DocumentResource to prepare a ParsedDocument.
        this.documentResource = documentResource;
        this.parsedDocument = new ParsedDocument(documentResource);

        // Perform checks.
        this.assertions = new ArrayList<>();
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public DocumentResource getDocumentResource() {
        return documentResource;
    }

    public ParsedDocument getParsedDocument() {
        return parsedDocument;
    }

    public List<Assertion> getAssertions() {
        return assertions;
    }
}
