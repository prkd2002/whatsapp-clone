# WhatsApp Clone 🟢💬

Ein WhatsApp-ähnlicher Chat-Clone mit Echtzeitnachrichtenübertragung per WebSocket, moderner Angular-Oberfläche, Spring Boot Backend und vollständiger Containerisierung mit Docker.

## 🔧 Technologien

- **Frontend:** Angular
- **Backend:** Spring Boot (Java)
- **Echtzeit-Kommunikation:** WebSockets (STOMP)
- **Authentifizierung:** Keycloak
- **Containerisierung:** Docker, Docker Compose
- **Datenbank:** PostgreSQL / H2 (je nach Konfiguration)


---

## 🚀 Schnellstart mit Docker

### Voraussetzungen
- Docker & Docker Compose installiert

### Schritte

```bash
# 1. Repository klonen
git clone https://github.com/dein-benutzername/whatsapp-clone.git
cd whatsapp-clone
```

# 2. Alles mit Docker starten
'''bash
docker-compose up --build
'''

- Frontend läuft unter: http://localhost:4200

- Backend-API: http://localhost:8080

- WebSocket-Endpoint: ws://localhost:8080/ws



