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

import pt.florinhas.requisicoes.domain.ManutencaoItem;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.TipoManutencao;
import pt.florinhas.requisicoes.domain.Transporte;
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
			record MaterialSeed(String nome, String categoria, String atributo, String valorAtributo) {
			}

			List<MaterialSeed> materiaisBase = List.of(
					new MaterialSeed("Papel", "PAPEL_E_ARQUIVO", "Formato", "A4"),
					new MaterialSeed("Papel", "PAPEL_E_ARQUIVO", "Formato", "Fotocopia"),
					new MaterialSeed("Esferográfica", "ESCRITA", "Cor", "Azul"),
					new MaterialSeed("Esferográfica", "ESCRITA", "Cor", "Preta"),
					new MaterialSeed("Esferográfica", "ESCRITA", "Cor", "Vermelha"),
					new MaterialSeed("Esferográfica", "ESCRITA", "Cor", "Verde"),
					new MaterialSeed("Lápis", "ESCRITA", "Dureza", "HB"),
					new MaterialSeed("Lapiseira", "ESCRITA", "Espessura", "0.5mm"),
					new MaterialSeed("Minas", "ESCRITA", "Espessura", "0.5mm"),
					new MaterialSeed("Borracha", "ESCRITA", "Tipo", "Branca"),
					new MaterialSeed("Corretor", "ESCRITA", "Tipo", "Liquido"),
					new MaterialSeed("Corretor", "ESCRITA", "Tipo", "Fita"),
					new MaterialSeed("Agrafos", "PAPEL_E_ARQUIVO", "Tipo", "26/6"),
					new MaterialSeed("Clipes", "PAPEL_E_ARQUIVO", null, null),
					new MaterialSeed("Pionés", "PAPEL_E_ARQUIVO", null, null),
					new MaterialSeed("Elásticos de borracha", "PAPEL_E_ARQUIVO", null, null),
					new MaterialSeed("Micas", "PAPEL_E_ARQUIVO", "Formato", "A4"),
					new MaterialSeed("Dossiê", "PAPEL_E_ARQUIVO", "Largura", "A4"),
					new MaterialSeed("Fita cola", "PAPEL_E_ARQUIVO", null, null),
					new MaterialSeed("Cola", "PAPEL_E_ARQUIVO", "Tipo", "Baton"),
					new MaterialSeed("Cola", "PAPEL_E_ARQUIVO", "Tipo", "Liquida"),
					new MaterialSeed("Agrafador", "PAPEL_E_ARQUIVO", null, null),
					new MaterialSeed("Saca-agrafos", "PAPEL_E_ARQUIVO", null, null),
					new MaterialSeed("Tesoura", "PAPEL_E_ARQUIVO", null, null),
					new MaterialSeed("Régua", "PAPEL_E_ARQUIVO", "Comprimento", "30cm"),
					new MaterialSeed("Furador", "PAPEL_E_ARQUIVO", "Tipo", "2 furos"),
					new MaterialSeed("Blocos de notas", "ESCRITA", "Formato", "A5"),
					new MaterialSeed("Post-its", "ESCRITA", null, null),
					new MaterialSeed("Sublinhador", "ESCRITA", "Cor", "Amarelo"),
					new MaterialSeed("Marcadores", "ESCRITA", "Tipo", "Quadro branco"),
					new MaterialSeed("Giz", "ESCRITA", "Cor", "Branco"),
					new MaterialSeed("Apagador", "ESCRITA", "Tipo", "Quadro branco"),
					new MaterialSeed("Cartolina", "PAPEL_E_ARQUIVO", "Formato", "A2"),
					new MaterialSeed("Papel higiénico", "HIGIENE_E_LIMPEZA", "Tipo", "Dupla folha"),
					new MaterialSeed("Sabonete", "HIGIENE_E_LIMPEZA", "Tipo", "Liquido"),
					new MaterialSeed("Sabonete", "HIGIENE_E_LIMPEZA", "Tipo", "Solido"),
					new MaterialSeed("Álcool", "HIGIENE_E_LIMPEZA", "Concentracao", "70%"),
					new MaterialSeed("Detergente", "HIGIENE_E_LIMPEZA", "Tipo", "Multiusos"),
					new MaterialSeed("Desinfetante", "HIGIENE_E_LIMPEZA", "Tipo", "Superficies"),
					new MaterialSeed("Spray limpa-móveis", "HIGIENE_E_LIMPEZA", null, null),
					new MaterialSeed("Pano", "HIGIENE_E_LIMPEZA", "Tipo", "Microfibra"),
					new MaterialSeed("Esponja", "HIGIENE_E_LIMPEZA", "Tipo", "Abrasiva"),
					new MaterialSeed("Sacos de lixo", "HIGIENE_E_LIMPEZA", "Capacidade", "50L"),
					new MaterialSeed("Ambientadores", "HIGIENE_E_LIMPEZA", "Aroma", "Lavanda"),
					new MaterialSeed("Detergente", "HIGIENE_E_LIMPEZA", "Tipo", "Loiça"),
					new MaterialSeed("Tinteiros", "TECNOLOGIA", "Cor", "Preto"),
					new MaterialSeed("PenDrive", "TECNOLOGIA", "Capacidade", "32GB"));

			Map<String, Material> materiaisExistentes = materialRepository.findAll().stream()
					.collect(Collectors.toMap(
							material -> (material.getNome() + "|" + (material.getValorAtributo() != null ? material.getValorAtributo() : "")).toLowerCase(Locale.ROOT),
							Function.identity(),
							(existing, ignored) -> existing));

			for (MaterialSeed seed : materiaisBase) {
				String key = (seed.nome() + "|" + (seed.valorAtributo() != null ? seed.valorAtributo() : "")).toLowerCase(Locale.ROOT);
				Material existente = materiaisExistentes.get(key);
				
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
				if (existente.getCategoria() == null || !existente.getCategoria().equals(seed.categoria())) {
					existente.setCategoria(seed.categoria());
					updated = true;
				}
				if (seed.atributo() != null && (existente.getAtributo() == null || !existente.getAtributo().equals(seed.atributo()))) {
					existente.setAtributo(seed.atributo());
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
	@Order(1)
	CommandLineRunner initTransportes(TransporteRepository transporteRepository) {
		return args -> {
			record TransporteSeed(
					String codigo,
					String tipo,
					String categoria,
					String marca,
					String modelo,
					String matricula,
					LocalDate dataMatricula,
					Integer lotacao) {
			}

			List<TransporteSeed> transportesBase = List.of(
					new TransporteSeed("V01", "Mini Autocarro", "PESADO_DE_PASSAGEIROS", "Iveco", "70c18", "32-TS-44", LocalDate.of(2017, 10, 26), 31),
					new TransporteSeed("V02", "Carrinha", "LIGEIRO_DE_PASSAGEIROS", "Renault", "Master", "61-PX-87", LocalDate.of(2015, 5, 26), 9),
					new TransporteSeed("V03", "Carrinha", "LIGEIRO_DE_MERCADORIAS", "Renault", "Kangoo", "36-OU-67", LocalDate.of(2014, 6, 25), 3),
					new TransporteSeed("V04", "Carrinha", "LIGEIRO_DE_MERCADORIAS", "Renault", "Trafic", "79-NV-51", LocalDate.of(2013, 7, 19), 2),
					new TransporteSeed("V05", "Carrinha", "LIGEIRO_DE_PASSAGEIROS", "Ford", "Transit", "75-HJ-95", LocalDate.of(2009, 3, 18), 9),
					new TransporteSeed("V06", "Carrinha", "LIGEIRO_ESPECIAL", "Mercedes", "215 CDI", "43-HD-54", LocalDate.of(2009, 1, 13), 6),
					new TransporteSeed("V07", "Carro", "LIGEIRO_DE_PASSAGEIROS", "Skoda", "Fabia", "68-ED-26", LocalDate.of(2007, 7, 31), 5),
					new TransporteSeed("V08", "Carrinha", "LIGEIRO_DE_PASSAGEIROS", "Renault", "Kangoo-Al", "90-43-LJ", LocalDate.of(1998, 7, 1), 6),
					new TransporteSeed("V09", "Carrinha", "LIGEIRO_DE_PASSAGEIROS", "Mercedes", "208 D/30", "54-95-GE", LocalDate.of(1996, 1, 19), 9));

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
	@Order(2)
	CommandLineRunner initManutencaoItems(ManutencaoItemRepository manutencaoItemRepository) {
		return args -> {
			record ManutencaoItemSeed(String categoria, String espaco, String itemVerificacao) {
			}

			List<ManutencaoItemSeed> itemsBase = List.of(
					// CATL
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Alumínios"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Blackouts"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Madeiras"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Armários"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Aquecedores"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Torneiras"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Eletricidade"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Cabides"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Paredes"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Tetos"),
					new ManutencaoItemSeed("CATL", "WC (Masc/Fem)", "Chão"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Alumínios"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Blackouts"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Madeiras"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Armários"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Aquecedores"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Torneiras"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Eletricidade"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Cabides"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Paredes"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Tetos"),
					new ManutencaoItemSeed("CATL", "Salão e Palco", "Chão"),
					// RC
					new ManutencaoItemSeed("RC", "Parque", "Alumínios"),
					new ManutencaoItemSeed("RC", "Parque", "Blackouts"),
					new ManutencaoItemSeed("RC", "Parque", "Madeiras"),
					new ManutencaoItemSeed("RC", "Parque", "Armários"),
					new ManutencaoItemSeed("RC", "Parque", "Aquecedores"),
					new ManutencaoItemSeed("RC", "Parque", "Torneiras"),
					new ManutencaoItemSeed("RC", "Parque", "Eletricidade"),
					new ManutencaoItemSeed("RC", "Parque", "Cabides"),
					new ManutencaoItemSeed("RC", "Parque", "Paredes"),
					new ManutencaoItemSeed("RC", "Parque", "Tetos"),
					new ManutencaoItemSeed("RC", "Parque", "Chão"),
					new ManutencaoItemSeed("RC", "Relvado", "Alumínios"),
					new ManutencaoItemSeed("RC", "Relvado", "Blackouts"),
					new ManutencaoItemSeed("RC", "Relvado", "Madeiras"),
					new ManutencaoItemSeed("RC", "Relvado", "Armários"),
					new ManutencaoItemSeed("RC", "Relvado", "Aquecedores"),
					new ManutencaoItemSeed("RC", "Relvado", "Torneiras"),
					new ManutencaoItemSeed("RC", "Relvado", "Eletricidade"),
					new ManutencaoItemSeed("RC", "Relvado", "Cabides"),
					new ManutencaoItemSeed("RC", "Relvado", "Paredes"),
					new ManutencaoItemSeed("RC", "Relvado", "Tetos"),
					new ManutencaoItemSeed("RC", "Relvado", "Chão"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Alumínios"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Blackouts"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Madeiras"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Armários"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Aquecedores"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Torneiras"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Eletricidade"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Cabides"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Paredes"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Tetos"),
					new ManutencaoItemSeed("RC", "Acolhimento", "Chão"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Alumínios"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Blackouts"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Madeiras"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Armários"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Aquecedores"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Torneiras"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Eletricidade"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Cabides"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Paredes"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Tetos"),
					new ManutencaoItemSeed("RC", "Gabinetes", "Chão"),
					new ManutencaoItemSeed("RC", "WCs", "Alumínios"),
					new ManutencaoItemSeed("RC", "WCs", "Blackouts"),
					new ManutencaoItemSeed("RC", "WCs", "Madeiras"),
					new ManutencaoItemSeed("RC", "WCs", "Armários"),
					new ManutencaoItemSeed("RC", "WCs", "Aquecedores"),
					new ManutencaoItemSeed("RC", "WCs", "Torneiras"),
					new ManutencaoItemSeed("RC", "WCs", "Eletricidade"),
					new ManutencaoItemSeed("RC", "WCs", "Cabides"),
					new ManutencaoItemSeed("RC", "WCs", "Paredes"),
					new ManutencaoItemSeed("RC", "WCs", "Tetos"),
					new ManutencaoItemSeed("RC", "WCs", "Chão"),
					new ManutencaoItemSeed("RC", "Oficina", "Alumínios"),
					new ManutencaoItemSeed("RC", "Oficina", "Blackouts"),
					new ManutencaoItemSeed("RC", "Oficina", "Madeiras"),
					new ManutencaoItemSeed("RC", "Oficina", "Armários"),
					new ManutencaoItemSeed("RC", "Oficina", "Aquecedores"),
					new ManutencaoItemSeed("RC", "Oficina", "Torneiras"),
					new ManutencaoItemSeed("RC", "Oficina", "Eletricidade"),
					new ManutencaoItemSeed("RC", "Oficina", "Cabides"),
					new ManutencaoItemSeed("RC", "Oficina", "Paredes"),
					new ManutencaoItemSeed("RC", "Oficina", "Tetos"),
					new ManutencaoItemSeed("RC", "Oficina", "Chão"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Alumínios"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Blackouts"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Madeiras"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Armários"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Aquecedores"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Torneiras"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Eletricidade"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Cabides"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Paredes"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Tetos"),
					new ManutencaoItemSeed("RC", "Biblioteca", "Chão"),
					new ManutencaoItemSeed("RC", "Refeitório", "Alumínios"),
					new ManutencaoItemSeed("RC", "Refeitório", "Blackouts"),
					new ManutencaoItemSeed("RC", "Refeitório", "Madeiras"),
					new ManutencaoItemSeed("RC", "Refeitório", "Armários"),
					new ManutencaoItemSeed("RC", "Refeitório", "Aquecedores"),
					new ManutencaoItemSeed("RC", "Refeitório", "Torneiras"),
					new ManutencaoItemSeed("RC", "Refeitório", "Eletricidade"),
					new ManutencaoItemSeed("RC", "Refeitório", "Cabides"),
					new ManutencaoItemSeed("RC", "Refeitório", "Paredes"),
					new ManutencaoItemSeed("RC", "Refeitório", "Tetos"),
					new ManutencaoItemSeed("RC", "Refeitório", "Chão"),
					new ManutencaoItemSeed("RC", "Elevador", "Alumínios"),
					new ManutencaoItemSeed("RC", "Elevador", "Blackouts"),
					new ManutencaoItemSeed("RC", "Elevador", "Madeiras"),
					new ManutencaoItemSeed("RC", "Elevador", "Armários"),
					new ManutencaoItemSeed("RC", "Elevador", "Aquecedores"),
					new ManutencaoItemSeed("RC", "Elevador", "Torneiras"),
					new ManutencaoItemSeed("RC", "Elevador", "Eletricidade"),
					new ManutencaoItemSeed("RC", "Elevador", "Cabides"),
					new ManutencaoItemSeed("RC", "Elevador", "Paredes"),
					new ManutencaoItemSeed("RC", "Elevador", "Tetos"),
					new ManutencaoItemSeed("RC", "Elevador", "Chão"),
					// PRE_ESCOLAR
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Alumínios"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Blackouts"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Madeiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Armários"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Aquecedores"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Torneiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Eletricidade"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Cabides"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Paredes"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Tetos"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Chão"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Alumínios"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Blackouts"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Madeiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Armários"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Aquecedores"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Torneiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Eletricidade"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Cabides"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Paredes"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Tetos"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "WCs", "Chão"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Alumínios"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Blackouts"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Madeiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Armários"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Aquecedores"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Torneiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Eletricidade"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Cabides"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Paredes"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Tetos"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Hall", "Chão"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Alumínios"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Blackouts"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Madeiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Armários"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Aquecedores"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Torneiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Eletricidade"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Cabides"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Paredes"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Tetos"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Corredor", "Chão"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Alumínios"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Blackouts"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Madeiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Armários"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Aquecedores"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Torneiras"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Eletricidade"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Cabides"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Paredes"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Tetos"),
					new ManutencaoItemSeed("PRE_ESCOLAR", "Parque exterior", "Chão"),
					// CRECHE
					new ManutencaoItemSeed("CRECHE", "Berçário", "Alumínios"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Blackouts"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Madeiras"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Armários"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Aquecedores"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Torneiras"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Eletricidade"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Cabides"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Paredes"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Tetos"),
					new ManutencaoItemSeed("CRECHE", "Berçário", "Chão"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Alumínios"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Blackouts"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Madeiras"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Armários"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Aquecedores"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Torneiras"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Eletricidade"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Cabides"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Paredes"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Tetos"),
					new ManutencaoItemSeed("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Chão"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Alumínios"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Blackouts"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Madeiras"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Armários"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Aquecedores"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Torneiras"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Eletricidade"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Cabides"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Paredes"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Tetos"),
					new ManutencaoItemSeed("CRECHE", "Fraldário", "Chão"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Alumínios"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Blackouts"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Madeiras"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Armários"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Aquecedores"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Torneiras"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Eletricidade"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Cabides"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Paredes"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Tetos"),
					new ManutencaoItemSeed("CRECHE", "Copa", "Chão"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Alumínios"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Blackouts"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Madeiras"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Armários"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Aquecedores"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Torneiras"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Eletricidade"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Cabides"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Paredes"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Tetos"),
					new ManutencaoItemSeed("CRECHE", "Refeitório", "Chão"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Alumínios"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Blackouts"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Madeiras"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Armários"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Aquecedores"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Torneiras"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Eletricidade"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Cabides"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Paredes"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Tetos"),
					new ManutencaoItemSeed("CRECHE", "Parque 3º andar", "Chão"));

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

			Map<String, List<String>> espacosObrigatorios = Map.of(
					"CATL", List.of(
							"WC masculino",
							"WC feminino",
							"Salão",
							"Salão (palco)"),
					"RC", List.of(
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
					"PRE_ESCOLAR", List.of(
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
					"CRECHE", List.of(
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
							item -> item.getCategoria() + "|" + item.getEspaco() + "|" + item.getItemVerificacao(),
							Function.identity(),
							(existing, ignored) -> existing));

			espacosObrigatorios.forEach((categoria, espacos) -> {
				for (String espaco : espacos) {
					for (String verificacao : verificacoes) {
						String chave = categoria + "|" + espaco + "|" + verificacao;
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
