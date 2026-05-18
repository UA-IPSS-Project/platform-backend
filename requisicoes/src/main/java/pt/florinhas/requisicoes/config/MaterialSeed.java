package pt.florinhas.requisicoes.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.repository.MaterialRepository;

@Slf4j
@Component
@Order(1)
public class MaterialSeed implements CommandLineRunner {

    private static final String CAT_PAPEL_E_ARQUIVO = "PAPEL_E_ARQUIVO";
    private static final String CAT_ESCRITA = "ESCRITA";
    private static final String CAT_HIGIENE_E_LIMPEZA = "HIGIENE_E_LIMPEZA";

    private static final String ATTR_COR = "Cor";
    private static final String ATTR_TIPO = "Tipo";
    private static final String ATTR_FORMATO = "Formato";

    private static final String ESFEROGRAFICA = "Esferográfica";
    private static final String A4 = "A4";

    private final MaterialRepository materialRepository;

    public MaterialSeed(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("--- STARTING MATERIAL SEED ---");
        record MaterialSeedItem(String nome, String categoria, String atributo, String valorAtributo) {
        }

        List<MaterialSeedItem> materiaisBase = List.of(
                new MaterialSeedItem("Papel", CAT_PAPEL_E_ARQUIVO, ATTR_FORMATO, A4),
                new MaterialSeedItem("Papel", CAT_PAPEL_E_ARQUIVO, ATTR_FORMATO, "Fotocopia"),
                new MaterialSeedItem(ESFEROGRAFICA, CAT_ESCRITA, ATTR_COR, "Azul"),
                new MaterialSeedItem(ESFEROGRAFICA, CAT_ESCRITA, ATTR_COR, "Preta"),
                new MaterialSeedItem(ESFEROGRAFICA, CAT_ESCRITA, ATTR_COR, "Vermelha"),
                new MaterialSeedItem(ESFEROGRAFICA, CAT_ESCRITA, ATTR_COR, "Verde"),
                new MaterialSeedItem("Lápis", CAT_ESCRITA, "Dureza", "HB"),
                new MaterialSeedItem("Lapiseira", CAT_ESCRITA, "Espessura", "0.5mm"),
                new MaterialSeedItem("Minas", CAT_ESCRITA, "Espessura", "0.5mm"),
                new MaterialSeedItem("Borracha", CAT_ESCRITA, ATTR_TIPO, "Branca"),
                new MaterialSeedItem("Corretor", CAT_ESCRITA, ATTR_TIPO, "Liquido"),
                new MaterialSeedItem("Corretor", CAT_ESCRITA, ATTR_TIPO, "Fita"),
                new MaterialSeedItem("Agrafos", CAT_PAPEL_E_ARQUIVO, ATTR_TIPO, "26/6"),
                new MaterialSeedItem("Clipes", CAT_PAPEL_E_ARQUIVO, null, null),
                new MaterialSeedItem("Pionés", CAT_PAPEL_E_ARQUIVO, null, null),
                new MaterialSeedItem("Elásticos de borracha", CAT_PAPEL_E_ARQUIVO, null, null),
                new MaterialSeedItem("Micas", CAT_PAPEL_E_ARQUIVO, ATTR_FORMATO, A4),
                new MaterialSeedItem("Dossiê", CAT_PAPEL_E_ARQUIVO, "Largura", A4),
                new MaterialSeedItem("Fita cola", CAT_PAPEL_E_ARQUIVO, null, null),
                new MaterialSeedItem("Cola", CAT_PAPEL_E_ARQUIVO, ATTR_TIPO, "Baton"),
                new MaterialSeedItem("Cola", CAT_PAPEL_E_ARQUIVO, ATTR_TIPO, "Liquida"),
                new MaterialSeedItem("Agrafador", CAT_PAPEL_E_ARQUIVO, null, null),
                new MaterialSeedItem("Saca-agrafos", CAT_PAPEL_E_ARQUIVO, null, null),
                new MaterialSeedItem("Tesoura", CAT_PAPEL_E_ARQUIVO, null, null),
                new MaterialSeedItem("Régua", CAT_PAPEL_E_ARQUIVO, "Comprimento", "30cm"),
                new MaterialSeedItem("Furador", CAT_PAPEL_E_ARQUIVO, ATTR_TIPO, "2 furos"),
                new MaterialSeedItem("Blocos de notas", CAT_ESCRITA, ATTR_FORMATO, "A5"),
                new MaterialSeedItem("Post-its", CAT_ESCRITA, null, null),
                new MaterialSeedItem("Sublinhador", CAT_ESCRITA, ATTR_COR, "Amarelo"),
                new MaterialSeedItem("Marcadores", CAT_ESCRITA, ATTR_TIPO, "Quadro branco"),
                new MaterialSeedItem("Giz", CAT_ESCRITA, ATTR_COR, "Branco"),
                new MaterialSeedItem("Apagador", CAT_ESCRITA, ATTR_TIPO, "Quadro branco"),
                new MaterialSeedItem("Cartolina", CAT_PAPEL_E_ARQUIVO, ATTR_FORMATO, "A2"),
                new MaterialSeedItem("Papel higiénico", CAT_HIGIENE_E_LIMPEZA, ATTR_TIPO, "Dupla folha"),
                new MaterialSeedItem("Sabonete", CAT_HIGIENE_E_LIMPEZA, ATTR_TIPO, "Liquido"),
                new MaterialSeedItem("Sabonete", CAT_HIGIENE_E_LIMPEZA, ATTR_TIPO, "Solido"),
                new MaterialSeedItem("Álcool", CAT_HIGIENE_E_LIMPEZA, "Concentracao", "70%"),
                new MaterialSeedItem("Detergente", CAT_HIGIENE_E_LIMPEZA, ATTR_TIPO, "Multiusos"),
                new MaterialSeedItem("Desinfetante", CAT_HIGIENE_E_LIMPEZA, ATTR_TIPO, "Superficies"),
                new MaterialSeedItem("Spray limpa-móveis", CAT_HIGIENE_E_LIMPEZA, null, null),
                new MaterialSeedItem("Pano", CAT_HIGIENE_E_LIMPEZA, ATTR_TIPO, "Microfibra"),
                new MaterialSeedItem("Esponja", CAT_HIGIENE_E_LIMPEZA, ATTR_TIPO, "Abrasiva"),
                new MaterialSeedItem("Sacos de lixo", CAT_HIGIENE_E_LIMPEZA, "Capacidade", "50L"),
                new MaterialSeedItem("Ambientadores", CAT_HIGIENE_E_LIMPEZA, "Aroma", "Lavanda"),
                new MaterialSeedItem("Detergente", CAT_HIGIENE_E_LIMPEZA, ATTR_TIPO, "Loiça"),
                new MaterialSeedItem("Tinteiros", "TECNOLOGIA", ATTR_COR, "Preto"),
                new MaterialSeedItem("PenDrive", "TECNOLOGIA", "Capacidade", "32GB"));

        Map<String, Material> materiaisExistentes = materialRepository.findAll().stream()
                .collect(Collectors.toMap(
                        material -> (material.getNome() + "|"
                                + (material.getValorAtributo() != null ? material.getValorAtributo() : ""))
                                .toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (existing, replacement) -> existing)); // Robust merge function

        int count = 0;
        for (MaterialSeedItem seed : materiaisBase) {
            String key = (seed.nome() + "|" + (seed.valorAtributo() != null ? seed.valorAtributo() : ""))
                    .toLowerCase(Locale.ROOT);
            Material existente = materiaisExistentes.get(key);

            if (existente == null) {
                Material material = new Material();
                material.setNome(seed.nome());
                material.setCategoria(seed.categoria());
                material.setAtributo(seed.atributo());
                material.setValorAtributo(seed.valorAtributo());
                materialRepository.save(material);
                count++;
                continue;
            }

            boolean updated = false;
            if (existente.getCategoria() == null || !existente.getCategoria().equals(seed.categoria())) {
                existente.setCategoria(seed.categoria());
                updated = true;
            }
            if (seed.atributo() != null
                    && (existente.getAtributo() == null || !existente.getAtributo().equals(seed.atributo()))) {
                existente.setAtributo(seed.atributo());
                updated = true;
            }

            if (updated) {
                materialRepository.save(existente);
                count++;
            }
        }
        log.info("--- MATERIAL SEED COMPLETED: {} materials processed ---", count);
    }
}
