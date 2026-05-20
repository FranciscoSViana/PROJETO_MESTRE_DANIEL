# language: pt

Funcionalidade: Gestão de Credenciados
  Como um usuário autenticado
  Eu quero gerenciar credenciados
  Para controlar os prestadores de serviço

  Contexto:
    Dado que o usuário admin está autenticado

  Cenário: Criar credenciado com dados válidos
    Quando eu faço POST em "/api/credenciados" com dados de credenciado válidos
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "rag"

  Cenário: Listar credenciados
    Dado que existe 1 credenciado cadastrado
    Quando eu faço GET em "/api/credenciados"
    Então a resposta deve ter status 200
    E a resposta deve conter uma lista paginada

  Cenário: Buscar credenciado por ID
    Dado que existe 1 credenciado cadastrado
    Quando eu faço GET em "/api/credenciados/{id}"
    Então a resposta deve ter status 200

  Cenário: Excluir credenciado
    Dado que existe 1 credenciado cadastrado
    Quando eu faço DELETE em "/api/credenciados/{id}"
    Então a resposta deve ter status 204
