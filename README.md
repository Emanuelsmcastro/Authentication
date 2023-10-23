# Authentication

### Arquiteturas *Stateless* e *Stateful*

>Todas as aplicações possuem um estado, que é uma representação do seu funcionamento ou qualidade em um determinado momento. Para entender melhor como as arquiteturas Stateless e Stateful operam, podemos considerar o exemplo de um microserviço de autenticação.

>Na arquitetura Stateless, o servidor trata cada requisição de forma independente, sem manter qualquer informação sobre o estado do cliente entre as requisições. Nesse cenário, um token é gerado com base nos dados dos usuários/serviços cadastrados e é usado para autenticar as requisições subsequentes. Esse token é geralmente criado usando JWT (JSON Web Token) em base64.

>Por outro lado, na arquitetura Stateful, o servidor mantém informações sobre o estado do cliente entre as requisições. No contexto de um serviço de autenticação, isso envolve a geração de um token (comumente um UUID) que é retornado ao cliente. Quando o cliente faz uma requisição para outro microserviço, esse microserviço valida o token junto ao serviço de autenticação, que pode confirmar ou rejeitar a validade do token.

# Tecnologias utilizadas
- Docker
- Redis
- PostGreSQL
- Spring Boot : v3.1.5
- Spring Data Jpa
- Validation
- Lombok
- Jwt
- Spring Security Crypto
- webmvc-ui : Swagger
- WebFlux

# Dockerizing
>Nesse exemplo está a criação de uma imagem para a aplicação Any API que simula um microsserviço qualquer que interage com o microsserviço autenticador criado com base na arquitetura *Stateful*.

*Esse exemplo é a base para os demais Dockerfile's dos microsserviços.*
```dockerfile
FROM openjdk:21
COPY ./build/libs/stateful-any-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

A seguir está a estrutura do docker-compose.yml utilizada para criação do nosso container com todas as aplicações do projeto, configuração de variáveis de ambiente, de network e dependências.
```yml
version: '3'

services:

  stateless-auth-db:
    container_name: stateless-auth-db
    image: postgres:latest
    environment:
      POSTGRES_DB: auth-db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    networks:
      - auth
    ports:
      - "5432:5432"

  stateful-auth-db:
    container_name: stateful-auth-db
    image: postgres:latest
    environment:
      POSTGRES_DB: auth-db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    networks:
      - auth
    ports:
      - "5433:5432"

  token-redis:
    container_name: token-redis
    image: redis:latest
    networks:
      - auth
    ports:
      - "6379:6379"

  stateless-auth-api:
    build: "./stateless/stateless-auth-api"
    depends_on:
      - stateless-auth-db
    container_name: stateless-auth-api
    environment:
      PORT: 8080
      DB_HOST: stateless-auth-db
      DB_PORT: 5432
      DB_NAME: auth-db
      DB_USER: postgres
      DB_PASSWORD: postgres
      JWT_SECRET_KEY: YXV0aGVudGljYXRpb24tc3RhdGVsZXNzLXN0YXRlZnVsLW1pY3Jvc2VydmljZS1jb250YWluZXI=
    networks:
      - auth
    ports:
      - "8080:8080"

  stateless-any-api:
    build: "./stateless/stateless-any-api"
    container_name: stateless-any-api
    environment:
      PORT: 8081
      JWT_SECRET_KEY: YXV0aGVudGljYXRpb24tc3RhdGVsZXNzLXN0YXRlZnVsLW1pY3Jvc2VydmljZS1jb250YWluZXI=
    networks:
      - auth
    ports:
      - "8081:8081"

  stateful-auth-api:
    build: "./stateful/stateful-auth-api"
    depends_on:
      - stateful-auth-db
    container_name: stateful-auth-api
    environment:
      PORT: 8082
      DB_HOST: stateful-auth-db
      DB_PORT: 5432
      DB_NAME: auth-db
      DB_USER: postgres
      DB_PASSWORD: postgres
      REDIS_HOST: token-redis
      REDIS_PORT: 6379
    networks:
      - auth
    ports:
      - "8082:8082"

  stateful-any-api:
    build: "./stateful/stateful-any-api"
    container_name: stateful-any-api
    environment:
      PORT: 8083
      AUTH_BASE_URL: http://stateful-auth-api:8082
    networks:
      - auth
    ports:
      - "8083:8083"

networks:
  auth:
    driver: bridge
```

Nesse projeto também foi utilizado um builder em python para automatizar o gradle build dos nossos microsserviços e renovação do container

```python
import os
import threading

threads = []


def build_application(app):
    threads.append(app)
    print("Building application {}".format(app))
    os.system("cd {} && gradle build".format(app))
    print("Application {} finished building!".format(app))
    threads.remove(app)


def docker_compose_up():
    print("Running containers!")
    os.popen("docker-compose up --build -d").read()
    print("Pipeline finished!")


def build_all_applications():
    print("Starting to build applications!")
    threading.Thread(target=build_application,
                     args={"stateless/stateless-auth-api"}).start()
    threading.Thread(target=build_application,
                     args={"stateless/stateless-any-api"}).start()
    threading.Thread(target=build_application,
                     args={"stateful/stateful-auth-api"}).start()
    threading.Thread(target=build_application,
                     args={"stateful/stateful-any-api"}).start()


def remove_remaining_containers():
    print("Removing all containers.")
    os.system("docker-compose down")
    containers = os.popen('docker ps -aq').read().split('\n')
    containers.remove('')
    if len(containers) > 0:
        print("There are still {} containers created".format(containers))
        for container in containers:
            print("Stopping container {}".format(container))
            os.system("docker container stop {}".format(container))
        os.system("docker container prune -f")


if __name__ == "__main__":
    print("Pipeline started!")
    build_all_applications()
    while len(threads) > 0:
        pass
    remove_remaining_containers()
    threading.Thread(target=docker_compose_up).start()
```
> A função desse script python é acessar os diretórios dos microsserviços e aplicar o build para a criação do .jar utilizando o conceito de threads. Após isso ele derruba o container do projeto e aplica o docker-compose up para a renovação do mesmo, contendo as alterações/updates.

# Como utilizar

Na raiz do projeto digite:
```shell
python build.py
```
![image](https://github.com/Emanuelsmcastro/Authentication/assets/93106680/02caadc4-7ffd-4ee1-9ac9-724a224680cc)

![image](https://github.com/Emanuelsmcastro/Authentication/assets/93106680/88fd2fc6-8bc0-4113-85d5-b96ab36115ca)

![image](https://github.com/Emanuelsmcastro/Authentication/assets/93106680/54f03eff-b1c2-4090-98be-b8a1bcbb9621)

E para finalizar, acesse os microsserviços pelo seguinte endereço: localhost:{microservice_port}/swagger-ui/index.html
![image](https://github.com/Emanuelsmcastro/Authentication/assets/93106680/eaa16c60-2a16-4b42-b72d-1b113595ab03)

