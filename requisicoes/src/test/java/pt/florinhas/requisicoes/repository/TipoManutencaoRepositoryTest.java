package pt.florinhas.requisicoes.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TipoManutencaoRepositoryTest {

    @Test
    void methods_DeveExistir() {

        assertEquals(
                "findAllByOrderByNomeAsc",
                "findAllByOrderByNomeAsc");

        assertEquals(
                "findByNomeIgnoreCase",
                "findByNomeIgnoreCase");
    }
}