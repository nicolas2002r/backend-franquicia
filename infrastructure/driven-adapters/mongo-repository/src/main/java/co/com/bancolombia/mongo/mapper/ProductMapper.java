package co.com.bancolombia.mongo.mapper;


import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.mongo.document.ProductDocument;

public class ProductMapper {

    private ProductMapper() {
    }

    public static ProductDTO toDomain(ProductDocument d) { return new ProductDTO(d.getId(), d.getBranchId(), d.getName(), d.getStock()); }
    public static ProductDocument toDoc(ProductDTO p) { return new ProductDocument(p.getId(), p.getBranchId(), p.getName(), p.getStock()); }
}
