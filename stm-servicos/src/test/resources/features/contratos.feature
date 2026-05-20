# language: pt

Funcionalidade: Gestão de Contratos
  Como um usuário autenticado
  Eu quero gerenciar contratos de clientes
  Para controlar os termos de atendimento

  Contexto:
    Dado que o usuário admin está autenticado
    E que existe um cliente cadastrado

  Cenário: Adicionar contrato a um cliente
    Quando eu faço POST em "/api/clientes/{clienteId}/contratos" com dados de contrato válidos
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "id"

  Cenário: Listar contratos do cliente
    Dado que existe 1 contrato para o cliente
    Quando eu faço GET em "/api/clientes/{clienteId}/contratos"
    Então a resposta deve ter status 200
    E a resposta deve ser uma lista

  Cenário: Excluir contrato
    Dado que existe 1 contrato para o cliente
    Quando eu faço DELETE em "/api/clientes/{clienteId}/contratos/{contratoId}"
    Então a resposta deve ter status 204
