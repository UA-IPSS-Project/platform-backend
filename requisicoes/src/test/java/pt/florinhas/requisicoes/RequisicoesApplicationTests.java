package pt.florinhas.requisicoes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pt.florinhas.requisicoes.security.JwtService;

@SpringBootTest
class RequisicoesApplicationTests {

	@MockitoBean
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

}
