# Sistema de Controle de Frota de Caminhões

Este é um sistema de controle de frota de caminhões e entregas desenvolvido com Spring Boot.

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2.3
- PostgreSQL
- Docker
- Swagger/OpenAPI
- JUnit para testes unitários
- Cypress para testes E2E

## Pré-requisitos

- Java 17
- Docker e Docker Compose
- Maven

## Como Executar

1. Clone o repositório:
```bash
git clone [URL_DO_REPOSITÓRIO]
cd desafio-caca
```

2. Execute o projeto com Docker Compose:
```bash
docker-compose up --build
```

3. Acesse a aplicação:
- API: http://localhost:8080
- Documentação Swagger: http://localhost:8080/swagger-ui.html

## Estrutura do Projeto

O projeto segue uma arquitetura em camadas:

- `controllers/`: Endpoints da API REST
- `services/`: Lógica de negócios
- `repositories/`: Acesso a dados
- `models/`: Entidades e DTOs
- `config/`: Configurações do Spring
- `exceptions/`: Tratamento de exceções

## Regras de Negócio Implementadas

1. Um caminhão só pode estar associado a uma entrega
2. Entregas com valores > R$ 30.000 recebem indicador de valiosa
3. Entregas de eletrônicos têm indicador de seguro
4. Entregas de combustível têm indicador de perigosa
5. Limite de 4 entregas por caminhão/mês
6. Limite de 2 entregas por motorista/mês
7. Taxa de 20% para entregas no Nordeste
8. Taxa de 40% para entregas na Argentina
9. Taxa de 30% para entregas na Amazônia
10. Limite de 1 entrega para o Nordeste por motorista

## Testes

### Testes Unitários
```bash
./mvnw test
```

### Testes E2E (Cypress)
```bash
cd cypress
npm install
npm run cypress:open
```

## Documentação da API

A documentação completa da API está disponível através do Swagger UI em:
http://localhost:8080/swagger-ui.html 