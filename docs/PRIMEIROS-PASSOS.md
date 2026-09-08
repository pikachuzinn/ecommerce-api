# Primeiros passos

## 1. Rodar e conferir localmente (faça isso antes do push)

```bash
cd ecommerce-api

# sobe só o banco
docker compose up -d db

# compila e roda os testes unitários (não precisam de Docker)
mvn -B test

# build completo + testes de integração (precisam do Docker rodando, por causa do Testcontainers)
mvn -B verify

# sobe a API
mvn spring-boot:run
```

Abra `http://localhost:8080/swagger-ui.html`, clique em **Authorize** e cole o
`accessToken` de `POST /api/v1/auth/login` com `admin@ecommerce.dev` / `Admin@123`.

### Se o build reclamar de alguma dependência

Este projeto usa Spring Boot 4, que ainda é recente. As três coordenadas que valem
conferir primeiro se o `mvn` falhar em resolver algo:

| Dependência | Valor no `pom.xml` | Como verificar |
|---|---|---|
| `spring-boot-starter-parent` | `4.0.6` | https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-parent/versions |
| `springdoc-openapi-starter-webmvc-ui` | `3.0.3` (linha 3.x = Spring Boot 4) | https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui/versions |
| `<java.version>` | `21` | Pode subir para `25` se o seu JDK for o 25 |

Se preferir um terreno mais conhecido, dá para voltar tudo para Spring Boot `3.5.x`
trocando duas coisas: a versão do parent e o starter `spring-boot-starter-webmvc`
de volta para `spring-boot-starter-web` (o Flyway volta a vir junto do `data-jpa`,
então o `spring-boot-starter-flyway` sai).

## 2. Subir no GitHub

```bash
cd ecommerce-api
git init
git add .
git commit -m "feat: API REST de gestão de pedidos com Spring Boot 4"
git branch -M main

# crie o repositório vazio em github.com/new (sem README, sem .gitignore)
git remote add origin https://github.com/pikachuzinn/ecommerce-api.git
git push -u origin main
```

Ou, com o GitHub CLI:

```bash
gh repo create ecommerce-api --public --source=. --remote=origin --push \
  --description "API REST de catálogo e pedidos — Java 21, Spring Boot 4, JPA, JWT, PostgreSQL"
```

## 3. Ajustes finais no repositório

- [ ] Trocar `pikachuzinn` pelo seu usuário do GitHub no `README.md` (badge de CI, clone e link do Actions)
- [ ] Trocar o link de contato em `OpenApiConfig.java`
- [ ] Preencher **About** no GitHub: descrição de uma linha + topics
      (`java`, `spring-boot`, `rest-api`, `jpa`, `postgresql`, `jwt`, `docker`, `testcontainers`)
- [ ] Conferir se o badge de CI ficou verde após o primeiro push
- [ ] Fixar o repositório no perfil (**Customize your pins**)

## 4. Como falar dele em entrevista

Não diga "fiz um CRUD de e-commerce". Escolha um problema concreto e conte a decisão:

> "Modelei o pedido como agregado, com as regras dentro da entidade em vez do
> service. A parte que mais me ensinou foi a baixa de estoque: com dois pedidos
> simultâneos do último item, os dois liam `stock = 1` e passavam na validação.
> Resolvi com `SELECT ... FOR UPDATE` na leitura do produto dentro da transação
> de fechamento, e deixei o `@Version` para o caminho administrativo, onde
> conflito é raro e falhar rápido custa menos que travar a linha."

Outros ganchos bons: por que o preço é copiado para o item, por que herança em
`Payment` em vez de enum com campos opcionais, e por que `ddl-auto: validate`
com Flyway em vez de `update`.
