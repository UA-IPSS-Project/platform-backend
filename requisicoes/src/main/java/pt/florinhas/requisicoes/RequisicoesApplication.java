package pt.florinhas.requisicoes;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.MaterialCategoria;
import pt.florinhas.requisicoes.domain.TipoManutencao;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;
import pt.florinhas.requisicoes.repository.MaterialRepository;
import pt.florinhas.requisicoes.repository.TipoManutencaoRepository;
import pt.florinhas.requisicoes.repository.TransporteRepository;

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

	@Bean
	@Order(0)
	CommandLineRunner alinharConstraintCategoriaTransporte(JdbcTemplate jdbcTemplate) {
		return args -> {
			jdbcTemplate.execute("ALTER TABLE transporte DROP CONSTRAINT IF EXISTS transporte_categoria_check");
			jdbcTemplate.execute(
					"ALTER TABLE transporte ADD CONSTRAINT transporte_categoria_check CHECK (categoria IN ('PESADO_DE_PASSAGEIROS', 'LIGEIRO_DE_PASSAGEIROS', 'LIGEIRO_DE_MERCADORIAS', 'LIGEIRO_ESPECIAL', 'LIGEIRO', 'PESADO', 'PASSAGEIROS', 'ADAPTADO'))");
		};
	}

	@Bean
	@Order(2)
	CommandLineRunner initTiposManutencao(TipoManutencaoRepository tipoManutencaoRepository) {
		return args -> {
			record TipoManutencaoSeed(String nome, String descricao) {
			}

			List<TipoManutencaoSeed> tiposBase = List.of(
					new TipoManutencaoSeed("Reparação", "Conserto e reparação de equipamentos e infraestruturas"),
					new TipoManutencaoSeed("Limpeza", "Limpeza e higienização de espaços e equipamentos"),
					new TipoManutencaoSeed("Pintura", "Trabalhos de pintura e conservação de superfícies"),
					new TipoManutencaoSeed("Eletricidade", "Intervenções elétricas e de iluminação"),
					new TipoManutencaoSeed("Canalização", "Reparação de canalização, torneiras e escoamentos"),
					new TipoManutencaoSeed("Carpintaria", "Reparação e ajuste de portas, móveis e estruturas em madeira"),
					new TipoManutencaoSeed("Climatização", "Manutenção de equipamentos de aquecimento e ar condicionado"),
					new TipoManutencaoSeed("Segurança", "Manutenção de alarmes, extintores e sistemas de segurança"));

			Map<String, TipoManutencao> tiposExistentesPorNome = tipoManutencaoRepository.findAll().stream()
					.collect(Collectors.toMap(
							tipo -> tipo.getNome().toLowerCase(Locale.ROOT),
							Function.identity(),
							(existing, ignored) -> existing));

			for (TipoManutencaoSeed seed : tiposBase) {
				TipoManutencao existente = tiposExistentesPorNome.get(seed.nome().toLowerCase(Locale.ROOT));
				if (existente == null) {
					TipoManutencao tipo = new TipoManutencao();
					tipo.setNome(seed.nome());
					tipo.setDescricao(seed.descricao());
					tipoManutencaoRepository.save(tipo);
					continue;
				}

			if (existente.getDescricao() == null || existente.getDescricao().isBlank()) {
					existente.setDescricao(seed.descricao());
					tipoManutencaoRepository.save(existente);
				}
			}
		};
	}

	@Bean
	@Order(1)
	CommandLineRunner initTransportes(TransporteRepository transporteRepository) {
		return args -> {
			record TransporteSeed(
					String codigo,
					String tipo,
					TransporteCategoria categoria,
					String marca,
					String modelo,
					String matricula,
					LocalDate dataMatricula,
					Integer lotacao) {
			}

			List<TransporteSeed> transportesBase = List.of(
					new TransporteSeed("V01", "Mini Autocarro", TransporteCategoria.PESADO_DE_PASSAGEIROS, "Iveco", "70c18", "32-TS-44", LocalDate.of(2017, 10, 26), 31),
					new TransporteSeed("V02", "Carrinha", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Renault", "Master", "61-PX-87", LocalDate.of(2015, 5, 26), 9),
					new TransporteSeed("V03", "Carrinha", TransporteCategoria.LIGEIRO_DE_MERCADORIAS, "Renault", "Kangoo", "36-OU-67", LocalDate.of(2014, 6, 25), 3),
					new TransporteSeed("V04", "Carrinha", TransporteCategoria.LIGEIRO_DE_MERCADORIAS, "Renault", "Trafic", "79-NV-51", LocalDate.of(2013, 7, 19), 2),
					new TransporteSeed("V05", "Carrinha", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Ford", "Transit", "75-HJ-95", LocalDate.of(2009, 3, 18), 9),
					new TransporteSeed("V06", "Carrinha", TransporteCategoria.LIGEIRO_ESPECIAL, "Mercedes", "215 CDI", "43-HD-54", LocalDate.of(2009, 1, 13), 6),
					new TransporteSeed("V07", "Carro", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Skoda", "Fabia", "68-ED-26", LocalDate.of(2007, 7, 31), 5),
					new TransporteSeed("V08", "Carrinha", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Renault", "Kangoo-Al", "90-43-LJ", LocalDate.of(1998, 7, 1), 6),
					new TransporteSeed("V09", "Carrinha", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Mercedes", "208 D/30", "54-95-GE", LocalDate.of(1996, 1, 19), 9));

			Map<String, Transporte> transportesPorCodigo = transporteRepository.findAll().stream()
					.filter(transporte -> transporte.getCodigo() != null && !transporte.getCodigo().isBlank())
					.collect(Collectors.toMap(
							transporte -> transporte.getCodigo().toUpperCase(Locale.ROOT),
							Function.identity(),
							(existing, ignored) -> existing));

			Map<String, Transporte> transportesPorMatricula = transporteRepository.findAll().stream()
					.filter(transporte -> transporte.getMatricula() != null && !transporte.getMatricula().isBlank())
					.collect(Collectors.toMap(
							transporte -> transporte.getMatricula().toUpperCase(Locale.ROOT),
							Function.identity(),
							(existing, ignored) -> existing));

			for (TransporteSeed seed : transportesBase) {
				Transporte transporte = transportesPorCodigo.get(seed.codigo().toUpperCase(Locale.ROOT));
				if (transporte == null) {
					transporte = transportesPorMatricula.get(seed.matricula().toUpperCase(Locale.ROOT));
				}
				if (transporte == null) {
					transporte = new Transporte();
				}

				transporte.setCodigo(seed.codigo());
				transporte.setTipo(seed.tipo());
				transporte.setCategoria(seed.categoria());
				transporte.setMarca(seed.marca());
				transporte.setModelo(seed.modelo());
				transporte.setMatricula(seed.matricula());
				transporte.setDataMatricula(seed.dataMatricula());
				transporte.setLotacao(seed.lotacao());

				Transporte persisted = transporteRepository.save(transporte);
				transportesPorCodigo.put(seed.codigo().toUpperCase(Locale.ROOT), persisted);
				transportesPorMatricula.put(seed.matricula().toUpperCase(Locale.ROOT), persisted);
			}
		};
	}

}
