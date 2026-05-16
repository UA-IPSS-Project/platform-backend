package pt.florinhas.requisicoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoMaterialRepositoryTest {

    @Test
    void interface_DeveExistir() {

        assertTrue(
                RequisicaoMaterialRepository.class.isInterface());
    }
}