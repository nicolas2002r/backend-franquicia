package co.com.bancolombia.mongo.mapper;


import co.com.bancolombia.model.Product;
import co.com.bancolombia.mongo.document.ProductDocument;

public class ProductMapper {

    private ProductMapper() {
    }

    public static Product toDomain(ProductDocument d) { return new Product(d.getId(), d.getBranchId(), d.getName(), d.getStock()); }
    public static ProductDocument toDoc(Product p) { return new ProductDocument(p.id(), p.branchId(), p.name(), p.stock()); }
}
