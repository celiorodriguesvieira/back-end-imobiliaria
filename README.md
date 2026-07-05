# back-end-imobiliaria

Trabalho da disciplina de Back-end (PUC-PR).

API de uma imobiliária feita em Kotlin com Spring Boot. O modelo é simples: um corretor pode ter vários imóveis (one-to-many).

## Vídeo de apresentação

Explicação do código e demonstração: https://www.youtube.com/watch?v=-ZimDwBQHwo

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
DELETE /corretores/{id}                (autenticado)
DELETE /corretores/{id}/avatar         (autenticado, refaz o avatar)
```

Ao criar um corretor, o servidor tenta definir um avatar automaticamente (ver
seção **Avatares** abaixo).

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

## Avatares

Nenhum corretor fica sem avatar. Ao criar um corretor (`POST /corretores`), o
servidor faz um esforço pra definir um, nesta ordem:

1. Procura um **Gravatar** global pelo hash SHA-256 do e-mail
   (`gravatar.com/avatar/{hash}?d=404`). O `d=404` faz o Gravatar responder 404
   quando o e-mail não tem avatar.
2. Se não houver Gravatar, gera as iniciais do nome via
   [ui-avatars.com](https://ui-avatars.com/) em **PNG**.
3. A imagem escolhida é **baixada e enviada para o S3** — o campo `avatar` do
   corretor guarda a URL do objeto no bucket, não o link das APIs externas.

O endpoint opcional `DELETE /corretores/{id}/avatar` (autenticado) refaz todo
esse processo: apaga o avatar atual do S3 e gera um novo.

### Configuração do S3

As credenciais são lidas de variáveis de ambiente (ver `application.yaml`):

```
AWS_REGION=us-east-1
AWS_S3_BUCKET=nome-do-bucket
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
# opcional, pra apontar pra um S3 local (LocalStack/MinIO):
AWS_S3_ENDPOINT=
```

Enquanto o bucket/credenciais não estiverem configurados, o corretor é criado
normalmente **sem avatar** (`avatar: null`) — o upload é ignorado com um aviso
no log, sem quebrar o cadastro.

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
  avatar/          AvatarService (Gravatar → ui-avatars → S3)
  storage/         S3Config + S3Storage (upload/delete no bucket)
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
