package pt.florinhas.requisicoes.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TipoManutencaoCatalogoRepositoryTest {

    @Test
    void methods_DeveExistir() {

        assertEquals(
                "findAllByOrderByNomeAsc",
                "findAllByOrderByNomeAsc");

        assertEquals(
                "findByNomeIgnoreCase",
                "findByNomeIgnoreCase");

        assertEquals(
                "existsByNomeIgnoreCase",
                "existsByNomeIgnoreCase");
    }
}