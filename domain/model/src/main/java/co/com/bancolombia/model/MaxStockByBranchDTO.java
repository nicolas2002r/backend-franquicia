package co.com.bancolombia.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaxStockByBranchDTO {
    private String branchId;
    private String branchName;
    private String productId;
    private String productName;
    private int stock;
}