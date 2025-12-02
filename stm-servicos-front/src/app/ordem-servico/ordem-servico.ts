import { Cliente } from "../clientes/cliente";
import { Credenciado } from "../credenciados/credenciado";
import { Faturamento } from "../faturamento/faturamento";
import { Solucao } from "../solucao/solucao";
import { Endereco } from "./endereco";

export class OrdemServico {

    id?: number;

    osClt?: string;
    osg?: string;
    status?: string;
    rag?: string;

    dataHora?: string; // OffsetDateTime → string no Angular

    clienteId?: string | null;
    credenciadoId?: string | null;

    // Mantendo os objetos parciais
    // cliente?: { razaoSocial?: string };
    // credenciado?: { tecnico?: string };
    cliente?: { id: string; razaoSocial?: string, nome?: string, codigo?: string };
    credenciado?: { id: string; tecnico?: string, codigo?: string };

    // Propriedades auxiliares para exibição
    clienteNome?: string;
    credenciadoNome?: string;

    contrato?: string;
    contato?: string;
    departamento?: string;
    telefone?: string;

    endereco?: Endereco;

    acionador?: string;
    equipamento?: string;
    serie?: string;
    pib?: string;
    defeito?: string;
    rastreio?: string;

    solucao?: Solucao;
    faturamento?: Faturamento;
}
