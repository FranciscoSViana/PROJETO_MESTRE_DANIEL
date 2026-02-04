export class HistoricoOrdemServico {
    id?: string;
    acao?: string;
    descricao?: string;
    dataHora?: string;
    usuario?: {
        id: string;
        nome: string;
        email: string;
    };
}
