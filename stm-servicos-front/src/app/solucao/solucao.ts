export class Solucao {

    id?: number;

    // Id da Ordem de Serviço (em TS usamos ID e não o objeto inteiro)
    ordemServicoId?: number;

    tecnico?: string;

    dataVisita?: string;   // OffsetDateTime → string
    inicio?: string;
    termino?: string;

    solucao?: string;

    km?: number;
    pedagios?: number;
    estac?: number;
    outros?: string;
}
