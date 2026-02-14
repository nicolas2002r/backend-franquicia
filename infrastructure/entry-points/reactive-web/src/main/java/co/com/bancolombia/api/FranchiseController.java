package co.com.bancolombia.api;


import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.MaxStockByBranch;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.usecase.FranchiseUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@Validated
public class FranchiseController {

    private final FranchiseUseCase useCase;

    public FranchiseController(FranchiseUseCase useCase) {
        this.useCase = useCase;
    }

    public record CreateNameRequest(String name) {
    }

    public record CreateProductRequest(String name, int stock) {
    }

    public record UpdateStockRequest(int stock) {
    }

    //crear franquicia
    @PostMapping("/franchises")
    public Mono<ResponseEntity<Franchise>> createFranchise(@RequestBody Mono<CreateNameRequest> req) {
        return req.flatMap(r -> useCase.createFranchise(r.name()))
                .map(f -> ResponseEntity.status(HttpStatus.CREATED).body(f));
    }

    // renombrar franquicia
    @PatchMapping("/franchises/{franchiseId}/name")
    public Mono<ResponseEntity<Franchise>> renameFranchise(
            @PathVariable("franchiseId") String franchiseId,
            @RequestBody Mono<CreateNameRequest> req
    ) {
        return req.flatMap(r -> useCase.updateFranchiseName(franchiseId, r.name()))
                .map(ResponseEntity::ok);
    }

    //agregar sucursal
    @PostMapping("/franchises/{franchiseId}/branches")
    public Mono<ResponseEntity<Branch>> addBranch(
            @PathVariable("franchiseId") String franchiseId,
            @RequestBody Mono<CreateNameRequest> req
    ) {
        return req.flatMap(r -> useCase.addBranch(franchiseId, r.name()))
                .map(b -> ResponseEntity.status(HttpStatus.CREATED).body(b));
    }

    //renombrar sucursal
    @PatchMapping("/franchises/{franchiseId}/branches/{branchId}/name")
    public Mono<ResponseEntity<Branch>> renameBranch(
            @PathVariable("franchiseId") String franchiseId,
            @PathVariable("branchId") String branchId,
            @RequestBody Mono<CreateNameRequest> req
    ) {
        return req.flatMap(r -> useCase.updateBranchName(franchiseId, branchId, r.name()))
                .map(ResponseEntity::ok);
    }

    //agregar producto
    @PostMapping("/franchises/{franchiseId}/branches/{branchId}/products")
    public Mono<ResponseEntity<Product>> addProduct(
            @PathVariable("franchiseId") String franchiseId,
            @PathVariable("branchId") String branchId,
            @RequestBody Mono<CreateProductRequest> req
    ) {
        return req.flatMap(r -> useCase.addProduct(franchiseId, branchId, r.name(), r.stock()))
                .map(p -> ResponseEntity.status(HttpStatus.CREATED).body(p));
    }

    //eliminar producto
    @DeleteMapping("/franchises/{franchiseId}/branches/{branchId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProduct(
            @PathVariable("franchiseId") String franchiseId,
            @PathVariable("branchId") String branchId,
            @PathVariable("productId") String productId
    ) {
        return useCase.deleteProduct(franchiseId, branchId, productId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    //actualizar stock
    @PatchMapping("/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock")
    public Mono<ResponseEntity<Product>> updateStock(
            @PathVariable("franchiseId") String franchiseId,
            @PathVariable("branchId") String branchId,
            @PathVariable("productId") String productId,
            @RequestBody Mono<UpdateStockRequest> req
    ) {
        return req.flatMap(r -> useCase.updateProductStock(franchiseId, branchId, productId, r.stock()))
                .map(ResponseEntity::ok);
    }

    //renombrar producto
    @PatchMapping("/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name")
    public Mono<ResponseEntity<Product>> renameProduct(
            @PathVariable("franchiseId") String franchiseId,
            @PathVariable("branchId") String branchId,
            @PathVariable("productId") String productId,
            @RequestBody Mono<CreateNameRequest> req
    ) {
        return req.flatMap(r -> useCase.updateProductName(franchiseId, branchId, productId, r.name()))
                .map(ResponseEntity::ok);
    }

    //max stock por sucursal
    @GetMapping("/franchises/{franchiseId}/branches/max-stock-products")
    public Flux<MaxStockByBranch> maxStock(@PathVariable("franchiseId") String franchiseId) {
        return useCase.getMaxStockByBranch(franchiseId);
    }
}

