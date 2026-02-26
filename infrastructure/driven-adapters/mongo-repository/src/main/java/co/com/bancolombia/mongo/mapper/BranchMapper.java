package co.com.bancolombia.mongo.mapper;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.mongo.document.BranchDocument;

public class BranchMapper {

    private BranchMapper() {
    }

    public static BranchDTO toDomain(BranchDocument d) { return new BranchDTO(d.getId(), d.getFranchiseId(), d.getName()); }
    public static BranchDocument toDoc(BranchDTO b) { return new BranchDocument(b.getId(), b.getFranchiseId(), b.getName()); }
}
