package co.com.bancolombia.mongo;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.mongo.document.BranchDocument;
import co.com.bancolombia.mongo.mapper.BranchMapper;
import co.com.bancolombia.mongo.repository.BranchRepository;
import co.com.bancolombia.mongo.helper.AdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MongoBranchAdapter extends AdapterOperations<BranchDTO, BranchDocument, String, BranchRepository> implements co.com.bancolombia.model.gateways.BranchRepository {

    public MongoBranchAdapter(BranchRepository repo, ObjectMapper objectMapper) {
        super(repo, objectMapper, d -> objectMapper.map(d, BranchDTO.class));
    }

    @Override
    public Mono<BranchDTO> save(BranchDTO branch) {
        return super.save(branch);
    }

    @Override
    public Mono<BranchDTO> findByIdAndFranchiseId(String branchId, String franchiseId) {
        return doQuery(repository.findByIdAndFranchiseId(branchId, franchiseId));
    }

    @Override
    public Flux<BranchDTO> findByFranchiseId(String franchiseId) {
        return doQueryMany(repository.findByFranchiseId(franchiseId));
    }

    @Override
    public Mono<BranchDTO> updateName(String branchId, String franchiseId, String newName) {
        return repository.findByIdAndFranchiseId(branchId, franchiseId)
                .flatMap(d -> { d.setName(newName); return repository.save(d); })
                .map(BranchMapper::toDomain);
    }
}