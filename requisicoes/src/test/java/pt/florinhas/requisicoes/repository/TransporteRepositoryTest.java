package pt.florinhas.requisicoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TransporteRepositoryTest {

    @Test
    void interface_DeveExistir() {

        assertTrue(
                TransporteRepository.class.isInterface());
    }
}