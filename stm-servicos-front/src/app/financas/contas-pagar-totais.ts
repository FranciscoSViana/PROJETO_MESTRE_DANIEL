export class ContasPagarTotais {

    totalGeral?: number;
    totalPago?: number;
    totalNaoPago?: number;
    qtdTotal?: number;
    qtdPago!: number;
    qtdNaoPago!: number;

    // pago
    pagoValorChamado?: number;
    pagoKmTotal?: number;
    pagoValorKm?: number;
    pagoPedagio?: number;
    pagoEstacionamento?: number;
    pagoOutros?: number;

    // não pago
    naoPagoValorChamado?: number;
    naoPagoKmTotal?: number;
    naoPagoValorKm?: number;
    naoPagoPedagio?: number;
    naoPagoEstacionamento?: number;
    naoPagoOutros?: number;
}
