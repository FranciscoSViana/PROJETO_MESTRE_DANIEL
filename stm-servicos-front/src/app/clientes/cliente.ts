import { Endereco } from "../ordem-servico/endereco";
import { Contrato } from "./contrato";

export class Cliente {

    id?: string;
    codigo?: number;
    contratos?: Contrato[];
    nome?: string;
    valorChamado?: number;
    valorKm?: number;
    cnpj?: string;
    inscricaoEstadual?: string;
    razaoSocial?: string;
    endereco?: Endereco;
}
