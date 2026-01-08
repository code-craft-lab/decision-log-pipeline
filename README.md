# OPA Decision Log Gateway - PoC

Proof of Concept aplikacji do odbierania batchy decyzji z Open Policy Agent (OPA) przez HTTP i publikowania ich do Apache Kafka.

## 📋 Wymagania

- **Java 21**
- **Docker** i **Docker Compose**
- **Gradle 8.8** (dostarczone przez wrapper)

## 🏗️ Architektura

```
HTTP POST /decision-logs
         ↓
   DecisionLogController (WebFlux)
         ↓
   DecisionLogPublisher (Service)
         ↓
    Kafka Producer
         ↓
   Topic: decision-logs
```

## 🚀 Uruchomienie

### 1. Uruchom Kafka lokalnie

```bash
docker-compose up -d
```

Sprawdź status:
```bash
docker-compose ps
```

### 2. Zbuduj projekt

```bash
./gradlew clean build
```

### 3. Uruchom aplikację

```bash
./gradlew bootRun
```

Aplikacja będzie dostępna pod adresem: `http://localhost:8080`

## 📡 Testowanie API

### Wyślij batch decyzji

```bash
curl -X POST http://localhost:8080/decision-logs \
  -H "Content-Type: application/json" \
  -d '[
    {"decision": "allow", "user": "alice", "resource": "/api/data"},
    {"decision": "deny", "user": "bob", "resource": "/api/admin"}
  ]'
```

**Oczekiwana odpowiedź:**
- HTTP `201 Created` – sukces
- HTTP `500 Internal Server Error` – błąd Kafka

### Sprawdź wiadomości w Kafka

```bash
docker exec -it opa-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic decision-logs \
  --from-beginning
```

Powinieneś zobaczyć:
```json
[{"decision": "allow", "user": "alice", "resource": "/api/data"},{"decision": "deny", "user": "bob", "resource": "/api/admin"}]
```

### Alternatywnie – lista topics

```bash
docker exec -it opa-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list
```

## 🧪 Testy

### Uruchom wszystkie testy

```bash
./gradlew test
```

### Testy jednostkowe (z MockK)

```bash
./gradlew test --tests "ai.lab.opa.decisionlog.gateway.service.DecisionLogPublisherTest"
```

### Testy integracyjne (z EmbeddedKafka)

```bash
./gradlew test --tests "ai.lab.opa.decisionlog.gateway.controller.DecisionLogControllerTest"
```

## 📂 Struktura Projektu

```
src/
├── main/
│   ├── kotlin/ai/lab/opa/decisionlog/gateway/
│   │   ├── DecisionLogGatewayApplication.kt     # Main class
│   │   ├── config/
│   │   │   └── KafkaProducerConfig.kt           # Kafka config
│   │   ├── controller/
│   │   │   └── DecisionLogController.kt         # REST endpoint
│   │   ├── service/
│   │   │   └── DecisionLogPublisher.kt          # Kafka publisher
│   │   └── model/
│   │       └── PublishResult.kt                 # Result sealed class
│   └── resources/
│       ├── application.yaml                     # Base config
│       └── application-local.yaml               # Local profile
└── test/
    └── kotlin/ai/lab/opa/decisionlog/gateway/
        ├── controller/
        │   └── DecisionLogControllerTest.kt     # Integration test
        └── service/
            └── DecisionLogPublisherTest.kt      # Unit test
```

## ⚙️ Konfiguracja

### application-local.yaml

```yaml
kafka:
  bootstrap-servers: localhost:9092
  topic:
    decision-logs: decision-logs
  producer:
    acks: all
    retries: 3

server:
  port: 8080
```

## 🛑 Zatrzymanie

### Zatrzymaj aplikację
`Ctrl+C` w terminalu z `bootRun`

### Zatrzymaj Kafka
```bash
docker-compose down
```

### Usuń volumes (opcjonalnie)
```bash
docker-compose down -v
```

## 🔍 Debugging

### Sprawdź logi aplikacji
```bash
./gradlew bootRun --info
```

### Sprawdź logi Kafka
```bash
docker logs opa-kafka -f
```

### Sprawdź status brokera
```bash
docker exec -it opa-kafka kafka-broker-api-versions \
  --bootstrap-server localhost:9092
```

## 📊 Metryki Kafka

```bash
docker exec -it opa-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list
```

## 🐛 Troubleshooting

### Port 9092 already in use
```bash
lsof -i :9092
# lub
sudo netstat -tulpn | grep 9092
```

### Kafka nie startuje
```bash
docker-compose logs kafka
```

### Testy nie przechodzą
```bash
./gradlew clean test --info
```

## 📚 Technologie

- **Kotlin 2.0.20**
- **Java 21**
- **Spring Boot 3.5.8**
- **Spring WebFlux** (reactive)
- **Spring for Apache Kafka**
- **Gradle 8.8**
- **Jackson** (JSON)
- **MockK** (testy)
- **EmbeddedKafka** (testy integracyjne)

## 🎯 Zakres PoC (MUST HAVE)

✅ Endpoint HTTP POST `/decision-logs` przyjmujący JSON array  
✅ Publikacja do Kafka (topic: `decision-logs`)  
✅ Obsługa błędów (500 przy błędzie Kafka)  
✅ Profil `local` z konfiguracją  
✅ Testy jednostkowe (MockK)  
✅ Testy integracyjne (EmbeddedKafka)  
✅ Docker Compose z Kafka + Zookeeper  
✅ Instrukcja uruchomienia  

## 📝 Notatki

- **Klucz wiadomości Kafka:** `opa-decision-batch` (statyczny)
- **Brak walidacji payload:** payload traktowany jako surowy JSON string
- **Brak autentykacji, DLQ, metryk** – to tylko PoC
- **Profile:** tylko `local` (domyślny)

## 🚧 Kolejne kroki (poza PoC)

- [ ] Walidacja szczegółowa payload (schemat OPA)
- [ ] Chunking dla dużych batchy
- [ ] Dead Letter Queue (DLQ)
- [ ] Metryki Prometheus/Micrometer
- [ ] Autentykacja (OAuth2/JWT)
- [ ] Integracja z S3/OpenSearch
- [ ] Testy end-to-end
- [ ] CI/CD pipeline

## 📄 Licencja

MIT

## 👤 Autor

AI Lab - Kafka Codegen Team
