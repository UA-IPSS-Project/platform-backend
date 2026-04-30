package pt.florinhas.marcacoes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.common_data.domain.*;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.validation.NifValidator;
import java.security.MessageDigest;

@SpringBootApplication
@EntityScan(basePackages = {
		"pt.florinhas.marcacoes.domain",
		"pt.florinhas.common_data.domain"
})
@EnableJpaRepositories(basePackages = {
		"pt.florinhas.marcacoes.repository",
		"pt.florinhas.common_data.repository"
})
@EnableScheduling
public class MarcacoesApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(MarcacoesApplication.class);

	private record SeedAccount(
			String nif,
			String nome,
			String email,
			String telefone,
			String defaultPassword,
			FuncionarioTipo tipo) {
	}

	public static void main(String[] args) {
		SpringApplication.run(MarcacoesApplication.class, args);
	}

	/**
	 * CommandLineRunner que garante a existência de contas base funcionais
	 * (Secretaria, Balneário, Escola) ao iniciar a aplicação.
	 */
	@Bean
	NifValidator nifValidator() {
		return new NifValidator();
	}
	@ConditionalOnBean(PasswordEncoder.class)

	@Bean
	CommandLineRunner initDefaultFuncionarios(FuncionarioRepository funcionarioRepository, PasswordEncoder encoder) {
		return args -> {

			upsertFuncionario(funcionarioRepository, encoder, new SeedAccount(
				    "999999998",
				    "Funcionário Secretaria",
				    "secretaria@florinhasdovouga.pt",
				    "999999998",
				    "sec123",
				    FuncionarioTipo.SECRETARIA));

			    upsertFuncionario(funcionarioRepository, encoder, new SeedAccount(
				    "999999997",
				    "Funcionário Balneário",
				    "balneario@florinhasdovouga.pt",
				    "999999997",
				    "bal123",
				    FuncionarioTipo.BALNEARIO));

				upsertFuncionario(funcionarioRepository, encoder, new SeedAccount(
				    "999999996",
				    "Funcionário Escola",
				    "escola@florinhasdovouga.pt",
				    "999999996",
				    "esc123",
				    FuncionarioTipo.ESCOLA));
		};
	}

	private static void upsertFuncionario(
			FuncionarioRepository funcionarioRepository,
			PasswordEncoder encoder,
			SeedAccount account) {
		String nifHash = sha256(account.nif());
		var byEmail = funcionarioRepository.findByEmail(account.email());
		Funcionario funcionario = funcionarioRepository.findByNif(nifHash)
				.or(() -> byEmail.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(byEmail.get(0)))
				.orElseGet(Funcionario::new);

		boolean isNew = funcionario.getId() == null;

		funcionario.setNome(account.nome());
		funcionario.setEmail(account.email());
		funcionario.setNif(nifHash);
		funcionario.setTelefone(account.telefone());
		funcionario.setTipo(account.tipo());
		funcionario.setActivo(true);
		funcionario.setTermsAcceptedAt(java.time.LocalDateTime.now());

		if (funcionario.getPassHash() == null || funcionario.getPassHash().isBlank()) {
			funcionario.setPassHash(encoder.encode(account.defaultPassword()));
		}

		funcionarioRepository.save(funcionario);
		LOGGER.info(">>> Conta base {} ({}) {}.", account.email(), account.tipo().name(), isNew ? "criada" : "atualizada");
	}

	private static String sha256(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(input.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
