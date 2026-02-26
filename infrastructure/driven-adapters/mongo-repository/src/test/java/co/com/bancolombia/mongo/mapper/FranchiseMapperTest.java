package co.com.bancolombia.mongo.mapper;

import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.mongo.document.FranchiseDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FranchiseMapperTest {

    @Test
    void toDomainShouldConvertDocumentToDTO() {
        FranchiseDocument doc = new FranchiseDocument("franchise123", "My Franchise");

        FranchiseDTO dto = FranchiseMapper.toDomain(doc);

        assertAll(
                () -> assertEquals("franchise123", dto.getId()),
                () -> assertEquals("My Franchise", dto.getName())
        );
    }

    @Test
    void toDomainWithNullFieldsShouldPreserveNulls() {
        FranchiseDocument doc = new FranchiseDocument(null, null);

        FranchiseDTO dto = FranchiseMapper.toDomain(doc);

        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getName())
        );
    }

    @Test
    void toDocShouldConvertDTOToDocument() {
        FranchiseDTO dto = new FranchiseDTO("franchise123", "My Franchise");

        FranchiseDocument doc = FranchiseMapper.toDoc(dto);

        assertAll(
                () -> assertEquals("franchise123", doc.getId()),
                () -> assertEquals("My Franchise", doc.getName())
        );
    }

    @Test
    void toDocWithNullFieldsShouldPreserveNulls() {
        FranchiseDTO dto = new FranchiseDTO(null, null);

        FranchiseDocument doc = FranchiseMapper.toDoc(dto);

        assertAll(
                () -> assertNull(doc.getId()),
                () -> assertNull(doc.getName())
        );
    }
}
