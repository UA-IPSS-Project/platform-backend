package pt.florinhas.requisicoes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pt.florinhas.requisicoes.security.JwtService;

@SpringBootTest
class RequisicoesApplicationTests {

	@MockBean
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

}
