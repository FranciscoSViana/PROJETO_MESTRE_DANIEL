# language: pt

Funcionalidade: Autenticação de Usuários
  Como um usuário do sistema
  Eu quero poder me autenticar
  Para acessar as funcionalidades protegidas

  Cenário: Login com credenciais válidas
    Dado que existe um usuário cadastrado com email "admin@sistema.com" e username "admin"
    Quando eu faço POST em "/api/auth/login" com as credenciais do admin
    Então a resposta deve ter status 200
    E a resposta deve conter um "accessToken"
    E a resposta deve conter um "refreshToken"
    E o tipo do token deve ser "Bearer"

  Cenário: Login com senha incorreta
    Dado que existe um usuário cadastrado com email "admin@sistema.com" e username "admin"
    Quando eu faço POST em "/api/auth/login" com senha incorreta
    Então a resposta deve ter status 400

  Cenário: Login com usuário inexistente
    Quando eu faço POST em "/api/auth/login" com username "inexistente" e senha "Senha@123"
    Então a resposta deve ter status 400

  Cenário: Cadastro de novo usuário com dados válidos
    Quando eu faço POST em "/api/auth/cadastro" com dados de usuário válidos
    Então a resposta deve ter status 200
    E a resposta deve conter uma mensagem de sucesso

  Cenário: Cadastro com email já existente
    Dado que existe um usuário cadastrado com email "joao@email.com" e username "joao.silva"
    Quando eu faço POST em "/api/auth/cadastro" com email "joao@email.com" duplicado
    Então a resposta deve ter status 400

  Cenário: Cadastro com senha fraca
    Quando eu faço POST em "/api/auth/cadastro" com senha fraca "123456"
    Então a resposta deve ter status 400

  Cenário: Endpoint de esqueceu a senha retorna 200 sempre
    Quando eu faço POST em "/api/auth/esqueci-senha" com email "qualquer@email.com"
    Então a resposta deve ter status 200

  Cenário: Atualizar token com refresh token válido
    Dado que o usuário admin está autenticado
    Quando eu faço POST em "/api/auth/refresh" com o refresh token obtido no login
    Então a resposta deve ter status 200
    E a resposta deve conter um novo "accessToken"
