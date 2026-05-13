package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import pt.florinhas.common_data.TestJpaConfig;
import pt.florinhas.common_data.domain.Valencia;

@DataJpaTest
@ContextConfiguration(classes = TestJpaConfig.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class ValenciaRepositoryTest {

    @Autowired
    private ValenciaRepository repository;

    @Test
    void findByNome_DeveRetornarValencia() {

        Valencia valencia =
                new Valencia();

        valencia.setNome("Balneario");

        repository.save(valencia);

        Optional<Valencia> result =
                repository.findByNome(
                        "Balneario"
                );

        assertTrue(result.isPresent());
    }

    @Test
    void existsByNome_DeveRetornarTrue() {

        Valencia valencia =
                new Valencia();

        valencia.setNome("Secretaria");

        repository.save(valencia);

        assertTrue(
                repository.existsByNome(
                        "Secretaria"
                )
        );
    }
}