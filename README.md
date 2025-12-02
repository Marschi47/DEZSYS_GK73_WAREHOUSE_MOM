# DEZSYS_GK73_WAREHOUSE_MOM

## 1. Durchführung
Die Umsetzung erfolgte mittels:
* **Spring Boot:** Framework für die Applikationslogik.
* **Apache Kafka (Docker):** Message Broker für den Nachrichtenaustausch.
* **JSON:** Datenformat für die Übertragung der Lagerbestände.

Es wurde ein Docker-Container für Kafka und Zookeeper aufgesetzt. Die Spring-Applikation fungiert sowohl als Producer (Simuliert Lagerstandorte) als auch als Consumer (Zentrale), getrennt durch verschiedene REST-Endpunkte und Kafka-Listener.

---

## 2. Beantwortung der Fragestellungen

### 2.1 Eigenschaften von Message Oriented Middleware (MOM)
Message Oriented Middleware (MOM) ermöglicht die Kommunikation zwischen verteilten Anwendungen durch das Senden und Empfangen von Nachrichten. Vier zentrale Eigenschaften sind:

1.  **Asynchronität:** Sender und Empfänger müssen nicht gleichzeitig aktiv sein. Der Sender kann weiterarbeiten, ohne auf die Antwort des Empfängers zu warten.
2.  **Entkopplung (Decoupling):** Die Systeme sind zeitlich und räumlich entkoppelt. Sie müssen keine direkten IP-Adressen voneinander kennen, sondern nur die Adresse des Brokers.
3.  **Persistenz (Reliability):** Nachrichten können (je nach Konfiguration) beim Broker zwischengespeichert werden ("Store and Forward"), sodass keine Daten verloren gehen, wenn der Empfänger kurzzeitig offline ist.
4.  **Skalierbarkeit:** Es können problemlos weitere Empfänger (Consumer) hinzugefügt werden, um die Last zu verteilen, ohne den Sender (Producer) anpassen zu müssen.

### 2.2 Transiente und synchrone Kommunikation
* **Transiente Kommunikation:** Die Nachricht wird nur übertragen, solange Sender und Empfänger *gleichzeitig* aktiv sind. Ist der Empfänger offline, geht die Nachricht verloren (ähnlich wie bei einem UDP-Paket oder einem flüchtigen Funksignal).
* **Synchrone Kommunikation:** Der Sender sendet eine Anfrage und **blockiert** (wartet), bis er eine Antwort vom Empfänger erhält. Ein klassisches Beispiel ist ein normaler HTTP-Request oder ein Telefonanruf.

### 2.3 Funktionsweise einer JMS Queue (Point-to-Point)
Eine Queue implementiert das **Point-to-Point (P2P)** Modell .
* Ein Producer sendet eine Nachricht in eine bestimmte Warteschlange (Queue).
* Auch wenn mehrere Consumer an der Queue lauschen, wird jede Nachricht garantiert nur von **genau einem** Consumer verarbeitet.
* Dies dient oft der Lastverteilung (Load Balancing).

### 2.4 JMS Overview - Wichtigste Klassen
Die Java Message Service (JMS) API definiert folgende Kernkomponenten:
* **ConnectionFactory:** Ein Objekt, das verwendet wird, um eine Verbindung zum Provider (Broker) herzustellen.
* **Connection:** Repräsentiert die aktive TCP/IP-Verbindung zum Message Broker.
* **Session:** Ein Thread-Kontext für das Senden und Empfangen von Nachrichten (hier finden oft Transaktionen statt).
* **Destination:** Das Ziel der Nachricht (entweder eine *Queue* oder ein *Topic*).
* **MessageProducer:** Das Objekt, das Nachrichten an eine Destination sendet.
* **MessageConsumer:** Das Objekt, das Nachrichten von einer Destination empfängt.

### 2.5 Funktionsweise eines JMS Topic (Publish-Subscribe)
Ein Topic implementiert das **Publish-Subscribe (Pub/Sub)** Modell .
* Ein Producer ("Publisher") sendet eine Nachricht an ein Thema (Topic).
* Im Gegensatz zur Queue wird die Nachricht an **alle** aktiven Consumer ("Subscriber") verteilt, die dieses Topic abonniert haben.
* Dies entspricht einer 1:N (Eins-zu-Viele) Kommunikation.

### 2.6 Lose gekoppeltes verteiltes System
* **Definition:** In einem lose gekoppelten System sind die Komponenten weitgehend unabhängig voneinander. Änderungen an einer Komponente führen nicht zwangsläufig zum Ausfall der anderen.
* **Warum "lose"?**
    * *Zeitlich:* Sender/Empfänger müssen nicht gleichzeitig laufen.
    * *Räumlich:* IP-Adressen sind abstrahiert (via Broker).
    * *Plattform:* Ein Java-Programm kann JSON senden, das von einem Python-Programm gelesen wird.
* **Beispiel:** Ein E-Mail-System. Der Absender schreibt die Mail, der Mailserver speichert sie. Der Empfänger ruft sie später ab. Beide PCs müssen nie gleichzeitig eingeschaltet sein.