package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import pt.florinhas.common_data.security.CryptoUtils;

class UtilizadorTest {

    @Test
    void getAuthorities_DeveRetornarRoleUtente() {

        Utilizador utilizador =
                new Utilizador();

        Collection<? extends GrantedAuthority> authorities =
                utilizador.getAuthorities();

        assertEquals(
                1,
                authorities.size());

        assertEquals(
                "ROLE_UTENTE",
                authorities.iterator()
                        .next()
                        .getAuthority());
    }

    @Test
    void getAuthorities_DeveRetornarRoleFuncionario() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setTipo(
                FuncionarioTipo.SECRETARIA);

        Collection<? extends GrantedAuthority> authorities =
                funcionario.getAuthorities();

        assertTrue(
                authorities.stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_SECRETARIA")));
    }

    @Test
    void getPassword_DeveRetornarPassHash() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setPassHash("hash");

        assertEquals(
                "hash",
                utilizador.getPassword());
    }

    @Test
    void getUsername_DeveRetornarEmail() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setEmail(
                "teste@email.com");

        assertEquals(
                "teste@email.com",
                utilizador.getUsername());
    }

    @Test
    void accountFlags_DeveRetornarTrue() {

        Utilizador utilizador =
                new Utilizador();

        assertTrue(
                utilizador.isAccountNonExpired());

        assertTrue(
                utilizador.isAccountNonLocked());

        assertTrue(
                utilizador.isCredentialsNonExpired());

        assertTrue(
                utilizador.isEnabled());
    }

    @Test
    void onCreate_DeveDefinirCreatedAt()
            throws Exception {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNif("123456789");

        CryptoUtils cryptoUtils =
                org.mockito.Mockito.mock(
                        CryptoUtils.class);

        org.mockito.Mockito.when(
                cryptoUtils.generateBlindIndex(
                        "123456789"))
                .thenReturn("hash");

        Utilizador.setCryptoUtils(
                cryptoUtils);

        Method method =
                Utilizador.class.getDeclaredMethod(
                        "onCreate");

        method.setAccessible(true);

        method.invoke(utilizador);

        assertEquals(
                "hash",
                utilizador.getNifHash());

        assertTrue(
                utilizador.getCreatedAt()
                        .isBefore(
                                LocalDateTime.now()
                                        .plusSeconds(1)));
    }

    @Test
    void onUpdate_DeveAtualizarNifHash()
            throws Exception {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNif("987654321");

        CryptoUtils cryptoUtils =
                org.mockito.Mockito.mock(
                        CryptoUtils.class);

        org.mockito.Mockito.when(
                cryptoUtils.generateBlindIndex(
                        "987654321"))
                .thenReturn("novoHash");

        Utilizador.setCryptoUtils(
                cryptoUtils);

        Method method =
                Utilizador.class.getDeclaredMethod(
                        "onUpdate");

        method.setAccessible(true);

        method.invoke(utilizador);

        assertEquals(
                "novoHash",
                utilizador.getNifHash());
    }

    @Test
    void updateNifHash_DeveLancarExcecaoQuandoCryptoUtilsNull()
            throws Exception {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNif("123456789");

        Utilizador.setCryptoUtils(null);

        Method method =
                Utilizador.class.getDeclaredMethod(
                        "onCreate");

        method.setAccessible(true);

        assertThrows(
                Exception.class,
                () -> method.invoke(utilizador));
    }
}