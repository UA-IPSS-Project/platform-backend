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

import pt.florinhas.requisicoes.domain.ManutencaoCategoria;
import pt.florinhas.requisicoes.domain.ManutencaoItem;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.MaterialCategoria;
import pt.florinhas.requisicoes.domain.TipoManutencao;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;
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

	@Bean
	@Order(3)
	CommandLineRunner initManutencaoItems(ManutencaoItemRepository manutencaoItemRepository) {
		return args -> {
			record ManutencaoItemSeed(ManutencaoCategoria categoria, String espaco, String itemVerificacao) {
			}

			List<ManutencaoItemSeed> itemsBase = List.of(
					// CATL
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "WC (Masc/Fem)", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.CATL, "Salão e Palco", "Chão"),
					// RC
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Parque", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Relvado", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Acolhimento", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Gabinetes", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "WCs", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Oficina", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Biblioteca", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Refeitório", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.RC, "Elevador", "Chão"),
					// PRE_ESCOLAR
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Salas (Amarela, Azul, Verde, Arco-Íris)", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "WCs", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Hall", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Corredor", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.PRE_ESCOLAR, "Parque exterior", "Chão"),
					// CRECHE
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Berçário", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Salas (Limão, Alface, Vermelha, Turquesa)", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Fraldário", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Copa", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Refeitório", "Chão"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Alumínios"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Blackouts"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Madeiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Armários"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Aquecedores"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Torneiras"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Eletricidade"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Cabides"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Paredes"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Tetos"),
					new ManutencaoItemSeed(ManutencaoCategoria.CRECHE, "Parque 3º andar", "Chão"));

			if (manutencaoItemRepository.count() == 0) {
				for (ManutencaoItemSeed seed : itemsBase) {
					ManutencaoItem item = new ManutencaoItem();
					item.setCategoria(seed.categoria());
					item.setEspaco(seed.espaco());
					item.setItemVerificacao(seed.itemVerificacao());
					manutencaoItemRepository.save(item);
				}
			}

			List<String> verificacoes = List.of(
					"Alumínios",
					"Blackouts",
					"Madeiras",
					"Armários",
					"Aquecedores",
					"Torneiras",
					"Eletricidade",
					"Cabides",
					"Paredes",
					"Tetos",
					"Chão");

			Map<ManutencaoCategoria, List<String>> espacosObrigatorios = Map.of(
					ManutencaoCategoria.CATL, List.of(
							"WC masculino",
							"WC feminino",
							"Salão",
							"Salão (palco)"),
					ManutencaoCategoria.RC, List.of(
							"Parque exterior",
							"Relvado",
							"Acolhimento pré",
							"Acolhimento creche",
							"Gabinete",
							"WC deficientes",
							"WC Rosa",
							"WC azul",
							"Gabinete médico",
							"Oficina",
							"Corredor + WC",
							"Biblioteca",
							"Refeitório",
							"Lavatórios + Hall",
							"Elevador",
							"Escadas acesso 1º"),
					ManutencaoCategoria.PRE_ESCOLAR, List.of(
							"Sala acolhimento",
							"Sala de educadoras",
							"WC deficientes",
							"WC azul",
							"WC cor de rosa",
							"Hall",
							"Escadas acesso 2º",
							"Corredor",
							"Sala Amarela",
							"Sala Azul",
							"Sala Verde",
							"Sala Arco-Íris",
							"WC",
							"Parque exterior"),
					ManutencaoCategoria.CRECHE, List.of(
							"Parque ext. 3º andar",
							"S. Acolhimento grande",
							"S. Acollhimento peq.",
							"WC",
							"WC azul",
							"Corredor e hall",
							"Escadas acesso sotão",
							"Sala Amarela limão",
							"Sala Verde Alface",
							"Sala Vermelha",
							"Refeitório",
							"Copa",
							"Fraldário",
							"Sala azul turquesa",
							"Berçário"));

			Map<String, ManutencaoItem> existentesPorChave = manutencaoItemRepository.findAll().stream()
					.collect(Collectors.toMap(
							item -> item.getCategoria().name() + "|" + item.getEspaco() + "|" + item.getItemVerificacao(),
							Function.identity(),
							(existing, ignored) -> existing));

			espacosObrigatorios.forEach((categoria, espacos) -> {
				for (String espaco : espacos) {
					for (String verificacao : verificacoes) {
						String chave = categoria.name() + "|" + espaco + "|" + verificacao;
						if (!existentesPorChave.containsKey(chave)) {
							ManutencaoItem item = new ManutencaoItem();
							item.setCategoria(categoria);
							item.setEspaco(espaco);
							item.setItemVerificacao(verificacao);
							manutencaoItemRepository.save(item);
						}
					}
				}
			});
		};
	}

}
