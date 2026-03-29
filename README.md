# Grão Mestre – Backend

API REST do sistema **Grão Mestre**, uma plataforma completa para gestão de uma cafeteria / e-commerce de cafés especiais.

Este backend é responsável por autenticação, cadastro de usuários, gestão de produtos, categorias, endereços, pedidos, despesas, relatórios financeiros e integração com pagamentos (Mercado Pago), atendendo tanto o painel web administrativo quanto o app mobile dos clientes.

---

## 🏗 Arquitetura & Stack

- **Linguagem:** Java (versão 17+ recomendada)
- **Framework:** Spring Boot
- **Principais módulos Spring:**
  - Spring Web
  - Spring Data JPA
  - Spring Security (JWT)
  - Spring Validation
- **Banco de Dados:** MySQL
- **Build:** Maven ou Gradle (ajustar conforme seu projeto)
- **Outros:**
  - Integração com **Mercado Pago** para pagamentos (PIX, Cartão)
  - Envio de e-mails (ativação de conta, recuperação de senha)
  - Tratamento global de exceções

A estrutura segue uma organização em camadas:

- `controller` – exposição de endpoints REST
- `service` – regras de negócio
- `repository` – acesso a dados (Spring Data JPA)
- `model/entity` – entidades de domínio
- `dto` – objetos de transferência de dados (request/response)
- `config` – segurança, CORS, integrações
- `exception` – tratamentos e mapeamento de erros

---

## 🔐 Autenticação & Autorização

O sistema utiliza **JWT** com Spring Security.

- Registro de usuário com envio de e-mail de ativação (token de verificação).
- Login com e-mail e senha, retorno de **access token**.
- Escopos/papéis (ex.: `ADMIN`, `USER`) controlando acesso a rotas sensíveis.
- Filtros de segurança configurados em `SecurityConfig` e utilitários em `SecurityUtils`.

Endpoints típicos:

- `POST /auth/register` – cria usuário e envia e-mail de ativação
- `GET /auth/activate?token=...` – ativa conta a partir do link enviado
- `POST /auth/login` – autenticação, retorna JWT
- `POST /auth/forgot-password` – geração de link de redefinição de senha
- `POST /auth/reset-password` – redefine senha com token válido

---

## 📦 Domínio Principal

### Usuários & Perfis

- Cadastro, consulta e atualização de usuários
- Perfis com escopos (ex.: `ADMIN`, `USER`)
- Associação com endereços
- Rotas protegidas por papel

Principais endpoints:

- `GET /users/me` – dados do usuário autenticado
- `GET /users/{id}` – busca por ID (ADMIN)
- `PUT /users/{id}` – atualização de dados
- `PUT /users/{id}/password` – alteração de senha autenticado
- `POST /users/password/reset` – redefinição via token

---

### Endereços

Cada usuário pode ter endereços cadastrados (ex.: endereço de entrega), com possibilidade de marcar um endereço padrão.

Exemplos:

- `GET /addresses/user/{userId}` – lista endereços do usuário
- `POST /addresses` – cria endereço
- `PUT /addresses/{id}` – atualiza
- `DELETE /addresses/{id}` – remove

---

### Categorias

Servem para organizar os produtos no catálogo.

- `GET /categories` – lista todas
- `POST /categories` – cria (ADMIN)
- `PUT /categories/{id}` – atualiza (ADMIN)
- `DELETE /categories/{id}` – remove (ADMIN)

---

### Produtos

Gestão completa de produtos (ex.: cafés, acessórios, etc.).

- Informações: nome, descrição, preço, categoria, imagem, etc.
- Integração com serviço de armazenamento de imagens (ex.: `ImageStorageService`).

Principais endpoints:

- `GET /products` – lista todos (com possíveis filtros/paginação)
- `GET /products/{id}` – detalhes
- `POST /products` – cadastro (ADMIN)
- `PUT /products/{id}` – atualização (ADMIN)
- `DELETE /products/{id}` – remoção (ADMIN)

---

### Pedidos (Orders)

Fluxo completo de pedidos, desde o carrinho até a conclusão.

- Criação de pedido a partir dos itens de carrinho
- Persistência do histórico de preços (`priceAtTime`)
- Atualização de status:
  - `PENDING`
  - `PROCESSING`
  - `PAID`
  - `SENDED`
  - `COMPLETED`
  - `CANCELED`
  - `RECUSE` (recusado)
- Relacionamento com pagamento (`Payment`)

Endpoints principais (exemplos):

- `GET /orders/my` – lista pedidos do usuário autenticado
- `GET /orders/my?status=PENDING` – buscar pedido pendente (carrinho)
- `POST /orders` – cria pedido
- `PUT /orders/{orderId}/status` – atualiza status (ADMIN)
- `DELETE /orders/{orderId}/items/{itemId}` – remove item
- `PUT /orders/{orderId}/items/{itemId}` – atualiza quantidade

Também há endpoints de **filtro e paginação** para uso no painel administrativo:

- `GET /orders/filter` – permite filtrar por:
  - status
  - período (hoje, ontem, semana atual, mês, intervalo customizado)
  - usuário
  - ID do pedido
  - ordenação por data/valor

---

### Pagamentos & Mercado Pago

Integração com **Mercado Pago** para checkout.

- `MercadoPagoConfig` – configurações de credenciais/SDK.
- `MercadoPagoService` – criação de preferência de pagamento, leitura de status.
- `MercadoPagoWebhookController` – recebe notificações de pagamento (webhook).

Fluxo resumido:

1. Front chama `POST /orders/{orderId}/checkout` (ou equivalente).
2. Backend cria preferência de pagamento no Mercado Pago e retorna `paymentUrl` para o front.
3. Cliente é redirecionado para o ambiente de pagamento.
4. Webhook do Mercado Pago notifica o backend do status.
5. Backend atualiza o `Payment` e o `OrderStatus` de acordo com o retorno (pago, recusado, etc.).

---

### Despesas & Relatórios Financeiros

Módulos voltados ao administrador para controle de caixa e lucratividade.

**Despesas (`Expense`):**

- `GET /expenses` – lista despesas
- `POST /expenses` – cria despesa
- `PUT /expenses/{id}` – atualiza
- `DELETE /expenses/{id}` – remove

**Relatórios (`FinancialReport`):**

- Consolidação de:
  - faturamento em período
  - quantidade de pedidos
  - ticket médio
  - somatório de despesas
  - lucro estimado

Exemplo de endpoints:

- `GET /reports/financial?period=THIS_MONTH`
- `GET /reports/financial?startDate=...&endDate=...`

---

## ⚙️ Configuração & Execução

### 1. Pré-requisitos

- Java 17+ instalado
- Maven ou Gradle
- Banco de dados rodando (ex.: Postgres/MySQL)
- Credenciais do Mercado Pago (sandbox ou produção)
- SMTP configurado para envio de e-mails (ativação de conta, reset de senha)

### 2. Variáveis de ambiente / application.properties

Configure o arquivo `application.properties` (ou `application.yml`) com as informações:

```properties
# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/grao_mestre
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update

# JWT
app.security.jwt.secret=uma-chave-secreta-bem-grande
app.security.jwt.expiration=3600000

# Mercado Pago
mercadopago.access-token=SEU_ACCESS_TOKEN
mercadopago.webhook-url=https://sua-url.com/webhook/mercadopago

# Email
spring.mail.host=smtp.seuprovedor.com
spring.mail.port=587
spring.mail.username=seu_email
spring.mail.password=sua_senha
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 3. Rodando a aplicação

Com Maven:

```bash
mvn spring-boot:run
```

Ou gerando o jar:

```bash
mvn clean package
java -jar target/GraoMestre.jar
```

A API deve subir (por padrão) em:

```text
http://localhost:8080
```

---

## 📱 Integração com Front-end Web & Mobile

Este backend é consumido por:

- **Front-end Web (React/TypeScript)** – painel administrativo + loja
- **App Mobile (React Native/Expo)** – app do cliente final

Os clientes consomem a API via:

- `Authorization: Bearer <token>` para rotas autenticadas
- Respostas em JSON padronizadas via DTOs
- Paginação em endpoints de listagem (`PageableResponse`)

---

## 📌 Próximos Passos / Possíveis Melhorias

- Testes automatizados mais abrangentes (unitários e integração).
- Logs estruturados e observabilidade (ex.: Spring Boot Actuator).
- Rate limiting / proteção extra para endpoints sensíveis.
- Internacionalização (i18n) das mensagens de erro.

---


## ✨ Autor

Projeto desenvolvido por **Lucas Liaw** como aplicação full stack (backend + frontend web + mobile), com foco em arquitetura limpa, boas práticas de engenharia e integração real com meios de pagamento.
