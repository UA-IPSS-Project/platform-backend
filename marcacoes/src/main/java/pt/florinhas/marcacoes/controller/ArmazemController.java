package pt.florinhas.marcacoes.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pt.florinhas.marcacoes.dto.ConsumoEstatisticaDTO;
import pt.florinhas.marcacoes.dto.ItemArmazemDTO;
import pt.florinhas.marcacoes.service.ArmazemService;

/**
 * Controller REST para gestão do armazém do Balneário.
 *
 * Endpoints:
 * - GET     /api/armazem             — listar todo o inventário
 * - POST    /api/armazem             — criar novo item
 * - GET     /api/armazem/categoria   — listar por categoria
 * - PUT     /api/armazem/{id}        — atualizar item (quantidade, nome, etc)
 * - DELETE  /api/armazem/{id}        — eliminar item
 * - POST    /api/armazem/stock-check — verificar stock para itens do formulário
 * - POST    /api/armazem/stock-check/calcado — verificar stock de calçado por tamanho
 * - GET     /api/armazem/estatisticas — dados de consumo agregados
 */
@RestController
@RequestMapping("/api/armazem")
@RequiredArgsConstructor
public class ArmazemController {

    private final ArmazemService armazemService;

    /**
     * Lista todos os itens do armazém ordenados por categoria e nome.
     */
    @GetMapping
    public ResponseEntity<List<ItemArmazemDTO>> listarTodos() {
        List<ItemArmazemDTO> itens = armazemService.listarTodos();
        return ResponseEntity.ok(itens);
    }

    /**
     * Lista itens de uma categoria específica.
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ItemArmazemDTO>> listarPorCategoria(@PathVariable String categoria) {
        List<ItemArmazemDTO> itens = armazemService.listarPorCategoria(categoria.toUpperCase());
        return ResponseEntity.ok(itens);
    }

    /**
     * Cria um novo item no armazém.
     */
    @PostMapping
    @PreAuthorize("hasRole('SECRETARIA') or hasRole('BALNEARIO')")
    public ResponseEntity<ItemArmazemDTO> criarItem(@RequestBody ItemArmazemDTO dto) {
        ItemArmazemDTO created = armazemService.criarItem(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Atualiza um item (quantidade, nome, categoria, etc).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA') or hasRole('BALNEARIO')")
    public ResponseEntity<ItemArmazemDTO> atualizarItem(
            @PathVariable Long id,
            @RequestBody ItemArmazemDTO dto) {

        ItemArmazemDTO updated = armazemService.atualizarItem(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina um item do armazém.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA') or hasRole('BALNEARIO')")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        armazemService.eliminarItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica os níveis de stock para itens do formulário de marcação.
     * Recebe uma lista de categorias do formulário e retorna o estado de cada um.
     */
    @PostMapping("/stock-check")
    public ResponseEntity<Map<String, Map<String, Object>>> verificarStock(
            @RequestBody List<String> formItems) {

        Map<String, Map<String, Object>> resultado = armazemService.verificarStock(formItems);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Verifica stock de calçado por tamanhos específicos.
     */
    @PostMapping("/stock-check/calcado")
    public ResponseEntity<Map<String, Map<String, Object>>> verificarStockCalcado(
            @RequestBody List<String> tamanhos) {

        Map<String, Map<String, Object>> resultado = armazemService.verificarStockCalcado(tamanhos);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtém estatísticas de consumo para um período.
     *
     * @param periodo DIA, SEMANA ou MES
     */
    @GetMapping("/estatisticas")
    @PreAuthorize("hasRole('SECRETARIA') or hasRole('BALNEARIO')")
    public ResponseEntity<ConsumoEstatisticaDTO> obterEstatisticas(
            @RequestParam(defaultValue = "MES") String periodo) {

        ConsumoEstatisticaDTO stats = armazemService.obterEstatisticas(periodo);
        return ResponseEntity.ok(stats);
    }
}
