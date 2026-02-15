package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.mapper.FranchiseMapper;
import co.com.bancolombia.api.model.RequestRecords;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.usecase.FranchiseUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FranchiseFunctionalHandler {

    private final FranchiseUseCase useCase;
    private final Validator validator;


    public FranchiseFunctionalHandler(FranchiseUseCase useCase, Validator validator) {
        this.useCase = useCase;
        this.validator = validator;
    }

    private <T> Mono<T> readAndValidate(ServerRequest request, Class<T> clazz) {
        return request.bodyToMono(clazz)
                .flatMap(this::validate);
    }

    private <T> Mono<T> validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (violations == null || violations.isEmpty()) return Mono.just(dto);

        String msg = violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));
        return Mono.error(new ValidationException(msg));
    }

    // Endpoints
    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return readAndValidate(request, RequestRecords.CreateNameRequest.class)
                .flatMap(r -> useCase.createFranchise(r.name()))
                .map(FranchiseMapper::toResponse)
                .flatMap(body -> ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }

    public Mono<ServerResponse> renameFranchise(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return readAndValidate(request, RequestRecords.CreateNameRequest.class)
                .flatMap(r -> useCase.updateFranchiseName(franchiseId, r.name()))
                .map(FranchiseMapper::toResponse)
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return readAndValidate(request, RequestRecords.CreateNameRequest.class)
                .flatMap(r -> useCase.addBranch(franchiseId, r.name()))
                .map(FranchiseMapper::toResponse)
                .flatMap(body -> ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }

    public Mono<ServerResponse> renameBranch(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");

        return readAndValidate(request, RequestRecords.CreateNameRequest.class)
                .flatMap(r -> useCase.updateBranchName(franchiseId, branchId, r.name()))
                .map(FranchiseMapper::toResponse)
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");

        return readAndValidate(request, RequestRecords.CreateProductRequest.class)
                .flatMap(r -> useCase.addProduct(franchiseId, branchId, r.name(), r.stock()))
                .map(FranchiseMapper::toResponse)
                .flatMap(body -> ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");

        return useCase.deleteProduct(franchiseId, branchId, productId)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");

        return readAndValidate(request, RequestRecords.UpdateStockRequest.class)
                .flatMap(r -> useCase.updateProductStock(franchiseId, branchId, productId, r.stock()))
                .map(FranchiseMapper::toResponse)
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }

    public Mono<ServerResponse> renameProduct(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");

        return readAndValidate(request, RequestRecords.CreateNameRequest.class)
                .flatMap(r -> useCase.updateProductName(franchiseId, branchId, productId, r.name()))
                .map(FranchiseMapper::toResponse)
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }

    public Mono<ServerResponse> maxStock(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(useCase.getMaxStockByBranch(franchiseId).map(FranchiseMapper::toResponse),
                        RequestRecords.MaxStockByBranchResponse.class);
    }
}


