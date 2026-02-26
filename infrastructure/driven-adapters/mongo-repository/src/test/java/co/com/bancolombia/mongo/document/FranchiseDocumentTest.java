package co.com.bancolombia.mongo.document;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FranchiseDocumentTest {

    @Test
    void shouldCreateDocumentUsingNoArgsConstructor() {
        FranchiseDocument document = new FranchiseDocument();
        document.setId("franchise123");
        document.setName("My Franchise");

        assertAll(
                () -> assertEquals("franchise123", document.getId()),
                () -> assertEquals("My Franchise", document.getName())
        );
    }

    @Test
    void shouldCreateDocumentUsingAllArgsConstructor() {
        FranchiseDocument document = new FranchiseDocument("franchise123", "My Franchise");

        assertEquals("franchise123", document.getId());
        assertEquals("My Franchise", document.getName());
    }
}
