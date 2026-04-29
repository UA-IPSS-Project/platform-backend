package pt.florinhas.candidaturas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Import;

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;

@Profile("dev")
@Configuration
@Import(EmbeddedMongoAutoConfiguration.class)
public class EmbeddedMongoDevConfig {

}