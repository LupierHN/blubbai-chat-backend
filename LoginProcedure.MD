# Ablaufplan: Anmeldung/Registrierung mit verpflichtendem 2FA

## Registrierung (`POST /api/v1/user/register`)
1. **User sendet Registrierungsdaten**
    - **201**: Registrierung erfolgreich, Rückgabe: `{ "2fa_required": true, "message": "2FA setup required" }`
    - **400**: Ungültige E-Mail/Telefonnummer (ERROR_1002, ERROR_1004)
    - **409**: Benutzername existiert bereits
    - **500**: Interner Fehler

## 2FA Setup (`GET /api/v1/user/2fa?method=2fa`)
1. **User fordert 2FA-Setup an**
    - **200**: QR-Code für TOTP wird zurückgegeben
    - **400/401/500**: wie oben

## 2FA Verifizierung (`POST /api/v1/user/2fa?code=XXXXXX`)
1. **User sendet 2FA-Code**
    - **200**: 2FA abgeschlossen, Rückgabe: Access- und Refresh-Token
    - **401**: Code falsch
    - **500**: User nicht gefunden

## Anmeldung (`POST /api/v1/user/login`)
1. **User sendet Login-Daten**
    - **200**: Login erfolgreich, wenn 2FA bereits abgeschlossen, Rückgabe: Access- und Refresh-Token
    - **403**: 2FA erforderlich, Rückgabe: `{ "2fa_required": true, "message": "2FA verification required" }`
    - **401**: Benutzer nicht gefunden/falsches Passwort
    - **500**: Interner Fehler

## Token-Validierung/Erneuerung wie gehabt