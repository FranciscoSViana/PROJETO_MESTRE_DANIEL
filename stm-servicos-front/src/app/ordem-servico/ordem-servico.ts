import { Cliente } from "../clientes/cliente";
import { Faturamento } from "../faturamento/faturamento";
import { Solucao } from "../solucao/solucao";
import { Endereco } from "./endereco";

export class OrdemServico {

    id?: string;

    osClt?: string;
    osg?: string;
    status?: string;

    dataHora?: string; // OffsetDateTime → string no Angular

    clienteId?: string | null;
    credenciadoId?: string | null;
    tecnicoId?: string | null;

    // Mantendo os objetos parciais
    cliente?: Cliente;
    credenciado?: { id: string; rag?: string, codigo?: string };
    tecnico?: { id: string; nome?: string };

    // Propriedades auxiliares para exibição
    clienteNome?: string;
    credenciadoNome?: string;

    contrato?: { id: string; descricao?: string };
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
