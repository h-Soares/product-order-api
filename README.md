# API Restful de pedidos e produtos

[![Continuous Integration with GitHub Action](https://github.com/h-Soares/product-order-api/actions/workflows/continuous-integration.yml/badge.svg)](https://github.com/h-Soares/product-order-api/actions/workflows/continuous-integration.yml)
[![Docker Hub Repo](https://img.shields.io/docker/pulls/hsoaress/product-order-api-img.svg)](https://hub.docker.com/repository/docker/hsoaress/product-order-api-img)

## 📌 Versão
1.0.0

## 👨‍💻 Autor
* <div style="display: flex; align-items: center;">
    <p style="margin: 0; font-size: 18px;">Hiago Soares | </p>
    <a href="https://www.linkedin.com/in/hiago-soares-96840a271/" style="margin: 10px; margin-top: 15px">
        <img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn Badge">
    </a>
</div>

## 🔎 Sobre o projeto
Esta é uma API Restful de pedidos e produtos que disponibiliza endpoints para realizar operações de CRUD (Create, Read, Update e Delete).

O sistema permite realizar ações sobre usuários e pedidos, adicionar categorias a produtos, adicionar items de produtos a um pedido, realizar pagamentos de pedidos, entre outros...

Utiliza validação de objetos e dos casos de uso, paginação, tratamento centralizado de erros, criptografia de senhas, otimização de consultas SQL para evitar o problema N + 1, versionamento da API, Content Negotiation para os formatos JSON e XML, autenticação com token JWT, verificação de roles e testes.


## 🛠️ Tecnologias utilizadas
* Maven
* ModelMapper
* Java 17
* Spring Boot 3.0.6
* Spring Web
* Spring Security: autenticação com Token JWT
* Spring Data JPA / Hibernate
* Java Bean Validation
* Testes com JUnit 5 e Mockito
* PostgreSQL
* Swagger
* Docker

## 🔧 Instalação

1. Clone o repositório

````bash
git clone https://github.com/h-Soares/product-order-api.git
````

2. Navegue até o diretório do projeto

```bash
cd product-order-api
```

3. Inicialize o banco de dados PostgreSQL na porta 5432.

4. Construa a aplicação:
```bash
mvn clean install
```
O comando irá baixar todas as dependências do projeto e criar um diretório target com os artefatos construídos, que incluem o arquivo jar do projeto. Além disso, serão executados todos os testes, e, se algum falhar, o Maven exibirá essa informação no console.

5. Execute a aplicação:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-DPOSTGRES_DB=NOME_BANCO -DPOSTGRES_USER=SEU_USER -DPOSTGRES_PASSWORD=SUA_SENHA -DSECRET_KEY=SUA_CHAVE"
```
A porta utilizada é a padrão: 8080.

## 🐳 Docker
Para utilizar a aplicação via Docker, siga os passos:
1. Baixar a imagem
````bash
docker pull hsoaress/product-order-api-img:latest
````

2. Inicializar o contêiner, substituindo os campos
(a aplicação roda na porta 8080)
````bash
docker run -p SUA_PORTA:8080 -e SECRET_KEY=SUA_CHAVE hsoaress/product-order-api-img:latest
````

## 🧪 Testes
Para executar todos os testes:
```bash
mvn test
```

## 📖 Documentação com Swagger (OpenAPI)
Com o projeto instalado, para acessar a documentação, vá até:

`http://localhost:SUA_PORTA/swagger-ui/index.html`

## 🚀 Deploy (FORA DO AR)
* https://product-order-api.onrender.com/swagger-ui/index.html
