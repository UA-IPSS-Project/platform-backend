package pt.florinhas.marcacoes;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;

@SpringBootApplication
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
	 * CommandLineRunner que garante a existência de contas base para administração
	 * e secretaria ao iniciar a aplicação.
	 */
	@Bean
	CommandLineRunner initAdminSecretaria(FuncionarioRepository funcionarioRepository, PasswordEncoder encoder) {
		return args -> {
			    upsertFuncionario(funcionarioRepository, encoder, new SeedAccount(
				    "999999999",
				    "Admin Plataforma",
				    "admin@florinhasdovouga.pt",
				    "999999999",
				    "admin123",
				    FuncionarioTipo.ADMIN));

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
		};
	}

	private static void upsertFuncionario(
			FuncionarioRepository funcionarioRepository,
			PasswordEncoder encoder,
			SeedAccount account) {
		Funcionario funcionario = funcionarioRepository.findByNif(account.nif()).orElseGet(Funcionario::new);
		boolean isNew = funcionario.getId() == null;

		funcionario.setNome(account.nome());
		funcionario.setEmail(account.email());
		funcionario.setNif(account.nif());
		funcionario.setTelefone(account.telefone());
		funcionario.setTipo(account.tipo());
		funcionario.setActivo(true);

		if (isNew || funcionario.getPassHash() == null || funcionario.getPassHash().isBlank()) {
			funcionario.setPassHash(encoder.encode(account.defaultPassword()));
		}

		funcionarioRepository.save(funcionario);
		LOGGER.info(">>> Conta base {} ({}) {}.", account.email(), account.tipo().name(), isNew ? "criada" : "atualizada");
	}

}
