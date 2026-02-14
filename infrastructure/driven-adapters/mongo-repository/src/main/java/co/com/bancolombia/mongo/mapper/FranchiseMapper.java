package co.com.bancolombia.mongo.mapper;


import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.mongo.document.FranchiseDocument;

public class FranchiseMapper {

    private FranchiseMapper() {
    }

    public static Franchise toDomain(FranchiseDocument d) { return new Franchise(d.getId(), d.getName()); }
    public static FranchiseDocument toDoc(Franchise f) { return new FranchiseDocument(f.id(), f.name()); }
}
