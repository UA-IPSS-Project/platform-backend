package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import pt.florinhas.common_data.TestCryptoConfig;
import pt.florinhas.common_data.TestJpaConfig;
import pt.florinhas.common_data.domain.Notificacao;
import pt.florinhas.common_data.domain.Utilizador;

@DataJpaTest
@ContextConfiguration(classes = TestJpaConfig.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class NotificacaoRepositoryTest {

    @Autowired
    private NotificacaoRepository repository;

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    @BeforeEach
    void setUp() {

        TestCryptoConfig.initCrypto();
    }

    @Test
    void countByUtilizadorIdAndLidaFalse_DeveContar() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNome("Teste");
        utilizador.setNif("123456789");

        utilizador =
                utilizadorRepository.save(
                        utilizador
                );

        Notificacao notificacao =
                new Notificacao();

        notificacao.setUtilizador(utilizador);
        notificacao.setTitulo("Titulo");
        notificacao.setMensagem("Mensagem");
        notificacao.setTipo("INFO");
        notificacao.setLida(false);

        repository.save(notificacao);

        long total =
                repository.countByUtilizadorIdAndLidaFalse(
                        utilizador.getId()
                );

        assertEquals(1, total);
    }

    @Test
    void findByUtilizadorIdOrderByDataCriacaoDesc_DeveRetornarLista() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNome("Teste");
        utilizador.setNif("987654321");

        utilizador =
                utilizadorRepository.save(
                        utilizador
                );

        Notificacao notificacao =
                new Notificacao();

        notificacao.setUtilizador(utilizador);
        notificacao.setTitulo("Titulo");
        notificacao.setMensagem("Mensagem");
        notificacao.setTipo("INFO");

        repository.save(notificacao);

        List<Notificacao> result =
                repository.findByUtilizadorIdOrderByDataCriacaoDesc(
                        utilizador.getId()
                );

        assertFalse(result.isEmpty());
    }
}