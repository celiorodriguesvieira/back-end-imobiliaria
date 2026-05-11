# back-end-imobiliaria

Trabalho da disciplina de Back-end (PUC-PR).

API de uma imobiliária feita em Kotlin com Spring Boot. O modelo é simples: um corretor pode ter vários imóveis (one-to-many).

## Aviso

No momento estou sem microfone, por isso não foi possível gravar o vídeo de apresentação. A documentação abaixo cobre como rodar e testar todos os endpoints.

## Tecnologias

- Kotlin 2.2.21 + JDK 21
- Spring Boot 4.0.6 (Web, Data JPA, Security, Validation)
- H2 em memória (não precisa instalar nada)
- Swagger (springdoc-openapi)

## Como rodar

```
./gradlew bootRun
```

A aplicação sobe em `http://localhost:8080/api`.

- Swagger: http://localhost:8080/api/swagger-ui/index.html
- Console do H2: http://localhost:8080/api/h2-console
  - JDBC URL: `jdbc:h2:mem:db`
  - User: `sa` / Senha: `sa`

Pras requisições autenticadas (PUT e DELETE), o login da API é `admin` / `admin123`.

## Endpoints

### Corretor

```
POST   /corretores
GET    /corretores
GET    /corretores/{id}
DELETE /corretores/{id}        (autenticado)
```

### Imóvel

```
POST   /imoveis
GET    /imoveis                (aceita filtros, ver abaixo)
GET    /imoveis/{id}
PUT    /imoveis/{id}                              (autenticado)
DELETE /imoveis/{id}                              (autenticado)
PUT    /imoveis/{id}/corretor/{corretorId}        (autenticado)
DELETE /imoveis/{id}/corretor                     (autenticado)
```

### Filtros e ordenação

No `GET /imoveis` dá pra combinar os parâmetros (todos opcionais):

- `tipo` — CASA, APARTAMENTO, TERRENO, COMERCIAL
- `bairro` — busca parcial, sem diferenciar maiúsculas
- `precoMin` / `precoMax`
- `corretorId`
- `sortBy` — id, preco, area, quartos ou titulo (default: id)
- `sortDir` — ASC ou DESC (default: ASC)

Exemplo:

```
GET /imoveis?tipo=APARTAMENTO&bairro=Batel&precoMin=300000&sortBy=preco&sortDir=DESC
```

## Validações

Os services validam as entradas e lançam `BadRequestException` ou `NotFoundException`. Quem trata é o `GlobalExceptionHandler`, que devolve um JSON padrão:

```json
{ "status": 400, "message": "Preço deve ser maior que zero", "timestamp": "..." }
```

O que está coberto:

- E-mail de corretor já cadastrado — 400
- Preço de imóvel zero ou negativo — 400
- `sortBy` fora dos campos permitidos — 400
- `sortDir` diferente de ASC ou DESC — 400
- Apagar corretor que ainda tem imóveis ligados — 400
- Tirar corretor de um imóvel que não tem nenhum — 400
- Buscar id que não existe — 404
- PUT ou DELETE sem login — 401

## Estrutura do projeto

```
src/main/kotlin/br/pucpr/auth/
  corretores/      Corretor + Repository + Service + Controller
  imoveis/         Imovel, TipoImovel, Repository, Service, Controller
  exception/       Exceptions e GlobalExceptionHandler
  security/        SecurityConfig (Basic Auth com BCrypt)
  AuthApplication.kt
```

## Logs

Usei `LoggerFactory` nos dois services principais. Algumas mensagens que aparecem no console:

```
INFO  Imóvel criado: id=1 titulo=Apto Centro 2 quartos
WARN  Tentativa de cadastro com e-mail duplicado: joao@imob.com
INFO  Corretor 2 atribuído ao imóvel 4
WARN  Tentativa de remover corretor 2 com 2 imóveis vinculados
```

## Como testar rápido

Com a app rodando, abre o Swagger e segue:

1. Cria dois corretores em `POST /corretores`.
2. Cria três ou quatro imóveis em `POST /imoveis`.
3. Tenta `GET /imoveis?tipo=APARTAMENTO&sortBy=preco&sortDir=DESC`.
4. No canto direito do Swagger, clica em "Authorize" e usa `admin` / `admin123`.
5. Liga um corretor a um imóvel com `PUT /imoveis/1/corretor/1`.
6. Tenta apagar esse corretor pra ver a validação retornando 400.

Pra ver os dados direto no banco, no H2 Console:

```sql
SELECT i.id, i.titulo, i.bairro, i.preco, i.tipo, c.nome AS corretor
FROM imoveis i
LEFT JOIN corretores c ON i.corretor_id = c.id
ORDER BY i.preco DESC;
```
