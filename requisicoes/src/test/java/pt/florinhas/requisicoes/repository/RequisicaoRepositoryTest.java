package pt.florinhas.requisicoes.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RequisicaoRepositoryTest {

    @Test
    void methods_DeveExistir() {

        assertEquals(
                "findByEstado",
                "findByEstado");

        assertEquals(
                "findWithFilters",
                "findWithFilters");

        assertEquals(
                "findIdsPaginated",
                "findIdsPaginated");

        assertEquals(
                "findByIdsWithCriadoPor",
                "findByIdsWithCriadoPor");
    }
}