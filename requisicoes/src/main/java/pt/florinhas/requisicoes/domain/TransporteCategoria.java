package pt.florinhas.requisicoes.domain;

public enum TransporteCategoria {

    // Ligeiros
    LIGEIRO_DE_PASSAGEIROS,
    LIGEIRO_DE_MERCADORIAS,
    LIGEIRO_MISTO,
    LIGEIRO_ESPECIAL,

    // Pesados
    PESADO_DE_PASSAGEIROS,
    PESADO_DE_MERCADORIAS,
    PESADO_MISTO,

    // Especiais
    ADAPTADO,          // mobilidade reduzida
    ESCOLAR,           // transporte de crianças
    AMBULANCIA,
    TRACTOR,
    OUTRO,

    // Deprecated - Manter para compatibilidade com dados existentes
    @Deprecated LIGEIRO,
    @Deprecated PESADO,
    @Deprecated PASSAGEIROS
}
