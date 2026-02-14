package co.com.bancolombia.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductDocument {
    @Id
    private String id;
    private String branchId;
    private String name;
    private int stock;
}
