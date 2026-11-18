# Página Fiscal do GEx — calculadora CBS/IBS

A página **Fiscal** do GEx (menu Gerencial → Fiscal) usa a calculadora oficial da Receita Federal,
[ReceitaFederal/calculadora-rtc-regime-regular](https://github.com/ReceitaFederal/calculadora-rtc-regime-regular)
(licença MIT). Essa calculadora é uma API Java que roda fora do GEx: no seu computador ou num servidor.

Sem a API, a página faz só uma **estimativa local** de itens com tributação integral (CST 000), usando as alíquotas de
2026 (CBS 0,9%, IBS 0,1%) ou as alíquotas que você informar a partir de 2027. Itens com redução, isenção ou diferimento
precisam da API.

## O que a página faz

- **Calculadora:** monta a operação com os produtos do catálogo (a NCM e o preço da tabela CF ou Distribuidor da UF são preenchidos automaticamente), escolhe o CST e a classificação tributária (cClassTrib) e calcula CBS, IBS estadual e IBS municipal por item, com a memória de cálculo da Receita.
- **XML:** baixa o grupo de tributação IBS/CBS da NF-e, já validado no XSD oficial.
- **Consultas:** NCM, classificação tributária, se a NCM é aceita numa classificação, alíquotas de referência e códigos de erro.
- **Conferir NCMs:** confere a descrição oficial de todas as NCMs do catálogo do GEx.

## Por que existe o `CorsConfig.java`

A API da Receita não libera chamadas vindas do navegador a partir de outro domínio (CORS). O GEx roda no navegador,
então sem esse arquivo todas as chamadas são bloqueadas (HTTP 403 no *preflight*). O `CorsConfig.java` só adiciona essa
liberação e não muda nenhum cálculo.

## Rodar no seu computador

Requisito: Java 21 ou mais novo.

```bash
git clone https://github.com/ReceitaFederal/calculadora-rtc-regime-regular.git
cd calculadora-rtc-regime-regular
cp /caminho/do/Gex/fiscal-api/CorsConfig.java src/main/java/br/gov/serpro/rtc/config/security/
mkdir -p ./calculadora/db
./mvnw flyway:migrate -Dflyway.configFiles=./flyway/flyway-pro.conf --batch-mode --errors
./mvnw spring-boot:run -Dspring-boot.run.profiles=offline
```

No Windows, use `mvnw.cmd` e `mkdir calculadora\db`.

Depois abra o GEx → Fiscal → Conexão, deixe `http://localhost:8080/api/calculadora` e clique em **Salvar e testar**.

## Rodar num servidor (para a equipe toda)

```bash
./mvnw clean package -DskipTests
GEX_CORS_ORIGINS=https://endereco-do-gex.com.br java -Dspring.profiles.active=offline -jar target/api-regime-geral.jar
```

- Rode o comando na raiz do projeto, onde fica `./calculadora/db`.
- `GEX_CORS_ORIGINS` limita quais sites podem chamar a API; sem ele, qualquer origem é aceita.
- Se o GEx é aberto em HTTPS, a API também precisa estar em HTTPS (por exemplo, atrás de um proxy reverso com
  certificado). A única exceção é `localhost`, que o navegador aceita.
- A URL configurada fica salva só no navegador de cada usuário (aba Conexão).

## Observações

- A API responde HTTP 200 mesmo quando um item tem erro; a página mostra o motivo item a item.
- A Receita só cadastra alíquotas de referência para 2026. A partir de 2027 a página pede as alíquotas nominais.
- Ao atualizar o repositório da Receita, gere o banco de novo com o comando `flyway:migrate`.
