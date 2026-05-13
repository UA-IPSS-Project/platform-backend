package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;

class CustomUserDetailsServiceTest {

    private UtilizadorRepository utilizadorRepository;

    private CryptoUtils cryptoUtils;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {

        utilizadorRepository =
                mock(UtilizadorRepository.class);

        cryptoUtils =
                mock(CryptoUtils.class);

        service =
                new CustomUserDetailsService(
                        utilizadorRepository,
                        cryptoUtils
                );
    }

    @Test
    @DisplayName("Deve carregar utilizador por email")
    void loadUserByUsername_DeveCarregarPorEmail() {

        Funcionario funcionario =
                mock(Funcionario.class);

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(funcionario));

        UserDetails result =
                service.loadUserByUsername("teste@test.com");

        assertEquals(funcionario, result);
    }

    @Test
    @DisplayName("Deve carregar utilizador por nif")
    void loadUserByUsername_DeveCarregarPorNif() {

        Funcionario funcionario =
                mock(Funcionario.class);

        when(utilizadorRepository.findByEmail("123456789"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(funcionario));

        UserDetails result =
                service.loadUserByUsername("123456789");

        assertEquals(funcionario, result);
    }

    @Test
    @DisplayName("Deve lançar exceção quando username é null")
    void loadUserByUsername_DeveLancarExceptionQuandoNull() {

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(null)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando utilizador não existe")
    void loadUserByUsername_DeveLancarExceptionQuandoNaoExiste() {

        when(utilizadorRepository.findByEmail("x"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("x"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("x")
        );
    }
}