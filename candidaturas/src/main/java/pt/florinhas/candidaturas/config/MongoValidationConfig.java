package pt.florinhas.candidaturas.config;

// Spring
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// MongoDB
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;

// Validation
import jakarta.validation.Validator;

@Configuration
public class MongoValidationConfig {

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(Validator validator) {
        return new ValidatingMongoEventListener(validator);
    }
}
