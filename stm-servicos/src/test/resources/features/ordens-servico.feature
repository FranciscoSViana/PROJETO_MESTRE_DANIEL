# language: pt

Funcionalidade: Gestão de Ordens de Serviço
  Como um usuário autenticado
  Eu quero gerenciar ordens de serviço
  Para controlar os atendimentos técnicos

  Contexto:
    Dado que o usuário admin está autenticado
    E que existe a estrutura base de dados para OS

  Cenário: Criar ordem de serviço com dados válidos
    Quando eu faço POST em "/api/ordens-servico" com dados de OS válidos
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "osg"

  Cenário: Criar OS sem autenticação retorna 401
    Dado que não estou autenticado
    Quando eu faço POST em "/api/ordens-servico" com dados de OS válidos
    Então a resposta deve ter status 401

  Cenário: Listar ordens de serviço
    Dado que existe 1 ordem de serviço cadastrada
    Quando eu faço GET em "/api/ordens-servico"
    Então a resposta deve ter status 200
    E a resposta deve conter uma lista paginada

  Cenário: Buscar OS por ID existente
    Dado que existe 1 ordem de serviço cadastrada
    Quando eu faço GET em "/api/ordens-servico/{id}"
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "osg"

  Cenário: Obter próximo número OSG
    Quando eu faço GET em "/api/ordens-servico/proximo-osg"
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "osg"

  Cenário: Atualizar status de rastreio
    Dado que existe 1 ordem de serviço cadastrada
    Quando eu faço PATCH em "/api/ordens-servico/{id}/rastreio" com status válido
    Então a resposta deve ter status 200

  Cenário: Excluir ordem de serviço
    Dado que existe 1 ordem de serviço cadastrada
    Quando eu faço DELETE em "/api/ordens-servico/{id}"
    Então a resposta deve ter status 204

  Cenário: Listar histórico da OS
    Dado que existe 1 ordem de serviço cadastrada
    Quando eu faço GET em "/api/ordens-servico/{id}/historico"
    Então a resposta deve ter status 200
    E a resposta deve ser uma lista
