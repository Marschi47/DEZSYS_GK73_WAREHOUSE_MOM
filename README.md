# DEZSYS_GK73_WAREHOUSE_MOM

**Name:** Florian Marschalek
**Datum:** 20.01.2026

## 1. Durchführung und Tests

### 1.1 Starten der Umgebung

Zuerst muss die Infrastruktur (Zookeeper und Kafka) mittels Docker gestartet werden:

```bash
docker-compose up -d

```

Anschließend werden die beiden Spring-Boot-Instanzen gestartet. Durch die Konfiguration in der `application.yml` und die Nutzung von Spring Profiles laufen diese auf unterschiedlichen Ports:

1. **Consumer (Zentrale)** starten: `ConsumerMain` (läuft auf Port **8081**).
2. **Producer (Lager)** starten: `ProducerMain` (läuft auf Port **8080**).

### 1.2 Szenario: Datenübertragung (Lager -> Zentrale)

Ein Lagerstandort sendet Bestandsdaten an die Message Queue. Im Gegensatz zur alten Version nutzt der Producer nun einen **GET-Request** mit Query-Parametern für eine einfachere Handhabung im Browser oder per Curl.

**Senden einer Nachricht:**

```bash
curl "http://localhost:8080/warehouse/send?id=101&loc=Wien-Hauptbahnhof&product=Laptop&amount=150"

```

**Erwartetes Verhalten:**

* **Producer-Response:** Der HTTP-Aufruf liefert direkt die Bestätigung zurück:
`Gesendet: 150x Laptop aus Wien-Hauptbahnhof`
* **Consumer-Konsole:** Die Zentrale empfängt das Event asynchron und gibt es auf der Konsole aus:
`Zentrale hat empfangen: Lager Wien-Hauptbahnhof: 150x Laptop`
* **Log-Datei:** Im Projektordner wird die Datei `warehouse_logging2.log` (für den Consumer) geschrieben.

### 1.3 Abruf der aggregierten Daten (Zentrale)

Die Zentrale (Port 8081) sammelt alle eingehenden Nachrichten in einer internen Liste. Diese können über den neuen Report-Endpunkt abgerufen werden. Der Controller gruppiert die Daten dabei automatisch nach Standort.

**Abruf des Reports (JSON):**

```bash
curl http://localhost:8081/central/report

```

*Output (Beispiel):*

```json
{
  "Wien-Hauptbahnhof": [
    {
      "id": 101,
      "location": "Wien-Hauptbahnhof",
      "product": "Laptop",
      "amount": 150
    }
  ]
}

```

---

## 2. Beantwortung der Fragestellungen

**1. Nennen Sie mindestens 4 Eigenschaften der Message Oriented Middleware?**

1. **Asynchronität:** Der Sender sendet die Nachricht ab und arbeitet sofort weiter, ohne auf die Antwort des Empfängers warten zu müssen.
2. **Lose Kopplung:** Sender und Empfänger sind voneinander entkoppelt (kennen sich nicht direkt, laufen auf unterschiedlichen Systemen/Plattformen).
3. **Persistenz (Zuverlässigkeit):** Nachrichten werden vom Broker zwischengespeichert ("Store and Forward"). Fällt der Empfänger aus, wird die Nachricht zugestellt, sobald er wieder online ist.
4. **Skalierbarkeit:** Es können einfach weitere Consumer hinzugefügt werden, um die Last zu verteilen (Load Balancing).

**2. Was versteht man unter einer transienten und synchronen Kommunikation?**

* **Synchron:** Der Sender blockiert und wartet ("handshake"), bis der Empfänger die Nachricht verarbeitet und geantwortet hat (z.B. klassischer HTTP Request oder Telefonanruf).
* **Transient:** Die Nachricht wird nicht dauerhaft gespeichert. Ist der Empfänger zum Zeitpunkt des Sendens nicht erreichbar oder fällt das Netzwerk aus, geht die Nachricht verloren (Gegenteil von persistenter Kommunikation).

**3. Beschreiben Sie die Funktionsweise einer JMS Queue?**
Eine Queue implementiert das **Point-to-Point (P2P)** Modell.

* Eine Nachricht wird von einem Sender in die Queue gelegt.
* Sie wird von genau **einem** Empfänger abgeholt und verarbeitet.
* Danach wird sie aus der Queue entfernt (Acknowledge).
* Dies eignet sich gut für Load Balancing (Verteilung von Aufgaben auf mehrere Worker).

**4. JMS Overview - Beschreiben Sie die wichtigsten JMS Klassen und deren Zusammenhang?**

* **ConnectionFactory:** Fabrik zum Erstellen einer Verbindung zum Message Broker.
* **Connection:** Eine aktive TCP/IP-Verbindung zum Provider.
* **Session:** Ein Thread-Kontext zum Senden und Empfangen von Nachrichten.
* **Destination:** Das Ziel der Nachricht (entweder Queue oder Topic).
* **MessageProducer:** Objekt zum Senden von Nachrichten an eine Destination.
* **MessageConsumer:** Objekt zum Empfangen von Nachrichten von einer Destination.

**5. Beschreiben Sie die Funktionsweise eines JMS Topic?**
Ein Topic implementiert das **Publish-Subscribe (Pub/Sub)** Modell.

* Eine Nachricht wird an ein Topic gesendet ("Publish").
* **Alle** Consumer, die dieses Topic abonniert haben ("Subscribe"), erhalten eine Kopie der Nachricht.
* Vergleichbar mit einem Radiosender (Einer sendet, viele hören zu).

**6. Was versteht man unter einem lose gekoppelten verteilten System? Nennen Sie ein Beispiel dazu. Warum spricht man hier von lose?**

* **Definition:** Ein System, bei dem die Komponenten so wenig wie möglich voneinander wissen müssen, um miteinander zu interagieren.
* **Beispiel:** Ein E-Mail-System. Ich sende eine Mail an den Server. Ich muss nicht wissen, welchen Computer mein Empfänger nutzt, welches Betriebssystem er hat oder ob er gerade online ist.
* **Warum "lose"?**
* *Zeitlich:* Sender und Empfänger müssen nicht gleichzeitig laufen.
* *Räumlich:* Die Komponenten kennen nur die Adresse der Middleware, nicht die IP des Partners.
* *Technologisch:* Ein Java-Programm kann Nachrichten an ein Python-Programm senden, solange das Datenformat (z.B. JSON) vereinbart ist.

---

## 3. Erklärung der wichtigsten Code-Komponenten

In diesem Abschnitt werden die zentralen Klassen und Mechanismen der Implementierung erläutert. Das System nutzt **Spring Profiles**, um Producer und Consumer zu trennen.

### 3.1 Konfiguration und Profile (`application.yml`)

Die Trennung der Rollen erfolgt über die `application.yml`. Hier wird definiert, dass das Programm auf unterschiedlichen Ports läuft, je nachdem, welches Profil beim Start gewählt wird.

```yaml
spring:
  kafka:
    bootstrap-servers: "localhost:9092"
---
spring:
  config:
    activate:
      on-profile: producer
server:
  port: 8080
---
spring:
  config:
    activate:
      on-profile: consumer
server:
  port: 8081

```

### 3.2 Der Producer: Senden von Nachrichten (`MessageProducer.java`)

Diese Klasse ist ein REST-Controller, der HTTP-GET-Anfragen entgegennimmt, daraus ein `WarehouseData`-Objekt erstellt und dieses als JSON an Kafka sendet.

```java
@GetMapping("/warehouse/send")
public String sendData(@RequestParam int id, @RequestParam String loc, 
                       @RequestParam String product, @RequestParam int amount) {
    // 1. Objekt erstellen
    WarehouseData data = new WarehouseData(id, loc, product, amount);

    // 2. Umwandeln in JSON
    String jsonMessage = objectMapper.writeValueAsString(data);

    // 3. Senden an Topic "quickstart-events"
    kafkaTemplate.send("quickstart-events", jsonMessage);

    return "Gesendet: " + amount + "x " + product + " aus " + loc;
}

```

* **Änderung:** Im Vergleich zur vorherigen Version werden nun auch Produktnamen (`product`) übertragen und die Parameter per URL (`@RequestParam`) übergeben.

### 3.3 Der Consumer: Empfang und Speicherung (`MessageConsumer.java`)

Der Consumer lauscht auf dem Topic `quickstart-events` und speichert die empfangenen Daten in einer lokalen Liste.

```java
@KafkaListener(topics = "quickstart-events", groupId = "central-group")
public void processMessage(String content) {
    try {
        // 1. Deserialisierung (JSON -> Java Objekt)
        WarehouseData data = objectMapper.readValue(content, WarehouseData.class);
        System.out.println("Zentrale hat empfangen: " + data);
        
        // 2. Speicherung in In-Memory Liste
        warehouseDatabase.add(data);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

```

* **warehouseDatabase:** Eine `ArrayList`, die als temporärer Datenspeicher dient.
* **getAllData():** Eine Hilfsmethode, die dem `ReportController` Zugriff auf die gesammelten Daten gewährt.

### 3.4 Datenbereitstellung und Gruppierung (`ReportController.java`)

Die Zentrale stellt die gesammelten Daten nun in strukturierter Form bereit. Anstatt einer flachen Liste wird eine Map zurückgegeben, die nach Lagerstandorten gruppiert ist.

```java
@GetMapping("/central/report")
public Map<String, List<WarehouseData>> getReport() {
    // 1. Daten vom Consumer holen
    List<WarehouseData> alleDaten = consumer.getAllData();

    // 2. Gruppierung nach Standort (Java Streams API)
    return alleDaten.stream()
            .collect(Collectors.groupingBy(WarehouseData::getLocation));
}

```

* **Funktionsweise:** Der Controller holt sich die Rohdaten vom `MessageConsumer` und nutzt Java Streams (`Collectors.groupingBy`), um die Einträge basierend auf dem Feld `location` zu sortieren. Das Ergebnis ist ein JSON-Objekt, bei dem der Key der Standortname ist.

```

```
