package co.com.bancolombia.mongo.mapper;


import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.mongo.document.FranchiseDocument;

public class FranchiseMapper {

    private FranchiseMapper() {
    }

    public static FranchiseDTO toDomain(FranchiseDocument d) { return new FranchiseDTO(d.getId(), d.getName()); }
    public static FranchiseDocument toDoc(FranchiseDTO f) { return new FranchiseDocument(f.getId(), f.getName()); }
}
