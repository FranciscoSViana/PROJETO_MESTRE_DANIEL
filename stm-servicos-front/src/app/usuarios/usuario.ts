export class Usuario {
    id?: string;
    nomeCompleto?: string;
    username?: string;
    nome?: string;         // mantido por compatibilidade
    email?: string;
    senha?: string;
    dataNascimento?: string; // ISO date string: "1990-05-15"
    idade?: number;
    roles: string[] = [];
    enabled?: boolean = true;
    createdAt?: Date;
}