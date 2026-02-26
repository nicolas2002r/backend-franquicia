package co.com.bancolombia.usecase;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.MaxStockByBranchDTO;
import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.*;
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
            ProductRepository productRepository
    ) {
        this.createFranchise = new CreateFranchiseCommand(franchiseRepository);
        this.updateFranchiseName = new UpdateFranchiseNameCommand(franchiseRepository);

        this.addBranch = new AddBranchCommand(franchiseRepository, branchRepository);
        this.updateBranchName = new UpdateBranchNameCommand(branchRepository);

        this.addProduct = new AddProductCommand(branchRepository, productRepository);
        this.deleteProduct = new DeleteProductCommand(branchRepository, productRepository);
        this.updateProductStock = new UpdateProductStockCommand(branchRepository, productRepository);
        this.updateProductName = new UpdateProductNameCommand(branchRepository, productRepository);

        this.getMaxStockByBranch = new GetMaxStockByBranchQuery(franchiseRepository, branchRepository, productRepository);
    }

    public Mono<FranchiseDTO> createFranchise(String name) {
        return createFranchise.execute(name);
    }

    public Mono<FranchiseDTO> updateFranchiseName(String franchiseId, String newName) {
        return updateFranchiseName.execute(franchiseId, newName);
    }

    public Mono<BranchDTO> addBranch(String franchiseId, String branchName) {
        return addBranch.execute(franchiseId, branchName);
    }

    public Mono<BranchDTO> updateBranchName(String franchiseId, String branchId, String newName) {
        return updateBranchName.execute(franchiseId, branchId, newName);
    }

    public Mono<ProductDTO> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return addProduct.execute(franchiseId, branchId, productName, stock);
    }

    public Mono<Void> deleteProduct(String franchiseId, String branchId, String productId) {
        return deleteProduct.execute(franchiseId, branchId, productId);
    }

    public Mono<ProductDTO> updateProductStock(String franchiseId, String branchId, String productId, int newStock) {
        return updateProductStock.execute(franchiseId, branchId, productId, newStock);
    }

    public Mono<ProductDTO> updateProductName(String franchiseId, String branchId, String productId, String newName) {
        return updateProductName.execute(franchiseId, branchId, productId, newName);
    }

    public Flux<MaxStockByBranchDTO> getMaxStockByBranch(String franchiseId) {
        return getMaxStockByBranch.execute(franchiseId);
    }
}