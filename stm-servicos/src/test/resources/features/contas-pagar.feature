# language: pt

Funcionalidade: Contas a Pagar
  Como um usuário autenticado
  Eu quero visualizar as contas a pagar
  Para controle financeiro dos pagamentos aos credenciados

  Contexto:
    Dado que o usuário admin está autenticado

  Cenário: Listar contas a pagar sem filtros
    Quando eu faço GET em "/api/financeiro/contas-pagar"
    Então a resposta deve ter status 200
    E a resposta deve conter uma lista paginada

  Cenário: Buscar totais das contas a pagar
    Quando eu faço GET em "/api/financeiro/contas-pagar/totais"
    Então a resposta deve ter status 200

  Cenário: Listar lotes de pagamento
    Quando eu faço GET em "/api/financeiro/contas-pagar/lotes"
    Então a resposta deve ter status 200
    E a resposta deve ser uma lista

  Cenário: Exportar contas a pagar em XLSX
    Quando eu faço GET em "/api/financeiro/contas-pagar/exportar/xlsx"
    Então a resposta deve ter status 200
    E o content-type deve indicar planilha Excel
