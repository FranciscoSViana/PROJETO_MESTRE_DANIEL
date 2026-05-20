# language: pt

Funcionalidade: Pagamentos de Credenciados (Contas a Pagar)
  Como um usuário autenticado
  Eu quero gerenciar pagamentos aos credenciados
  Para controlar as contas a pagar

  Contexto:
    Dado que o usuário admin está autenticado
    E que existe a estrutura base de dados para OS
    E que existe 1 ordem de serviço concluída

  Cenário: Registrar pagamento de OS concluída
    Quando eu faço POST em "/api/ordens-servico/{id}/pagamento" com dados de pagamento válidos
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "osg"
    E a resposta deve conter o campo "valorTotal"

  Cenário: Buscar pagamento da OS
    Dado que existe 1 pagamento registrado para a OS
    Quando eu faço GET em "/api/ordens-servico/{id}/pagamento"
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "valorTotal"

  Cenário: Editar pagamento existente
    Dado que existe 1 pagamento registrado para a OS
    Quando eu faço PUT em "/api/ordens-servico/{id}/pagamento" com dados de pagamento atualizados
    Então a resposta deve ter status 200
