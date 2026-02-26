package co.com.bancolombia.mongo.mapper;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.mongo.document.BranchDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BranchMapperTest {

    @Test
    void toDomainShouldConvertDocumentToDTO() {
        BranchDocument doc = new BranchDocument("branch123", "franchise456", "Main Branch");

        BranchDTO dto = BranchMapper.toDomain(doc);

        assertAll(
                () -> assertEquals("branch123", dto.getId()),
                () -> assertEquals("franchise456", dto.getFranchiseId()),
                () -> assertEquals("Main Branch", dto.getName())
        );
    }

    @Test
    void toDomainWithNullFieldsShouldPreserveNulls() {
        BranchDocument doc = new BranchDocument(null, null, null);

        BranchDTO dto = BranchMapper.toDomain(doc);

        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getFranchiseId()),
                () -> assertNull(dto.getName())
        );
    }

    @Test
    void toDocShouldConvertDTOToDocument() {
        BranchDTO dto = new BranchDTO("branch123", "franchise456", "Main Branch");

        BranchDocument doc = BranchMapper.toDoc(dto);

        assertAll(
                () -> assertEquals("branch123", doc.getId()),
                () -> assertEquals("franchise456", doc.getFranchiseId()),
                () -> assertEquals("Main Branch", doc.getName())
        );
    }

    @Test
    void toDocWithNullFieldsShouldPreserveNulls() {
        BranchDTO dto = new BranchDTO(null, null, null);

        BranchDocument doc = BranchMapper.toDoc(dto);

        assertAll(
                () -> assertNull(doc.getId()),
                () -> assertNull(doc.getFranchiseId()),
                () -> assertNull(doc.getName())
        );
    }
}
