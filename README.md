# VIPLive Lab – Planning API (Kotlin + Spring Boot)

Deze repository bevat mijn uitwerking van de technische casus voor Topicus (VIPLive Lab).
De opdracht is een eenvoudige planning-API te bouwen die afspraken zonder overlap kan inplannen en het eerstvolgende vrije tijdslot kan bepalen.

De nadruk ligt op domeinlogica, duidelijke ontwerpkeuzes en een API die voorspelbaar en uitbreidbaar blijft.

---

## 1. Overzicht van de opdracht

De API ondersteunt:

- Het inplannen van een nieuwe afspraak.
- Het ophalen van het eerstvolgende tijdslot waarin een nieuwe afspraak zou passen.
- Het afdwingen dat afspraken niet overlappen.
- Het gestructureerd teruggeven van foutmeldingen.

Het domeinmodel is bewust klein en onafhankelijk gehouden.
Afspraken bestaan uit generieke velden zoals start, end, description en een UUID. Hierdoor blijft de kernlogica overzichtelijk en is de applicatie niet afhankelijk van opslag- of API-details.

Dit maakt het ontwerp geschikt om later uit te breiden, bijvoorbeeld met koppelingen naar externe agenda’s of ICS-export, zonder dat het huidige domein hoeft te worden aangepast.

---

## 2. Domeinmodel en ontwerpkeuzes

### Appointment
Een afspraak bestaat uit:

- `id` — UUID
- `start` — ZonedDateTime
- `end` — ZonedDateTime
- `description` — optioneel

**Waarom ZonedDateTime?**  
Omdat een agenda in de zorg (en in integraties met andere systemen) tijdzone-bewust moet zijn.

### AppointmentRepository
Een abstractie voor opslag, zodat later eenvoudig een database of externe agenda kan worden aangesloten.

### AppointmentService
Bevat de businessregels:

- start moet strikt vóór end liggen
- afspraken mogen niet overlappen
- berekening van eerstvolgende vrije slot
- optionele bovengrens (`searchUntil`) bij zoeken

Alle logica blijft hier, zodat de persistence-laag eenvoudig vervangbaar is.

---

## 3. Implementatie

### InMemoryAppointmentRepository

Kenmerken:

- Thread-safe lijst met `synchronizedList`
- Invoegen op correcte positie zodat de lijst altijd gesorteerd blijft
- Methoden:
    - `save(appointment)`
    - `findOverlapping(start, end)`
    - `findBetween(start, end?)`

### AppointmentServiceImpl

Verantwoordelijkheden:

- Validate tijdsbereik
- Overlapping vermijden
- UUID genereren
- Vrij tijdslot bepalen: de berekening van het vrije tijdslot is gebaseerd op een efficiënte lineaire scan over de gesorteerde afspraken, waarbij de gaten tussen de afspraken worden gecontroleerd op de gevraagde duration.

---

## 4. API Endpoints

### POST `/api/appointments`

Maakt een nieuwe afspraak.

**Request body**
```json
{
  "start": "2025-01-10T09:00:00+01:00[Europe/Amsterdam]",
  "end": "2025-01-10T09:30:00+01:00[Europe/Amsterdam]",
  "description": "Intake gesprek"
}
```

**Responses**

| Status | Betekenis |
|--------|-----------|
| **200 OK** | Afspraak gemaakt |
| **400 Bad Request** | Ongeldig tijdsbereik |
| **409 Conflict** | Overlap met bestaande afspraak |

---

### GET `/api/appointments/free-slot`

Berekent het eerstvolgende beschikbare tijdslot.

**Query parameters**

| Parameter | Type | Verplicht | Beschrijving |
|-----------|------|-----------|--------------|
| `from` | ZonedDateTime | ja | Startmoment van zoeken |
| `durationMinutes` | int | ja | Gewenste duur in minuten |
| `searchUntil` | ZonedDateTime | nee | Optionele bovengrens |

**Voorbeeld**
```
/api/appointments/free-slot?from=2025-01-10T08:00:00+01:00[Europe/Amsterdam]&durationMinutes=30
```

**Responses**

| Status | Betekenis |
|--------|-----------|
| **200 OK** | Starttijd van het vrije slot |
| **404 Not Found** | Geen passend tijdslot gevonden |

---

## 5. Foutafhandeling

Een globale exception handler vertaalt domeinexceptions naar HTTP-responses.

| Exception | HTTP status | Code |
|-----------|-------------|------|
| `InvalidAppointmentTimeException` | 400 | `INVALID_APPOINTMENT_TIME` |
| `AppointmentConflictException` | 409 | `APPOINTMENT_CONFLICT` |
| `NoFreeSlotAvailableException` | 404 | `NO_FREE_SLOT` |
| Onverwachte fout | 500 | `INTERNAL_ERROR` |

**Error response voorbeeld**
```json
{
  "code": "APPOINTMENT_CONFLICT",
  "message": "Appointment overlaps with an existing one."
}
```

---

## 6. Swagger / OpenAPI

Swagger UI:
```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:
```
http://localhost:8080/v3/api-docs
```

DTO’s en controllers zijn voorzien van annotaties voor duidelijke documentatie.

---

## 7. Running the project

### Vereisten
- JDK 21
- Maven 3.8+
- Kotlin (via Maven plugin)

### Applicatie starten
```bash
mvn spring-boot:run
```

Backend draait daarna op:
```
http://localhost:8080
```

---

## 8. Beperkingen en uitbreidingsmogelijkheden

### Huidige beperkingen
- In-memory opslag verdwijnt na restart.
- Eén enkele agenda (geen multi-user scenario).
- Vrij slot-berekening is lineair.
- Geen authenticatie.
- Niet geschikt voor echte concurrentiebelasting.

### Mogelijke uitbreidingen
- Persistente opslag (PostgreSQL, Redis, EventStore).
- ICS-export of synchronisatie met externe agenda’s.
- Event Sourcing voor auditing.
- Policies zoals verplichte pauzes tussen afspraken.
- Validatie om afspraken in het verleden te voorkomen.

