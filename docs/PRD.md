# Dokument wymagań produktu (PRD) - OPA Decision Log Pipeline

##
1. Przegląd produktu
OPA Decision Log Pipeline to serwis przyjmujący batche decyzji z Open Policy Agent (OPA), publikujący je do Apache Kafka, a następnie trwałe zapisujący w dwóch niezależnych systemach: długoterminowym magazynie (Amazon S3) oraz magazynie czasu rzeczywistego (OpenSearch). Celem jest zapewnienie niezawodnej, skalowalnej i bezpiecznej obsługi do 200 mln decyzji miesięcznie przy minimalnych opóźnieniach, gwarantując dostęp operacyjny (≤ 60 s) oraz archiwizację (20 lat).

## 2. Problem użytkownika
Organizacja wykorzystująca OPA w setkach mikrousług potrzebuje:
1. Niezawodnego gromadzenia wszystkich decyzji autoryzacyjnych dla audytu i zgodności.
2. Błyskawicznego wyszukiwania ostatnich decyzji (≤ 3 dni) na potrzeby operacyjne i bezpieczeństwa.
3. Prostej procedury przywracania utraconych logów i obsługi błędów zapisu.

## 3. Wymagania funkcjonalne
FR-01 Serwis HTTP odbiera batche decyzji OPA przez POST /decision-logs z uwierzytelnieniem kluczem API.
FR-02 Serwis akceptuje ≥ 50 000 logów/min i potwierdza przyjęcie dopiero po opublikowaniu batcha do Apache Kafka.
FR-03 Batch o rozmiarze > 1 MB jest dzielony (chunking) przed publikacją do Kafka.
FR-04 Producent publikuje surowy JSON array do topic Kafka; partycjonowanie kluczem dowolnym.
FR-05 Konsument S3 odczytuje komunikaty z Kafka i zapisuje je w S3 jako pliki gzip wg ścieżki `s3://{bucket}/{env}/project/`.
FR-06 Konsument OpenSearch odczytuje komunikaty z Kafka i indeksuje każdą decyzję; dostępność danych ≤ 60 s od wysłania przez OPA.
FR-07 Dane w S3 muszą być przechowywane przez 20 lat bez wersjonowania prefiksów.
FR-08 Dane w OpenSearch usuwane automatycznie po 3 dniach.
FR-09 W przypadku błędu zapisu do S3 lub OpenSearch komunikat trafia do dedykowanej kolejki DLQ.
FR-10 System publikuje metryki sukcesu zapisów, opóźnień i rozmiaru DLQ do platformy obserwowalności.
FR-11 Uwierzytelnienie oparte o wiele kluczy API przechowywanych w Kubernetes Secrets; każdy klucz konfigurowany z nazwą i datą ważności.
FR-12 Konsola CLI (lub endpoint) pozwala operatorowi ponownie przetwarzać komunikaty z DLQ według zakresu czasowego lub ID.

## 4. Granice produktu
Ograniczenia MVP (out-of-scope):
• Wyszukiwanie i zaawansowana analiza danych w S3.
• Back-fill historycznych logów.
• Maskowanie lub anonimizacja PII.
• Rozbudowane metryki typu tracing i histogramy latencji (zapewnia platforma).
• Interfejs graficzny self-service.
• Obsługa żądań usunięcia danych (GDPR delete-requests).
• Provisioning klastra OpenSearch oraz infrastruktury.

Nierozwiązane kwestie do przyszłej decyzji:
1. Parametry rollover plików gzip (czas vs. rozmiar).
2. Szczegółowy projekt i rotacja wielu kluczy API.
3. Formalny playbook obsługi DLQ (SLO reakcja, odpowiedzialności).

## 5. Historyjki użytkowników

ID: US-001
Tytuł: Przyjęcie batcha decyzji
Opis: Jako OPA chcę wysłać batch decyzji, aby zostały niezawodnie zapisane.
Kryteria akceptacji:
- Serwis HTTP przyjmuje POST z JSON array.
- Odpowiedź HTTP 201 zwracana po sukcesie publikacji do Kafka.
- Maks. rozmiar pojedynczego batcha przyjęty: dowolny, ale po podziale do ≤ 1 MB komunikaty trafiają do Kafka.

ID: US-002
Tytuł: Uwierzytelnienie kluczem API
Opis: Jako administrator chcę zarządzać wieloma kluczami API, aby bezpiecznie uwierzytelnić różne instancje OPA.
Kryteria akceptacji:
- Każde żądanie z nieprawidłowym kluczem zwraca 401.
- Więcej niż 1 aktywny klucz jednocześnie.

ID: US-003
Tytuł: Zapisywanie do S3 
Opis: Jako system archiwizacji chcę, aby decyzje były zapisywane w S3 na 20 lat, aby zapewnić zgodność audytową.
Kryteria akceptacji:
- Konsument zapisuje pliki gzip wg ustalonej ścieżki.
- W razie błędu komunikat trafia na DLQ.
- Współczynnik sukcesu zapisu ≥ 99,99 %.

ID: US-004
Tytuł: Indeksowanie do OpenSearch
Opis: Jako operator bezpieczeństwa chcę wyszukiwać decyzje w OpenSearch w ciągu 1 min, aby badać incydenty.
Kryteria akceptacji:
- Decyzje dostępne w OpenSearch ≤ 60 s od wysłania przez OPA.
- Indeksy automatycznie usuwane po 3 dniach.
- Współczynnik sukcesu indeksacji ≥ 99,99 %.

ID: US-005
Tytuł: Obsługa dużego ruchu 
Opis: Jako SRE chcę obsłużyć szczyt 50 000 logów/min bez utraty, aby zapewnić niezawodność.
Kryteria akceptacji:
- Test obciążeniowy potwierdza brak utraconych batchy przy 50 000 logów/min.

ID: US-006
Tytuł: Przekierowanie do DLQ 
Opis: Jako SRE chcę, aby błędne komunikaty trafiały do DLQ, abym mógł je ręcznie odzyskać. 
Kryteria akceptacji:
- Każdy błąd zapisu skutkuje komunikatem w DLQ z meta-danymi przyczyny.
- Komunikat nigdy nie znika z DLQ bez akcji operatora.

ID: US-007
Tytuł: Re-proces DLQ 
Opis: Jako SRE chcę ponownie przetworzyć komunikaty z DLQ, aby odzyskać utracone logi. 
Kryteria akceptacji:
- CLI/endpoint pozwala wskazać zakres czasowy lub ID.
- Po re-procesie komunikat przenoszony z DLQ do oryginalnego tematu.
- Operacja jest idempotentna.

ID: US-008
Tytuł: Monitoring metryk 
Opis: Jako lider operacji chcę widzieć metryki sukcesu i opóźnień, aby kontrolować SLA. 
Kryteria akceptacji:
- Metryki write-success, latency, DLQ size publikowane w Prometheus.
- Alert ≥ 10 komunikatów w DLQ > 24 h.

ID: US-009
Tytuł: Chunking dużych batchy 
Opis: Jako OPA wysyłający bardzo duże batchy chcę, aby producent sam podzielił je, aby zmieścić się w limicie Kafka. 
Kryteria akceptacji:
- Batch > 1 MB dzielony na N części ≤ 1 MB.
- Części publikowane z tym samym nagłówkiem batch-id.

## 6. Metryki sukcesu
• ≥ 99,99 % decyzji zapisanych w S3 (rolling 30 dni, metryka write-success).
• ≥ 99,99 % decyzji z ostatnich 3 dni dostępnych w OpenSearch w ≤ 60 s (metryka age < 60 s).
• 0 batchy utraconych podczas szczytu 50 000 logów/min w testach wydajnościowych.
• DLQ = 0 komunikatów starszych niż 24 h.
• Średni czas re-procesu komunikatu z DLQ do sukcesu < 10 min.
