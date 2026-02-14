package co.com.bancolombia.mongo.mapper;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.mongo.document.BranchDocument;

public class BranchMapper {

    private BranchMapper() {
    }

    public static Branch toDomain(BranchDocument d) { return new Branch(d.getId(), d.getFranchiseId(), d.getName()); }
    public static BranchDocument toDoc(Branch b) { return new BranchDocument(b.id(), b.franchiseId(), b.name()); }
}
