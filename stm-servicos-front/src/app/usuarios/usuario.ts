export class Usuario {

    id?: string;           // equivalente ao Long do Java
    nome?: string;
    email?: string;
    senha?: string;
    roles: string[] = [];  // Set<String> do Java → string[]
    enabled?: boolean = true;
    createdAt?: Date;
}
