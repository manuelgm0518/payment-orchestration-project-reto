# Payment Orchestration Project

Proyecto de orquestación de pagos utilizando Temporal.io, Spring Boot y una arquitectura de microservicios.

## Requisitos

- Java 17
- Maven
- Docker y Docker Compose

## Ejecución local

1. Clonar el repositorio y posicionarse en la raíz:

   ```bash
   git clone git@github.com:manuelgm0518/payment-orchestration-project-reto.git
   cd payment-orchestration-project-reto
   ```

2. Iniciar la infraestructura base (Temporal, Temporal UI, MongoDB, Postgres, Mongo Express):

   ```bash
   docker-compose up
   ```

3. Compilar los microservicios Java:

   ```bash
   mvn clean package -DskipTests
   ```

4. Iniciar los servicios Java en terminales independientes:

   - **Eureka Server** (puerto 8761):
     ```bash
     cd eureka-server
     mvn spring-boot:run
     # o java -jar target/eureka-server-*.jar
     ```
   - **ms-validator-charger** (puerto 8082):
     ```bash
     cd ms-validator-charger
     mvn spring-boot:run
     # o java -jar target/ms-validator-charger-*.jar
     ```
   - **ms-persistence** (puerto 8083):
     ```bash
     cd ms-persistence
     mvn spring-boot:run
     # o java -jar target/ms-persistence-*.jar
     ```
   - **temporal-orchestrator** (puerto 8080):
     ```bash
     cd temporal-orchestrator
     mvn spring-boot:run
     # o java -jar target/temporal-orchestrator-*.jar
     ```

## Prueba del orquestador

Para iniciar un proceso de pago, realizar la siguiente petición:

```bash
curl -X POST http://localhost:8080/api/v1/orchestrator/process-payment \
  -H "Content-Type: application/json" \
  -d '{"clientNumber": "1234567890", "chargeAmount": 100.0}'
```

Respuesta esperada:

```json
{
  "processId": "<uuid>",
  "status": "STARTED"
}
```

## Recursos útiles

- Consola Eureka: http://localhost:8761
- Temporal UI: http://localhost:8233
- Mongo Express: http://localhost:8081

Se recomienda verificar que todos los servicios estén registrados en Eureka antes de realizar pruebas.


## Ejecución
<img width="517" alt="Screenshot 2025-06-27 at 1 51 50 p m" src="https://github.com/user-attachments/assets/da00a6e5-81c9-436f-b4df-896921fd2e32" />

<img width="1782" alt="Screenshot 2025-06-27 at 1 51 27 p m" src="https://github.com/user-attachments/assets/5b79d504-2ab0-46d9-8c99-a28926c0870c" />

<img width="626" alt="Screenshot 2025-06-27 at 1 52 54 p m" src="https://github.com/user-attachments/assets/68eb313c-03bb-415c-a968-6e2ebbf2ce62" />



