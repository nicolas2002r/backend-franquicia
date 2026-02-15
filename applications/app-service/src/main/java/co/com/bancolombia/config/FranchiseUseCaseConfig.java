package co.com.bancolombia.config;


import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.FranchiseUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FranchiseUseCaseConfig {

    @Bean
    public FranchiseUseCase franchiseUseCase(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            AuditLogger auditLogger
    ) {
        return new FranchiseUseCase(
                franchiseRepository,
                branchRepository,
                productRepository,
                auditLogger
        );
    }
}
