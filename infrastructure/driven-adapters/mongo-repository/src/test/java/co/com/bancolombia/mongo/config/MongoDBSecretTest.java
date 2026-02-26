package co.com.bancolombia.mongo.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoDBSecretTest {

    @Test
    void builderShouldSetUri() {
        String expectedUri = "mongodb://localhost:27017/testdb";
        MongoDBSecret secret = MongoDBSecret.builder()
                .uri(expectedUri)
                .build();

        assertEquals(expectedUri, secret.getUri());
    }
}