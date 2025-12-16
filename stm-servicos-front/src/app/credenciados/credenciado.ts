import { Endereco } from '../ordem-servico/endereco';
import { Tecnico } from './tecnico';

export class Credenciado {

    id?: string;
    codigo?: string;
    rag?: string;
    tipoPessoa?: string;
    numeroPessoa?: string;
    valorChamado?: number | null;
    valorKm?: number | null;
    quantidadeOSAtendidas?: number;
    contato?: string;
    telefones?: string;
    email?: string;
    tecnicos?: Tecnico[];
    endereco?: Endereco;
}
