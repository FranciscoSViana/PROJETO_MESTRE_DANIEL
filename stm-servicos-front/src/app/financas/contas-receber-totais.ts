export class ContasReceberTotais {
    totalGeral?: number;
    totalRecebido?: number;
    totalNaoRecebido?: number;
    qtdTotal?: number;
    qtdRecebido!: number;
    qtdNaoRecebido!: number;
    // recebido
    recebidoValorChamado?: number;
    recebidoValorKm?: number;
    recebidoPedagio?: number;
    recebidoEstacionamento?: number;
    recebidoOutros?: number;
    // não recebido
    naoRecebidoValorChamado?: number;
    naoRecebidoValorKm?: number;
    naoRecebidoPedagio?: number;
    naoRecebidoEstacionamento?: number;
    naoRecebidoOutros?: number;
}