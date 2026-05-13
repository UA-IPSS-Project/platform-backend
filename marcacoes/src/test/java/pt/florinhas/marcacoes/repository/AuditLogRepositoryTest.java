package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class AuditLogRepositoryTest {

    @Test
    void classeDeveExistir() {

        assertNotNull(AuditLogRepository.class);
    }

    @Test
    void deveTerMetodoFindAllByOrderByTimestampDesc() throws Exception {

        var method =
                AuditLogRepository.class.getMethod(
                        "findAllByOrderByTimestampDesc",
                        Pageable.class
                );

        assertEquals(
                Page.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindWithFilters() throws Exception {

        var method =
                AuditLogRepository.class.getMethod(
                        "findWithFilters",
                        Long.class,
                        String.class,
                        String.class,
                        LocalDateTime.class,
                        LocalDateTime.class,
                        Pageable.class
                );

        assertEquals(
                Page.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindByEntityTypeAndEntityIdOrderByTimestampDesc() throws Exception {

        var method =
                AuditLogRepository.class.getMethod(
                        "findByEntityTypeAndEntityIdOrderByTimestampDesc",
                        String.class,
                        Long.class
                );

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }
}