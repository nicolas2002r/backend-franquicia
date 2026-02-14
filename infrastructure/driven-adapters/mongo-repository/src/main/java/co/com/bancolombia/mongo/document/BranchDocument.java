package co.com.bancolombia.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("branches")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BranchDocument {
    @Id private String id;
    private String franchiseId;
    private String name;

}
