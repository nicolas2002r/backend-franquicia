package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.model.RequestRecords;
import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.MaxStockByBranch;
import co.com.bancolombia.model.Product;

public class FranchiseMapper {

    private FranchiseMapper() {}

    public static RequestRecords.FranchiseResponse toResponse(Franchise f) {
        return new RequestRecords.FranchiseResponse(f.id(), f.name());
    }

    public static RequestRecords.BranchResponse toResponse(Branch b) {
        return new RequestRecords.BranchResponse(b.id(), b.franchiseId(), b.name());
    }

    public static RequestRecords.ProductResponse toResponse(Product p) {
        return new RequestRecords.ProductResponse(p.id(), p.branchId(), p.name(), p.stock());
    }

    public static RequestRecords.MaxStockByBranchResponse toResponse(MaxStockByBranch x) {
        return new RequestRecords.MaxStockByBranchResponse(
                x.branchId(),
                x.branchName(),
                x.productId(),
                x.productName(),
                x.stock()
        );
    }
}
