package co.com.bancolombia.usecase;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.MaxStockByBranch;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final AuditLogger auditLogger;

    public static final String NO_SUCURSAL = "No se encontró sucursal para franquicia";
    public static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado en la sucursal";
    public static final String FRANQUICIA_NO_ENCONTRADA = "Franquicia no encontrada: ";

    public FranchiseUseCase(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            AuditLogger auditLogger
    ) {
        this.franchiseRepository = franchiseRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.auditLogger = auditLogger;
    }

    // 1) crear franquicia
    public Mono<Franchise> createFranchise(String name) {
        return Mono.just(name)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .switchIfEmpty(Mono.error(new ValidationException("Franchise name is required")))
                .flatMap(n -> franchiseRepository.existsByName(n)
                        .flatMap(exists -> exists
                                ? Mono.error(new ConflictException("Franchise name already exists"))
                                : Mono.just(n)))
                .flatMap(n -> {
                    Mono<Franchise> save = franchiseRepository.save(new Franchise(null, n));
                    Mono<Void> audit = auditLogger.info("AUDIT createFranchise name=" + n);

                    return Flux.merge(
                                    save,
                                    audit.then(Mono.empty())
                            )
                            .next()
                            .cast(Franchise.class);
                })
                .doOnNext(f -> auditLogger.info("onNext createFranchise id=" + f.id()).subscribe())
                .doOnError(e -> auditLogger.error("onError createFranchise", e).subscribe())
                .doFinally(sig -> auditLogger.info("onComplete createFranchise signal=" + sig).subscribe());
    }

    //renombrar franquicia
    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return Mono.just(newName)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .switchIfEmpty(Mono.error(new ValidationException("Franchise name is required")))
                .flatMap(n -> franchiseRepository.existsByName(n)
                        .flatMap(exists -> exists
                                ? Mono.error(new ConflictException("Franchise name already exists"))
                                : Mono.just(n)))
                .flatMap(n -> franchiseRepository.updateName(franchiseId, n)
                        .switchIfEmpty(Mono.error(new NotFoundException(FRANQUICIA_NO_ENCONTRADA + franchiseId))))
                .doOnNext(f -> auditLogger.info("onNext updateFranchiseName id=" + f.id()).subscribe())
                .doOnError(e -> auditLogger.error("onError updateFranchiseName", e).subscribe())
                .doFinally(sig -> auditLogger.info("onComplete updateFranchiseName signal=" + sig).subscribe());
    }

    //agregar sucursal
    public Mono<Branch> addBranch(String franchiseId, String branchName) {
        return Mono.just(branchName)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .switchIfEmpty(Mono.error(new ValidationException("Branch name is required")))
                .flatMap(n -> franchiseRepository.findById(franchiseId)
                        .switchIfEmpty(Mono.error(new NotFoundException(FRANQUICIA_NO_ENCONTRADA + franchiseId)))
                        .thenReturn(n))
                .flatMap(n -> branchRepository.save(new Branch(null, franchiseId, n)))
                .doOnNext(b -> auditLogger.info("onNext addBranch id=" + b.id()).subscribe())
                .doOnError(e -> auditLogger.error("onError addBranch", e).subscribe())
                .doFinally(sig -> auditLogger.info("onComplete addBranch signal=" + sig).subscribe());
    }

    //renombrar sucursal
    public Mono<Branch> updateBranchName(String franchiseId, String branchId, String newName) {
        return Mono.just(newName)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .switchIfEmpty(Mono.error(new ValidationException("Branch name is required")))
                .flatMap(n -> branchRepository.updateName(branchId, franchiseId, n)
                        .switchIfEmpty(Mono.error(new NotFoundException(NO_SUCURSAL))))
                .doOnNext(b -> auditLogger.info("onNext updateBranchName id=" + b.id()).subscribe())
                .doOnError(e -> auditLogger.error("onError updateBranchName", e).subscribe())
                .doFinally(sig -> auditLogger.info("onComplete updateBranchName signal=" + sig).subscribe());
    }

    //agregar producto
    public Mono<Product> addProduct(String franchiseId, String branchId, String productName, int stock) {
        if (stock < 0) return Mono.error(new ValidationException("Stock cannot be negative"));

        return Mono.just(productName)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .switchIfEmpty(Mono.error(new ValidationException("Product name is required")))
                .flatMap(n -> branchRepository.findByIdAndFranchiseId(branchId, franchiseId)
                        .switchIfEmpty(Mono.error(new NotFoundException(NO_SUCURSAL)))
                        .thenReturn(n))
                .flatMap(n -> productRepository.save(new Product(null, branchId, n, stock)))
                .doOnNext(p -> auditLogger.info("onNext addProduct id=" + p.id()).subscribe())
                .doOnError(e -> auditLogger.error("onError addProduct", e).subscribe())
                .doFinally(sig -> auditLogger.info("onComplete addProduct signal=" + sig).subscribe());
    }

    //eliminar producto
    public Mono<Void> deleteProduct(String franchiseId, String branchId, String productId) {
        return branchRepository.findByIdAndFranchiseId(branchId, franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException(NO_SUCURSAL)))
                .flatMap(b -> productRepository.findByIdAndBranchId(productId, branchId)
                        .switchIfEmpty(Mono.error(new NotFoundException(PRODUCTO_NO_ENCONTRADO))))
                .flatMap(p -> productRepository.deleteByIdAndBranchId(productId, branchId))
                .doOnSuccess(v -> auditLogger.info("onComplete deleteProduct productId=" + productId).subscribe())
                .doOnError(e -> auditLogger.error("onError deleteProduct", e).subscribe());
    }

    //modificar stock
    public Mono<Product> updateProductStock(String franchiseId, String branchId, String productId, int newStock) {
        if (newStock < 0) return Mono.error(new ValidationException("Stock cannot be negative"));

        return branchRepository.findByIdAndFranchiseId(branchId, franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException(NO_SUCURSAL)))
                .flatMap(b -> productRepository.findByIdAndBranchId(productId, branchId)
                        .switchIfEmpty(Mono.error(new NotFoundException(PRODUCTO_NO_ENCONTRADO))))
                .map(p -> new Product(p.id(), p.branchId(), p.name(), newStock))
                .flatMap(productRepository::save)
                .doOnNext(p -> auditLogger.info("onNext updateProductStock productId=" + p.id() + " stock=" + p.stock()).subscribe())
                .doOnError(e -> auditLogger.error("onError updateProductStock", e).subscribe())
                .doFinally(sig -> auditLogger.info("onComplete updateProductStock signal=" + sig).subscribe());
    }

    //renombrar producto
    public Mono<Product> updateProductName(String franchiseId, String branchId, String productId, String newName) {
        return Mono.just(newName)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .switchIfEmpty(Mono.error(new ValidationException("Product name is required")))
                .flatMap(n -> branchRepository.findByIdAndFranchiseId(branchId, franchiseId)
                        .switchIfEmpty(Mono.error(new NotFoundException(NO_SUCURSAL)))
                        .thenReturn(n))
                .flatMap(n -> productRepository.findByIdAndBranchId(productId, branchId)
                        .switchIfEmpty(Mono.error(new NotFoundException(PRODUCTO_NO_ENCONTRADO)))
                        .map(p -> new Product(p.id(), p.branchId(), n, p.stock())))
                .flatMap(productRepository::save)
                .doOnNext(p -> auditLogger.info("onNext updateProductName productId=" + p.id() + " name=" + p.name()).subscribe())
                .doOnError(e -> auditLogger.error("onError updateProductName", e).subscribe())
                .doFinally(sig -> auditLogger.info("onComplete updateProductName signal=" + sig).subscribe());
    }

    // máximo stock por sucursal
    public Flux<MaxStockByBranch> getMaxStockByBranch(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException(FRANQUICIA_NO_ENCONTRADA + franchiseId)))
                .flatMapMany(f -> branchRepository.findByFranchiseId(franchiseId))
                .switchIfEmpty(Flux.error(new NotFoundException("No branches for franchise: " + franchiseId)))
                .flatMap(branch ->
                        Mono.zip(
                                Mono.just(branch),
                                productRepository.findTopByBranchIdOrderByStockDesc(branch.id())
                                        .switchIfEmpty(Mono.error(new NotFoundException("Branch has no products: " + branch.id())))
                        ).map(t -> new MaxStockByBranch(
                                t.getT1().id(),
                                t.getT1().name(),
                                t.getT2().id(),
                                t.getT2().name(),
                                t.getT2().stock()
                        ))
                )
                .doOnNext(x -> auditLogger.info("onNext maxStock branchId=" + x.branchId() + " productId=" + x.productId()).subscribe())
                .doOnError(e -> auditLogger.error("onError maxStock", e).subscribe())
                .doFinally(sig -> auditLogger.info("onComplete maxStock signal=" + sig).subscribe());
    }
}
