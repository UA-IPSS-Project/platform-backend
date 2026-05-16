package pt.florinhas.marcacoes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

import io.minio.MinioClient;
import io.minio.SetBucketEncryptionArgs;
import io.minio.messages.SseConfiguration;
import io.minio.messages.SseConfigurationRule;
import io.minio.messages.SseAlgorithm;

import pt.florinhas.common_data.domain.*;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.common_data.security.CryptoUtils;

@SpringBootApplication
@ComponentScan(basePackages = {
		"pt.florinhas.marcacoes",
		"pt.florinhas.common_data"
})
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

	@Bean
	CommandLineRunner configureBucketEncryption(
			MinioClient minioClient,
			@Value("${minio.bucket:marcacoes}") String bucketName) {
		return args -> {
			try {
				minioClient.setBucketEncryption(SetBucketEncryptionArgs.builder()
						.bucket(bucketName)
						.config(new SseConfiguration(new SseConfigurationRule(SseAlgorithm.AES256, null)))
						.build());
				LOGGER.info(">>> SSE-S3 encryption enabled on bucket '{}'.", bucketName);
			} catch (Exception e) {
				LOGGER.warn(">>> Could not enable SSE-S3 on bucket '{}': {}", bucketName, e.getMessage());
			}
		};
	}
	@ConditionalOnBean(PasswordEncoder.class)

	@Bean
	CommandLineRunner initDefaultFuncionarios(
			FuncionarioRepository funcionarioRepository, 
			PasswordEncoder encoder, 
			CryptoUtils cryptoUtils,
			pt.florinhas.marcacoes.service.KeycloakAdminClient keycloakAdminClient) {
		return args -> {

			upsertFuncionario(funcionarioRepository, encoder, cryptoUtils, keycloakAdminClient, new SeedAccount(
				    "999999998",
				    "Funcionário Secretaria",
				    "secretaria@florinhasdovouga.pt",
				    "999999998",
				    "sec123",
				    FuncionarioTipo.SECRETARIA));

			    upsertFuncionario(funcionarioRepository, encoder, cryptoUtils, keycloakAdminClient, new SeedAccount(
				    "999999997",
				    "Funcionário Balneário",
				    "balneario@florinhasdovouga.pt",
				    "999999997",
				    "bal123",
				    FuncionarioTipo.BALNEARIO));

				upsertFuncionario(funcionarioRepository, encoder, cryptoUtils, keycloakAdminClient, new SeedAccount(
				    "999999996",
				    "Funcionário Escola",
				    "escola@florinhasdovouga.pt",
				    "999999996",
				    "esc123",
				    FuncionarioTipo.ESCOLA));

			upsertFuncionario(funcionarioRepository, encoder, cryptoUtils, keycloakAdminClient, new SeedAccount(
				    "999999995",
				    "Encarregado Proteção Dados",
				    "dpo@florinhasdovouga.pt",
				    "999999995",
				    "dpo123",
				    FuncionarioTipo.DPO));
		};
	}

	private static void upsertFuncionario(
			FuncionarioRepository funcionarioRepository,
			PasswordEncoder encoder,
			CryptoUtils cryptoUtils,
			pt.florinhas.marcacoes.service.KeycloakAdminClient keycloakAdminClient,
			SeedAccount account) {
		String nif = account.nif();
		var byEmail = funcionarioRepository.findByEmail(account.email());
		Funcionario funcionario = funcionarioRepository.findByNifHash(cryptoUtils.generateBlindIndex(nif))
				.or(() -> byEmail.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(byEmail.get(0)))
				.orElseGet(Funcionario::new);

		boolean isNew = funcionario.getId() == null;

		funcionario.setNome(account.nome());
		funcionario.setEmail(account.email());
		funcionario.setNif(nif);
		funcionario.setTelefone(account.telefone());
		funcionario.setTipo(account.tipo());
		funcionario.setActivo(true);
		funcionario.setTermsAcceptedAt(java.time.LocalDateTime.now());

		if (funcionario.getPassHash() == null || funcionario.getPassHash().isBlank()) {
			funcionario.setPassHash(encoder.encode(account.defaultPassword()));
		}

		funcionarioRepository.save(funcionario);
		
		// Sync with Keycloak
		keycloakAdminClient.criarUtilizador(
				account.email(), 
				account.nome(), 
				account.tipo().name(), 
				account.defaultPassword()
		);

		LOGGER.info(">>> Conta base {} ({}) {}.", account.email(), account.tipo().name(), isNew ? "criada" : "atualizada");
	}

}
