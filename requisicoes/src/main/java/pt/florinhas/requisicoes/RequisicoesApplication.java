package pt.florinhas.requisicoes;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.MaterialCategoria;
import pt.florinhas.requisicoes.repository.MaterialRepository;

@SpringBootApplication
public class RequisicoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequisicoesApplication.class, args);
	}

	@Bean
	CommandLineRunner initMateriais(MaterialRepository materialRepository) {
		return args -> {
			record MaterialSeed(String nome, MaterialCategoria categoria, String atributo, String valorAtributo) {
			}

			List<MaterialSeed> materiaisBase = List.of(
					new MaterialSeed("Papel", MaterialCategoria.PAPEL_E_ARQUIVO, "Formato", "A4"),
					new MaterialSeed("Papel", MaterialCategoria.PAPEL_E_ARQUIVO, "Formato", "Fotocopia"),
					new MaterialSeed("Esferográfica", MaterialCategoria.ESCRITA, "Cor", "Azul"),
					new MaterialSeed("Esferográfica", MaterialCategoria.ESCRITA, "Cor", "Preta"),
					new MaterialSeed("Esferográfica", MaterialCategoria.ESCRITA, "Cor", "Vermelha"),
					new MaterialSeed("Esferográfica", MaterialCategoria.ESCRITA, "Cor", "Verde"),
					new MaterialSeed("Lápis", MaterialCategoria.ESCRITA, "Dureza", "HB"),
					new MaterialSeed("Lapiseira", MaterialCategoria.ESCRITA, "Espessura", "0.5mm"),
					new MaterialSeed("Minas", MaterialCategoria.ESCRITA, "Espessura", "0.5mm"),
					new MaterialSeed("Borracha", MaterialCategoria.ESCRITA, "Tipo", "Branca"),
					new MaterialSeed("Corretor", MaterialCategoria.ESCRITA, "Tipo", "Liquido"),
					new MaterialSeed("Corretor", MaterialCategoria.ESCRITA, "Tipo", "Fita"),
					new MaterialSeed("Agrafos", MaterialCategoria.PAPEL_E_ARQUIVO, "Tipo", "26/6"),
					new MaterialSeed("Clipes", MaterialCategoria.PAPEL_E_ARQUIVO, null, null),
					new MaterialSeed("Pionés", MaterialCategoria.PAPEL_E_ARQUIVO, null, null),
					new MaterialSeed("Elásticos de borracha", MaterialCategoria.PAPEL_E_ARQUIVO, null, null),
					new MaterialSeed("Micas", MaterialCategoria.PAPEL_E_ARQUIVO, "Formato", "A4"),
					new MaterialSeed("Dossiê", MaterialCategoria.PAPEL_E_ARQUIVO, "Largura", "A4"),
					new MaterialSeed("Fita cola", MaterialCategoria.PAPEL_E_ARQUIVO, null, null),
					new MaterialSeed("Cola", MaterialCategoria.PAPEL_E_ARQUIVO, "Tipo", "Baton"),
					new MaterialSeed("Cola", MaterialCategoria.PAPEL_E_ARQUIVO, "Tipo", "Liquida"),
					new MaterialSeed("Agrafador", MaterialCategoria.PAPEL_E_ARQUIVO, null, null),
					new MaterialSeed("Saca-agrafos", MaterialCategoria.PAPEL_E_ARQUIVO, null, null),
					new MaterialSeed("Tesoura", MaterialCategoria.PAPEL_E_ARQUIVO, null, null),
					new MaterialSeed("Régua", MaterialCategoria.PAPEL_E_ARQUIVO, "Comprimento", "30cm"),
					new MaterialSeed("Furador", MaterialCategoria.PAPEL_E_ARQUIVO, "Tipo", "2 furos"),
					new MaterialSeed("Blocos de notas", MaterialCategoria.ESCRITA, "Formato", "A5"),
					new MaterialSeed("Post-its", MaterialCategoria.ESCRITA, null, null),
					new MaterialSeed("Sublinhador", MaterialCategoria.ESCRITA, "Cor", "Amarelo"),
					new MaterialSeed("Marcadores", MaterialCategoria.ESCRITA, "Tipo", "Quadro branco"),
					new MaterialSeed("Giz", MaterialCategoria.ESCRITA, "Cor", "Branco"),
					new MaterialSeed("Apagador", MaterialCategoria.ESCRITA, "Tipo", "Quadro branco"),
					new MaterialSeed("Cartolina", MaterialCategoria.PAPEL_E_ARQUIVO, "Formato", "A2"),
					new MaterialSeed("Papel higiénico", MaterialCategoria.HIGIENE_E_LIMPEZA, "Tipo", "Dupla folha"),
					new MaterialSeed("Sabonete", MaterialCategoria.HIGIENE_E_LIMPEZA, "Tipo", "Liquido"),
					new MaterialSeed("Sabonete", MaterialCategoria.HIGIENE_E_LIMPEZA, "Tipo", "Solido"),
					new MaterialSeed("Álcool", MaterialCategoria.HIGIENE_E_LIMPEZA, "Concentracao", "70%"),
					new MaterialSeed("Detergente", MaterialCategoria.HIGIENE_E_LIMPEZA, "Tipo", "Multiusos"),
					new MaterialSeed("Desinfetante", MaterialCategoria.HIGIENE_E_LIMPEZA, "Tipo", "Superficies"),
					new MaterialSeed("Spray limpa-móveis", MaterialCategoria.HIGIENE_E_LIMPEZA, null, null),
					new MaterialSeed("Pano", MaterialCategoria.HIGIENE_E_LIMPEZA, "Tipo", "Microfibra"),
					new MaterialSeed("Esponja", MaterialCategoria.HIGIENE_E_LIMPEZA, "Tipo", "Abrasiva"),
					new MaterialSeed("Sacos de lixo", MaterialCategoria.HIGIENE_E_LIMPEZA, "Capacidade", "50L"),
					new MaterialSeed("Ambientadores", MaterialCategoria.HIGIENE_E_LIMPEZA, "Aroma", "Lavanda"),
					new MaterialSeed("Detergente", MaterialCategoria.HIGIENE_E_LIMPEZA, "Tipo", "Loiça"),
					new MaterialSeed("Tinteiros", MaterialCategoria.TECNOLOGIA, "Cor", "Preto"),
					new MaterialSeed("PenDrive", MaterialCategoria.TECNOLOGIA, "Capacidade", "32GB"));

			Map<String, Material> materiaisExistentesPorNome = materialRepository.findAll().stream()
					.collect(Collectors.toMap(
							material -> material.getNome().toLowerCase(Locale.ROOT),
							Function.identity(),
							(existing, ignored) -> existing));

			for (MaterialSeed seed : materiaisBase) {
				Material existente = materiaisExistentesPorNome.get(seed.nome().toLowerCase(Locale.ROOT));
				if (existente == null) {
					Material material = new Material();
					material.setNome(seed.nome());
					material.setCategoria(seed.categoria());
					material.setAtributo(seed.atributo());
					material.setValorAtributo(seed.valorAtributo());
					materialRepository.save(material);
					continue;
				}

				boolean updated = false;
				if (existente.getCategoria() == null) {
					existente.setCategoria(seed.categoria());
					updated = true;
				}
				if (existente.getAtributo() == null || existente.getAtributo().isBlank()) {
					existente.setAtributo(seed.atributo());
					updated = true;
				}
				if (existente.getValorAtributo() == null || existente.getValorAtributo().isBlank()) {
					existente.setValorAtributo(seed.valorAtributo());
					updated = true;
				}

				if (updated) {
					materialRepository.save(existente);
				}
			}
		};
	}

}
