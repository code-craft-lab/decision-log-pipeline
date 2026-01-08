## Główny problem
OPA (Open Policy Agent) działa jako sidecar przy setkach mikro-serwisów w Kubernetes. Każda autoryzacja generuje decision log. Problemem jest obsługa decyzji OPA i utrwalenie ich w dwóch niezależnych miejscach. Pierwsze to long term storage. Drugi to real time access dla decyzji OPA.

## Najmniejszy zestaw funkcjonalności
- Obsługa odbioru decyzji z OPA za pośrednictwem protokołu HTTP
- Zapis decyzji OPA do Kafka
- Odczyt decyzji OPA z Kafka i zapisanie do long term storage S3
- Problem zapisu do S3 przenosi wiadomość na DLQ
- Odczyt decyzji OPA z Kafka i zapisanie do teal time storage OpenSearch
- Problem zapisu do OpenSearch przenosi wiadomość na DLQ

## Co NIE wchodzi w zakres MVP (Out-of-Scope)
- Wyszukiwanie i analiza w S3
- Back-fill historycznych logów
- PII masking/redaction
- Rozbudowana obserwowalność (tracing, latency histograms)
- Self-service UI
- GDPR delete-requests

## Kryteria sukcesu
- 99,99 % decyzji OPA zostaje utrwaonych w S3
- 99,99 % decyzji OPA z ostatnich 3 dni można wyszukać w OpenSearch
