# language: pt

Funcionalidade: Soluções de Ordens de Serviço
  Como um usuário autenticado
  Eu quero registrar a solução de uma OS
  Para finalizar o atendimento técnico

  Contexto:
    Dado que o usuário admin está autenticado
    E que existe a estrutura base de dados para OS
    E que existe 1 ordem de serviço cadastrada

  Cenário: Finalizar OS com solução válida
    Quando eu faço POST em "/api/ordens-servico/{id}/solucao" com dados de solução válidos
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "solucao"

  Cenário: Finalizar OS já concluída retorna erro
    Dado que a OS já foi concluída
    Quando eu faço POST em "/api/ordens-servico/{id}/solucao" com dados de solução válidos
    Então a resposta deve ter status 400

  Cenário: Buscar solução de OS existente
    Dado que a OS foi concluída com solução registrada
    Quando eu faço GET em "/api/ordens-servico/{id}/solucao"
    Então a resposta deve ter status 200
    E a resposta deve conter o campo "solucao"
