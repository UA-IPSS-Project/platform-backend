package pt.florinhas.marcacoes.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.RoupaDTO;
import pt.florinhas.marcacoes.service.CalendarioService;

@ExtendWith(MockitoExtension.class)
class MarcacaoValidatorTest {

        @Mock
        private CalendarioService calendarioService;
        private MarcacaoValidator validator;

        @BeforeEach
        void setup() {
                validator = new MarcacaoValidator(calendarioService);
        }

        @Test
        @DisplayName("Deve validar criação de marcação com sucesso")
        void validarCriacao_DeveAceitarRequestValido() {
                CriarMarcacaoRequest request = new CriarMarcacaoRequest();
                request.setAssunto("Pedido");
                request.setData(LocalDateTime.now().plusDays(1));

                while (request.getData().getDayOfWeek().getValue() >= 6) {
                        request.setData(request.getData().plusDays(1));
                }

                when(calendarioService.getFeriadosDoAno(anyInt())).thenReturn(List.of());
                when(calendarioService.isSlotBloqueado(any(), any(), any())).thenReturn(false);

                assertDoesNotThrow(() -> validator.validarCriacao(request));
        }

        @Test
        @DisplayName("Deve falhar quando o assunto é nulo ou vazio")
        void validarCriacao_DeveLancarExcecaoAssuntoInvalido() {
                CriarMarcacaoRequest request = new CriarMarcacaoRequest();
                request.setAssunto("");
                request.setData(LocalDateTime.now().plusDays(1));

                assertThrows(IllegalArgumentException.class, () -> validator.validarCriacao(request));
        }

        @Test
        @DisplayName("Deve validar roupas por categoria ou ID de item")
        void validarRoupas_DeveAceitarCategoriaOuItem() {
                RoupaDTO r1 = new RoupaDTO();
                r1.setCategoria("Meias");
                RoupaDTO r2 = new RoupaDTO();
                r2.setItemId(1L);

                assertDoesNotThrow(() -> validator.validarRoupas(List.of(r1, r2)));
        }

        @Test
        @DisplayName("Deve falhar quando roupa não tem categoria nem item")
        void validarRoupas_DeveLancarExcecaoQuandoAmbosNulos() {
                RoupaDTO r = new RoupaDTO();
                assertThrows(IllegalArgumentException.class, () -> validator.validarRoupas(List.of(r)));
        }
}