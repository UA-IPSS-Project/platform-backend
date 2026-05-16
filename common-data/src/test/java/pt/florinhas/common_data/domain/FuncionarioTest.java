package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class FuncionarioTest {

    @Test
    void getAuthorities_DeveRetornarRoleFuncionarioQuandoTipoNull() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setTipo(null);

        Collection<? extends GrantedAuthority> authorities =
                funcionario.getAuthorities();

        assertEquals(
                1,
                authorities.size());

        assertTrue(
                authorities.stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_FUNCIONARIO")));
    }

    @Test
    void getAuthorities_DeveRetornarRoleDoTipoEFuncionario() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setTipo(
                FuncionarioTipo.SECRETARIA);

        Collection<? extends GrantedAuthority> authorities =
                funcionario.getAuthorities();

        assertEquals(
                2,
                authorities.size());

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
    void gettersSetters_DeveFuncionar() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setActivo(true);

        funcionario.setTipo(
                FuncionarioTipo.BALNEARIO);

        assertTrue(
                funcionario.isActivo());

        assertEquals(
                FuncionarioTipo.BALNEARIO,
                funcionario.getTipo());
    }
}