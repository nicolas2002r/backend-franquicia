package co.com.bancolombia.api.route;

import co.com.bancolombia.api.handler.FranchiseFunctionalHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FranchiseRouter {

    private static final String BASE = "/api/v1";

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseFunctionalHandler handler) {
        return route()
                // Franquicias
                .POST(BASE + "/franchises", accept(MediaType.APPLICATION_JSON), handler::createFranchise)
                .PATCH(BASE + "/franchises/{franchiseId}/name", accept(MediaType.APPLICATION_JSON), handler::renameFranchise)

                // Sucursales
                .POST(BASE + "/franchises/{franchiseId}/branches", accept(MediaType.APPLICATION_JSON), handler::addBranch)
                .PATCH(BASE + "/franchises/{franchiseId}/branches/{branchId}/name", accept(MediaType.APPLICATION_JSON), handler::renameBranch)

                // Productos
                .POST(BASE + "/franchises/{franchiseId}/branches/{branchId}/products", accept(MediaType.APPLICATION_JSON), handler::addProduct)
                .DELETE(BASE + "/franchises/{franchiseId}/branches/{branchId}/products/{productId}", handler::deleteProduct)
                .PATCH(BASE + "/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock", accept(MediaType.APPLICATION_JSON), handler::updateStock)
                .PATCH(BASE + "/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name", accept(MediaType.APPLICATION_JSON), handler::renameProduct)

                // Max stock
                .GET(BASE + "/franchises/{franchiseId}/branches/max-stock-products", handler::maxStock)

                .build();
    }
}
