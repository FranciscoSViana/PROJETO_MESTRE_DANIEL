export class Faturamento {

    id?: number;

    // Ordem de serviço não vem dentro do payload normalmente
    ordemServicoId?: number;

    cliente?: string;
    contrato?: string;
    statusOs?: string;
    rag?: string;

    chamado?: string;
    deslocamento?: number;
    ttlKm?: number;
    pedagios?: number;
    estac?: number;
    outros?: string;

    total?: number;
    doc?: string;
    status?: string;

    km?: number;

    totalGeral?: number;
    notaFiscal?: string;

    faturado?: number;
    saldoMO?: number;
    saldoKm?: number;
    saldoOutros?: number;
    imposto?: number;
    saldoTotal?: number;
}
