package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class FuncionarioTest {

    @Test
    void getAuthorities_DeveRetornarRoleFuncionario() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setTipo(
                FuncionarioTipo.SECRETARIA);

        Collection<? extends GrantedAuthority> authorities =
                funcionario.getAuthorities();

        assertEquals(2, authorities.size());

        assertTrue(
                authorities.stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_SECRETARIA")));

        assertTrue(
                authorities.stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_FUNCIONARIO")));
    }

    @Test
    void getAuthorities_DeveRetornarRoleDefault() {

        Funcionario funcionario =
                new Funcionario();

        Collection<? extends GrantedAuthority> authorities =
                funcionario.getAuthorities();

        assertEquals(1, authorities.size());

        assertEquals(
                "ROLE_FUNCIONARIO",
                authorities.iterator()
                        .next()
                        .getAuthority());
    }
}