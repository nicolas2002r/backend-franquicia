package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.model.RequestRecords;
import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.MaxStockByBranchDTO;
import co.com.bancolombia.model.ProductDTO;

public class FranchiseMapper {

    private FranchiseMapper() {}

    // Mapeo de FranchiseDTO a FranchiseResponse
    public static RequestRecords.FranchiseResponse toResponse(FranchiseDTO franchise) {
        return new RequestRecords.FranchiseResponse(franchise.getId(), franchise.getName());
    }

    // Mapeo de BranchDTO a BranchResponse
    public static RequestRecords.BranchResponse toResponse(BranchDTO branch) {
        return new RequestRecords.BranchResponse(branch.getId(), branch.getFranchiseId(), branch.getName());
    }

    // Mapeo de ProductDTO a ProductResponse
    public static RequestRecords.ProductResponse toResponse(ProductDTO product) {
        return new RequestRecords.ProductResponse(product.getId(), product.getBranchId(), product.getName(), product.getStock());
    }

    // Mapeo de MaxStockByBranchDTO a MaxStockByBranchResponse
    public static RequestRecords.MaxStockByBranchResponse toResponse(MaxStockByBranchDTO maxStock) {
        return new RequestRecords.MaxStockByBranchResponse(
                maxStock.getBranchId(),
                maxStock.getBranchName(),
                maxStock.getProductId(),
                maxStock.getProductName(),
                maxStock.getStock()
        );
    }
}