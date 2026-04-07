package pt.florinhas.requisicoes.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.repository.MaterialRepository;

@Component
@Order(1)
public class MaterialSeed implements CommandLineRunner {

    private final MaterialRepository materialRepository;

    public MaterialSeed(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        record MaterialSeedItem(String nome, String categoria, String atributo, String valorAtributo) {
        }

        List<MaterialSeedItem> materiaisBase = List.of(
                new MaterialSeedItem("Papel", "PAPEL_E_ARQUIVO", "Formato", "A4"),
                new MaterialSeedItem("Papel", "PAPEL_E_ARQUIVO", "Formato", "Fotocopia"),
                new MaterialSeedItem("Esferográfica", "ESCRITA", "Cor", "Azul"),
                new MaterialSeedItem("Esferográfica", "ESCRITA", "Cor", "Preta"),
                new MaterialSeedItem("Esferográfica", "ESCRITA", "Cor", "Vermelha"),
                new MaterialSeedItem("Esferográfica", "ESCRITA", "Cor", "Verde"),
                new MaterialSeedItem("Lápis", "ESCRITA", "Dureza", "HB"),
                new MaterialSeedItem("Lapiseira", "ESCRITA", "Espessura", "0.5mm"),
                new MaterialSeedItem("Minas", "ESCRITA", "Espessura", "0.5mm"),
                new MaterialSeedItem("Borracha", "ESCRITA", "Tipo", "Branca"),
                new MaterialSeedItem("Corretor", "ESCRITA", "Tipo", "Liquido"),
                new MaterialSeedItem("Corretor", "ESCRITA", "Tipo", "Fita"),
                new MaterialSeedItem("Agrafos", "PAPEL_E_ARQUIVO", "Tipo", "26/6"),
                new MaterialSeedItem("Clipes", "PAPEL_E_ARQUIVO", null, null),
                new MaterialSeedItem("Pionés", "PAPEL_E_ARQUIVO", null, null),
                new MaterialSeedItem("Elásticos de borracha", "PAPEL_E_ARQUIVO", null, null),
                new MaterialSeedItem("Micas", "PAPEL_E_ARQUIVO", "Formato", "A4"),
                new MaterialSeedItem("Dossiê", "PAPEL_E_ARQUIVO", "Largura", "A4"),
                new MaterialSeedItem("Fita cola", "PAPEL_E_ARQUIVO", null, null),
                new MaterialSeedItem("Cola", "PAPEL_E_ARQUIVO", "Tipo", "Baton"),
                new MaterialSeedItem("Cola", "PAPEL_E_ARQUIVO", "Tipo", "Liquida"),
                new MaterialSeedItem("Agrafador", "PAPEL_E_ARQUIVO", null, null),
                new MaterialSeedItem("Saca-agrafos", "PAPEL_E_ARQUIVO", null, null),
                new MaterialSeedItem("Tesoura", "PAPEL_E_ARQUIVO", null, null),
                new MaterialSeedItem("Régua", "PAPEL_E_ARQUIVO", "Comprimento", "30cm"),
                new MaterialSeedItem("Furador", "PAPEL_E_ARQUIVO", "Tipo", "2 furos"),
                new MaterialSeedItem("Blocos de notas", "ESCRITA", "Formato", "A5"),
                new MaterialSeedItem("Post-its", "ESCRITA", null, null),
                new MaterialSeedItem("Sublinhador", "ESCRITA", "Cor", "Amarelo"),
                new MaterialSeedItem("Marcadores", "ESCRITA", "Tipo", "Quadro branco"),
                new MaterialSeedItem("Giz", "ESCRITA", "Cor", "Branco"),
                new MaterialSeedItem("Apagador", "ESCRITA", "Tipo", "Quadro branco"),
                new MaterialSeedItem("Cartolina", "PAPEL_E_ARQUIVO", "Formato", "A2"),
                new MaterialSeedItem("Papel higiénico", "HIGIENE_E_LIMPEZA", "Tipo", "Dupla folha"),
                new MaterialSeedItem("Sabonete", "HIGIENE_E_LIMPEZA", "Tipo", "Liquido"),
                new MaterialSeedItem("Sabonete", "HIGIENE_E_LIMPEZA", "Tipo", "Solido"),
                new MaterialSeedItem("Álcool", "HIGIENE_E_LIMPEZA", "Concentracao", "70%"),
                new MaterialSeedItem("Detergente", "HIGIENE_E_LIMPEZA", "Tipo", "Multiusos"),
                new MaterialSeedItem("Desinfetante", "HIGIENE_E_LIMPEZA", "Tipo", "Superficies"),
                new MaterialSeedItem("Spray limpa-móveis", "HIGIENE_E_LIMPEZA", null, null),
                new MaterialSeedItem("Pano", "HIGIENE_E_LIMPEZA", "Tipo", "Microfibra"),
                new MaterialSeedItem("Esponja", "HIGIENE_E_LIMPEZA", "Tipo", "Abrasiva"),
                new MaterialSeedItem("Sacos de lixo", "HIGIENE_E_LIMPEZA", "Capacidade", "50L"),
                new MaterialSeedItem("Ambientadores", "HIGIENE_E_LIMPEZA", "Aroma", "Lavanda"),
                new MaterialSeedItem("Detergente", "HIGIENE_E_LIMPEZA", "Tipo", "Loiça"),
                new MaterialSeedItem("Tinteiros", "TECNOLOGIA", "Cor", "Preto"),
                new MaterialSeedItem("PenDrive", "TECNOLOGIA", "Capacidade", "32GB"));

        Map<String, Material> materiaisExistentes = materialRepository.findAll().stream()
                .collect(Collectors.toMap(
                        material -> (material.getNome() + "|"
                                + (material.getValorAtributo() != null ? material.getValorAtributo() : ""))
                                .toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (existing, ignored) -> existing));

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
            }
        }
    }
}
