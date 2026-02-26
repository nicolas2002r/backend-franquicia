package co.com.bancolombia.mongo.document;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductDocumentTest {

    @Test
    void shouldCreateDocumentUsingNoArgsConstructor() {
        ProductDocument document = new ProductDocument();
        document.setId("product123");
        document.setBranchId("branch456");
        document.setName("Laptop");
        document.setStock(10);

        assertAll(
                () -> assertEquals("product123", document.getId()),
                () -> assertEquals("branch456", document.getBranchId()),
                () -> assertEquals("Laptop", document.getName()),
                () -> assertEquals(10, document.getStock())
        );
    }

    @Test
    void shouldCreateDocumentUsingAllArgsConstructor() {
        ProductDocument document = new ProductDocument("product123", "branch456", "Laptop", 10);

        assertEquals("product123", document.getId());
        assertEquals("branch456", document.getBranchId());
        assertEquals("Laptop", document.getName());
        assertEquals(10, document.getStock());
    }
}
