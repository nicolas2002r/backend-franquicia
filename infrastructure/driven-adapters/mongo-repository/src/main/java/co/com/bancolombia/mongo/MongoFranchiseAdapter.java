package co.com.bancolombia.mongo;

import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.mongo.document.FranchiseDocument;
import co.com.bancolombia.mongo.mapper.FranchiseMapper;
import co.com.bancolombia.mongo.repository.ReactiveFranchiseMongoRepository;
import co.com.bancolombia.mongo.helper.AdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MongoFranchiseAdapter extends AdapterOperations<FranchiseDTO, FranchiseDocument, String, ReactiveFranchiseMongoRepository> implements FranchiseRepository {

    public MongoFranchiseAdapter(ReactiveFranchiseMongoRepository repo, ObjectMapper objectMapper) {
        super(repo, objectMapper,d -> objectMapper.map(d, FranchiseDTO.class));
    }

    @Override
    public Mono<FranchiseDTO> save(FranchiseDTO franchise) {
        return super.save(franchise);
    }

    @Override
    public Mono<FranchiseDTO> findById(String id) {
        return super.findById(id);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Mono<FranchiseDTO> updateName(String id, String newName) {
        return repository.findById(id)
                .flatMap(d -> { d.setName(newName); return repository.save(d); })
                .map(FranchiseMapper::toDomain);
    }
}