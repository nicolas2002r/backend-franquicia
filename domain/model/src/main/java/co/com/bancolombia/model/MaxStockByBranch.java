package co.com.bancolombia.model;

public record MaxStockByBranch(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock
) {}