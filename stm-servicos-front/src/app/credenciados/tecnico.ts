import { Endereco } from "../ordem-servico/endereco";

export class Tecnico {

    id?: string;        // UUID
    codigo?: string;    // Long

    nome?: string;
    cpf?: string;

    telefone?: string;
    email?: string;

    endereco?: Endereco;
}
