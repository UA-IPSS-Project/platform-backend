package pt.florinhas.requisicoes;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.repository.MaterialRepository;

@SpringBootApplication
public class RequisicoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequisicoesApplication.class, args);
	}

	@Bean
	CommandLineRunner initMateriais(MaterialRepository materialRepository) {
		return args -> {
			List<String> materiaisBase = List.of(
					"Papel A4",
					"Papel para fotocopiadora",
					"Esferográfica azul",
					"Esferográfica preta",
					"Esferográfica vermelha",
					"Esferográfica verde",
					"Lápis",
					"Lapiseira",
					"Minas",
					"Borracha",
					"Corretor líquido",
					"Corretor em fita",
					"Agrafos",
					"Clipes",
					"Pionés",
					"Elásticos de borracha",
					"Micas",
					"Dossiê",
					"Fita cola",
					"Cola baton",
					"Cola líquida",
					"Agrafador",
					"Saca-agrafos",
					"Tesoura", 
					"Régua",
					"Furador",
					"Blocos de notas",
					"Post-its",
					"Sublinhador",
					"Marcadores para quadro branco",
					"Giz",
					"Apagadores",
					"Cartolinas",
					"Papel higiénico",
					"Sabonete líquido",
					"Álcool 70%",
					"Detergente multiusos",
					"Desinfetante",
					"Spray limpa-móveis",
					"Panos de microfibras",
					"Esponja",
					"Sacos de lixo",
					"Ambientadores",
					"Detergente da loiça",
					"Tinteiros",
					"PenDrives");

			for (String nomeMaterial : materiaisBase) {
				if (!materialRepository.existsByNomeIgnoreCase(nomeMaterial)) {
					Material material = new Material();
					material.setNome(nomeMaterial);
					materialRepository.save(material);
				}
			}
		};
	}

}
