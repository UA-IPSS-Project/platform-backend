package pt.florinhas.requisicoes.dto;

public record ManutencaoItemRequest(
        Long itemId,
        String observacoes) {
}
