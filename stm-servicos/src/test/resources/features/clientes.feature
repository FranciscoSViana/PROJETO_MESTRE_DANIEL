# language: pt

Funcionalidade: Gestão de Clientes
  Como um usuário autenticado
  Eu quero gerenciar clientes
  Para controlar as empresas contratantes

  Contexto:
    Dado que o usuário admin está autenticado

  Cenário: Criar cliente com dados válidos
    Quando eu faço POST em "/api/clientes" com dados de cliente válidos
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "nome"

  Cenário: Criar cliente sem autenticação retorna 401
    Dado que não estou autenticado
    Quando eu faço POST em "/api/clientes" com dados de cliente válidos
    Então a resposta deve ter status 401

  Cenário: Listar clientes
    Dado que existem 2 clientes cadastrados
    Quando eu faço GET em "/api/clientes"
    Então a resposta deve ter status 200
    E a resposta deve conter uma lista paginada

  Cenário: Buscar cliente por ID existente
    Dado que existe um cliente cadastrado
    Quando eu faço GET em "/api/clientes/{id}" com o ID do cliente criado
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "id"

  Cenário: Buscar cliente por ID inexistente retorna 404
    Quando eu faço GET em "/api/clientes/00000000-0000-0000-0000-000000000000"
    Então a resposta deve ter status 404

  Cenário: Atualizar cliente existente
    Dado que existe um cliente cadastrado
    Quando eu faço PUT em "/api/clientes/{id}" com dados atualizados
    Então a resposta deve ter status 200

  Cenário: Excluir cliente existente
    Dado que existe um cliente cadastrado
    Quando eu faço DELETE em "/api/clientes/{id}"
    Então a resposta deve ter status 204
