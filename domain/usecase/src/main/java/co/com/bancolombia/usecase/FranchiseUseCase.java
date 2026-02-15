package co.com.bancolombia.usecase;


import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.MaxStockByBranch;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.*;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FranchiseUseCase {

    private final CreateFranchiseCommand createFranchise;
    private final UpdateFranchiseNameCommand updateFranchiseName;
    private final AddBranchCommand addBranch;
    private final UpdateBranchNameCommand updateBranchName;
    private final AddProductCommand addProduct;
    private final DeleteProductCommand deleteProduct;
    private final UpdateProductStockCommand updateProductStock;
    private final UpdateProductNameCommand updateProductName;
    private final GetMaxStockByBranchQuery getMaxStockByBranch;

    public FranchiseUseCase(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            AuditLogger auditLogger
    ) {
        AuditSupport audit = new AuditSupport(auditLogger);

        this.createFranchise = new CreateFranchiseCommand(franchiseRepository, audit);
        this.updateFranchiseName = new UpdateFranchiseNameCommand(franchiseRepository, audit);
        this.addBranch = new AddBranchCommand(franchiseRepository, branchRepository, audit);
        this.updateBranchName = new UpdateBranchNameCommand(branchRepository, audit);
        this.addProduct = new AddProductCommand(branchRepository, productRepository, audit);
        this.deleteProduct = new DeleteProductCommand(branchRepository, productRepository, audit);
        this.updateProductStock = new UpdateProductStockCommand(branchRepository, productRepository, audit);
        this.updateProductName = new UpdateProductNameCommand(branchRepository, productRepository, audit);
        this.getMaxStockByBranch = new GetMaxStockByBranchQuery(franchiseRepository, branchRepository, productRepository, audit);
    }

    public Mono<Franchise> createFranchise(String name) {
        return createFranchise.execute(name);
    }

    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return updateFranchiseName.execute(franchiseId, newName);
    }

    public Mono<Branch> addBranch(String franchiseId, String branchName) {
        return addBranch.execute(franchiseId, branchName);
    }

    public Mono<Branch> updateBranchName(String franchiseId, String branchId, String newName) {
        return updateBranchName.execute(franchiseId, branchId, newName);
    }

    public Mono<Product> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return addProduct.execute(franchiseId, branchId, productName, stock);
    }

    public Mono<Void> deleteProduct(String franchiseId, String branchId, String productId) {
        return deleteProduct.execute(franchiseId, branchId, productId);
    }

    public Mono<Product> updateProductStock(String franchiseId, String branchId, String productId, int newStock) {
        return updateProductStock.execute(franchiseId, branchId, productId, newStock);
    }

    public Mono<Product> updateProductName(String franchiseId, String branchId, String productId, String newName) {
        return updateProductName.execute(franchiseId, branchId, productId, newName);
    }

    public Flux<MaxStockByBranch> getMaxStockByBranch(String franchiseId) {
        return getMaxStockByBranch.execute(franchiseId);
    }
}
