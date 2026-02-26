package co.com.bancolombia.mongo.document;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BranchDocumentTest {

    @Test
    void shouldCreateDocumentUsingNoArgsConstructor() {
        BranchDocument document = new BranchDocument();
        document.setId("branch123");
        document.setFranchiseId("franchise456");
        document.setName("Main Branch");

        assertAll(
                () -> assertEquals("branch123", document.getId()),
                () -> assertEquals("franchise456", document.getFranchiseId()),
                () -> assertEquals("Main Branch", document.getName())
        );
    }

    @Test
    void shouldCreateDocumentUsingAllArgsConstructor() {
        BranchDocument document = new BranchDocument("branch123", "franchise456", "Main Branch");

        assertEquals("branch123", document.getId());
        assertEquals("franchise456", document.getFranchiseId());
        assertEquals("Main Branch", document.getName());
    }
}
