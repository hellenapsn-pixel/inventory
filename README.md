# Inventory 🎮

O **Inventory** é uma plataforma web para organizar, avaliar e descobrir jogos. A proposta é funcionar como uma biblioteca pessoal de jogos, permitindo que cada usuário registre seus jogos, acompanhe avaliações e interaja com outros usuários.

O projeto foi desenvolvido como **TCC**, utilizando uma arquitetura web em Java e separando as responsabilidades entre interface, Servlets, Models e DAOs.

## ✨ Funcionalidades

- Cadastro e autenticação de usuários
- Login com Google
- Proteção de senhas com BCrypt
- Recuperação/verificação de conta por e-mail
- Cadastro e gerenciamento de jogos
- Biblioteca pessoal de jogos
- Avaliação de jogos
- Favoritar jogos
- Listas de jogos
- Perfil de usuário
- Seguir outros usuários
- Feed de atividades
- Busca e filtros de jogos
- Exibição de capas e informações dos jogos
- Reprodução de trailers dentro da aplicação
- Área administrativa para gerenciamento de jogos

## 🛠️ Tecnologias utilizadas

### Backend
- **Java 8**
- **Java Servlets**
- **Maven**
- **DAO (Data Access Object)**
- **Model**
- **SQLite**
- **Gson**
- **BCrypt**
- **JavaMail**

### Frontend
- **HTML**
- **CSS**
- **JavaScript**

### Ambiente
- **Apache Tomcat 8.5**
- **Docker**

## 🏗️ Arquitetura

O projeto utiliza uma arquitetura baseada na separação de responsabilidades:

**Interface → Servlet → DAO → SQLite**

- **Interface:** apresenta as telas e recebe as ações do usuário.
- **Servlet:** recebe as requisições do navegador, processa a lógica necessária e comunica-se com os DAOs.
- **Model:** representa os objetos utilizados pelo sistema, como usuário e jogo.
- **DAO:** concentra as operações de acesso ao banco de dados, como consultas, inserções, alterações e exclusões.
- **SQLite:** armazena os dados da aplicação.

## 🗄️ Banco de dados

O projeto utiliza **SQLite**, escolhido por armazenar os dados em arquivo, sem a necessidade de manter um servidor de banco de dados separado.

Entre os dados trabalhados pelo sistema estão informações relacionadas a:

- Usuários
- Jogos
- Biblioteca
- Avaliações
- Favoritos
- Listas
- Seguidores

O relacionamento entre usuários e jogos é utilizado, por exemplo, na **biblioteca**, permitindo associar os jogos aos respectivos usuários.

## 🔐 Autenticação e segurança

O Inventory possui diferentes mecanismos de autenticação e segurança:

- Login tradicional com e-mail e senha
- Login com Google
- Senhas protegidas utilizando **BCrypt**
- Controle de sessão para manter o usuário autenticado
- Verificação/recuperação de conta por e-mail

## 📁 Estrutura principal

```
inventory/
└── inventory-main/
    ├── pom.xml
    ├── Dockerfile
    └── src/
        └── main/
            ├── java/
            │   ├── controller/
            │   ├── dao/
            │   ├── model/
            │   └── ...
            ├── resources/
            └── webapp/
                ├── HTML
                ├── CSS
                ├── JavaScript
                └── recursos da aplicação
```

## ▶️ Como executar

### Pré-requisitos

Instale:

- Java 8
- Maven
- Apache Tomcat 8.5

### Compilar

Entre na pasta do projeto:

```bash
cd inventory-main
```

Execute:

```bash
mvn clean package
```

O Maven irá gerar o arquivo:

```
target/Inventory.war
```

Depois, coloque o arquivo `Inventory.war` na pasta `webapps` do Tomcat e inicie o servidor.

## 🐳 Executando com Docker

O projeto possui um `Dockerfile` que:

1. Compila a aplicação com Maven.
2. Gera o arquivo WAR.
3. Utiliza Tomcat para executar a aplicação.
4. Disponibiliza a aplicação na porta **8080**.

Para criar a imagem:

```bash
docker build -t inventory .
```

Para executar:

```bash
docker run -p 8080:8080 inventory
```

Depois, acesse:

```
http://localhost:8080
```

## 🌐 Deploy

O projeto foi preparado para execução em ambientes que suportam **Docker e aplicações Java/Tomcat**.

O diretório de aplicação é:

```
inventory-main/
```

É importante utilizar esse diretório como raiz do serviço, pois nele estão o `pom.xml`, o `Dockerfile` e o código-fonte.

## 🎓 Sobre o projeto

O Inventory foi desenvolvido como projeto de **Trabalho de Conclusão de Curso (TCC)**, com o objetivo de aplicar conhecimentos de desenvolvimento web, banco de dados, arquitetura de software, autenticação e integração de serviços.

## 👩‍💻 Projeto

**Inventory — Plataforma para organização e avaliação de jogos**

Repositório: https://github.com/hellenapsn-pixel/inventory
