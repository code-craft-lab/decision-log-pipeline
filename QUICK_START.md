# Quick Start Guide

## 🚀 Szybki Start (3 kroki)

### 1. Uruchom Kafka
```bash
docker compose up -d
```

### 2. Uruchom aplikację
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3. Wyślij testowy request
```bash
curl -v -X POST http://localhost:8080/decision-logs \
  -H "Content-Type: application/json" \
  -d '[{"decision": "allow", "user": "alice"}]'
```

**Oczekiwana odpowiedź:** HTTP 201

---

## 🔍 Weryfikacja w Kafka

```bash
docker exec -it opa-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic decision-logs \
  --from-beginning
```

Powinieneś zobaczyć:
```json
[{"decision": "allow", "user": "alice"}]
```

---

## 🧪 Uruchom testy

```bash
./gradlew test
```

---

## 🛑 Zatrzymanie

```bash
# Zatrzymaj aplikację: Ctrl+C

# Zatrzymaj Kafka:
docker compose down
```

---

## 📖 Pełna dokumentacja

Zobacz [README.md](./README.md) dla szczegółowych informacji.

