# UserController API Specification

## `GET /api/v1/user`
**Beschreibung:** Gibt das Profil des aktuell authentifizierten Nutzers zurück.

- **Header:**  
  - `Authorization`: JWT-Token

- **Antworten:**  
  - `200 OK`: User-Objekt  
  - `401 UNAUTHORIZED`: Kein oder ungültiger Token  
  - `500 INTERNAL_SERVER_ERROR`:  
    ```json
    { "code": 1005, "message": "User not found" }
    ```

---

## `POST /api/v1/user/register`
**Beschreibung:** Registriert einen neuen Nutzer.

- **Body:**  
  - User-Objekt (`username`, `email`, `phoneNumber`, `password`, ...)

- **Antworten:**  
  - `201 CREATED`: Liste von Token-Objekten  
  - `400 BAD_REQUEST`:  
    - Ungültige E-Mail:  
      ```json
      { "code": 1002, "message": "Invalid E-Mail" }
      ```
    - Ungültige Telefonnummer:  
      ```json
      { "code": 1004, "message": "Invalid Phone" }
      ```
  - `409 CONFLICT`: Nutzername existiert bereits  
  - `500 INTERNAL_SERVER_ERROR`: Allgemeiner Fehler

---

## `POST /api/v1/user/login`
**Beschreibung:** Authentifiziert einen Nutzer.

- **Body:**  
  - User-Objekt (`username`, `password`)

- **Antworten:**  
  - `200 OK`: Liste von Token-Objekten  
  - `401 UNAUTHORIZED`:  
    - Nutzer nicht gefunden:  
      ```json
      { "code": 4004, "message": "User not found" }
      ```
    - Falsches Passwort:  
      ```json
      { "code": 4002, "message": "Wrong Password" }
      ```
  - `500 INTERNAL_SERVER_ERROR`: Allgemeiner Fehler

---

## `PUT /api/v1/user/update`
**Beschreibung:** Aktualisiert das Nutzerprofil.

- **Header:**  
  - `Authorization`: JWT-Token

- **Body:**  
  - User-Objekt (aktualisierte Daten)

- **Query:**  
  - `oldPassword`: String

- **Antworten:**  
  - `200 OK`: Aktualisiertes User-Objekt  
  - `400 BAD_REQUEST`:  
    - Ungültige E-Mail:  
      ```json
      { "code": 1002, "message": "Invalid E-Mail" }
      ```
    - Ungültige Telefonnummer:  
      ```json
      { "code": 1004, "message": "Invalid Phone" }
      ```
  - `401 UNAUTHORIZED`:  
    - Falsches Passwort:  
      ```json
      { "code": 4002, "message": "Wrong Password" }
      ```
  - `500 INTERNAL_SERVER_ERROR`: Allgemeiner Fehler

---

## `DELETE /api/v1/user/delete`
**Beschreibung:** Löscht den aktuell authentifizierten Nutzer.

- **Header:**  
  - `Authorization`: JWT-Token

- **Antworten:**  
  - `204 NO CONTENT`: Erfolgreich gelöscht  
  - `404 NOT FOUND`: Nutzer nicht gefunden

---

## `GET /api/v1/user/2fa`
**Beschreibung:** Initialisiert oder verwaltet 2FA für den Nutzer.

- **Header:**  
  - `Authorization`: JWT-Token

- **Query:**  
  - `method`: (optional, z.B. `"2fa"`)

- **Antworten:**  
  - `200 OK`: QR-Code-String (bei TOTP) oder leer  
  - `401 UNAUTHORIZED`: Kein oder ungültiger Token  
  - `500 INTERNAL_SERVER_ERROR`:  
    ```json
    { "code": 1005, "message": "User not found" }
    ```

---

## `POST /api/v1/user/2fa`
**Beschreibung:** Überprüft den 2FA-Code.

- **Header:**  
  - `Authorization`: JWT-Token

- **Query:**  
  - `code`: 2FA-Code

- **Antworten:**  
  - `200 OK`: Erfolgreich  
  - `401 UNAUTHORIZED`:  
    - Kein 2FA aktiviert oder falscher Code  
  - `500 INTERNAL_SERVER_ERROR`:  
    ```json
    { "code": 1005, "message": "User not found" }
    ```

---

## `POST /api/v1/user/validateToken`
**Beschreibung:** Prüft, ob ein übergebener Token gültig ist.

- **Body:**  
  - Token-Objekt

- **Antworten:**  
  - `200 OK`: Boolean (`true` = gültig, `false` = ungültig)

---

## `POST /api/v1/user/renewToken`
**Beschreibung:** Erneuert einen Token, sofern gültig.

- **Header:**  
  - `Authorization`: JWT-Token

- **Body:**  
  - Token-Objekt

- **Antworten:**  
  - `200 OK`: Neuer Token  
  - `401 UNAUTHORIZED`: Token ungültig oder Nutzer nicht gefunden

---

**Hinweis:**  
Alle Fehlerantworten sind als JSON-Objekte mit `code` und `message` aufgebaut, sofern nicht anders angegeben.