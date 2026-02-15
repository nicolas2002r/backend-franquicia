package co.com.bancolombia.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RequestRecords {

    public record CreateNameRequest(@NotBlank String name) {}

    public record CreateProductRequest(
            @NotBlank String name,
            @Min(0) int stock
    ) {}

    public record UpdateStockRequest(@Min(0) int stock) {}

    public record FranchiseResponse(String id, String name) {}

    public record BranchResponse(String id, String franchiseId, String name) {}

    public record ProductResponse(String id, String branchId, String name, int stock) {}

    public record MaxStockByBranchResponse(
            String branchId,
            String branchName,
            String productId,
            String productName,
            int stock
    ) {}


}
