# language: pt

Funcionalidade: Contas a Receber
  Como um usuário autenticado
  Eu quero visualizar as contas a receber
  Para controle financeiro dos recebimentos dos clientes

  Contexto:
    Dado que o usuário admin está autenticado

  Cenário: Listar contas a receber sem filtros
    Quando eu faço GET em "/api/financeiro/contas-receber"
    Então a resposta deve ter status 200
    E a resposta deve conter uma lista paginada

  Cenário: Buscar totais das contas a receber
    Quando eu faço GET em "/api/financeiro/contas-receber/totais"
    Então a resposta deve ter status 200

  Cenário: Listar lotes de recebimento
    Quando eu faço GET em "/api/financeiro/contas-receber/lotes"
    Então a resposta deve ter status 200
    E a resposta deve ser uma lista

  Cenário: Exportar contas a receber em XLSX
    Quando eu faço GET em "/api/financeiro/contas-receber/exportar/xlsx"
    Então a resposta deve ter status 200
    E o content-type deve indicar planilha Excel
