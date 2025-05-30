# UserController API Specification

This document describes the REST API endpoints for user management.  
All endpoints return JSON responses.  
Unless otherwise noted, all error responses are JSON objects with `code` and `message` fields.

---

## `GET /api/v1/user`
**Description:**  
Returns the profile of the currently authenticated user.

- **Headers:**  
  - `Authorization`: JWT token (required)

- **Filter Responses:**  
  - `401 UNAUTHORIZED`:  
    - If the JWT is missing, invalid, or the user does not exist:  
      ```json
      { "code": 401, "message": "Invalid or missing JWT token" }
      ```
      or  
      ```json
      { "code": 401, "message": "User not found" }
      ```
  - `403 FORBIDDEN`:  
    - If 2FA is required but not completed:  
      ```json
      { "2fa_required": true, "message": "2FA verification required" }
      ```
  - `400 BAD REQUEST`:  
    - If the 2FA method is not set:  
      ```json
      { "2fa_method": null, "message": "2FA Method not set" }
      ```

- **Controller Responses:**  
  - `200 OK`:  
    - Returns the user object if the token is valid and the user exists.
  - `404 NOT FOUND`:  
    - Returned if the user does not exist.
  - `500 INTERNAL_SERVER_ERROR`:  
    - Returned for unexpected server errors.

---

## `POST /api/v1/user/noa/register`
**Description:**  
Registers a new user.

- **Request Body:**  
  - User object (must include `username`, `email`, `phoneNumber`, `password`)

- **Responses:**  
  - `201 CREATED`:  
    - Returns a list of token objects if registration is successful.
  - `400 BAD REQUEST`:  
    - If the email is invalid:  
      ```json
      { "code": 1002, "message": "Invalid E-Mail" }
      ```
    - If the phone number is invalid:  
      ```json
      { "code": 1004, "message": "Invalid Phone" }
      ```
  - `409 CONFLICT`:  
    - If the username already exists.
  - `500 INTERNAL_SERVER_ERROR`:  
    - For any other server error.

---

## `POST /api/v1/user/noa/login`
**Description:**  
Authenticates a user.

- **Request Body:**  
  - User object (must include `username`, `password`)

- **Responses:**  
  - `200 OK`:  
    - Returns a list of token objects if login is successful.
  - `401 UNAUTHORIZED`:  
    - If the user is not found:  
      ```json
      { "code": 4004, "message": "User not found" }
      ```
    - If the password is incorrect:  
      ```json
      { "code": 4002, "message": "Wrong Password" }
      ```
  - `500 INTERNAL_SERVER_ERROR`:  
    - For any other server error.

---

## `PUT /api/v1/user/update`
**Description:**  
Updates the profile of the authenticated user.

- **Headers:**  
  - `Authorization`: JWT token (required)

- **Filter Responses:**  
  - `401 UNAUTHORIZED`:  
    - If the JWT is missing, invalid, or the user does not exist:  
      ```json
      { "code": 401, "message": "Invalid or missing JWT token" }
      ```
      or  
      ```json
      { "code": 401, "message": "User not found" }
      ```
  - `403 FORBIDDEN`:  
    - If 2FA is required but not completed:  
      ```json
      { "2fa_required": true, "message": "2FA verification required" }
      ```
  - `400 BAD REQUEST`:  
    - If the 2FA method is not set:  
      ```json
      { "2fa_method": null, "message": "2FA Method not set" }
      ```

- **Controller Responses:**  
  - `200 OK`:  
    - Returns the updated user object if successful.
  - `400 BAD REQUEST`:  
    - If the email is invalid:  
      ```json
      { "code": 1002, "message": "Invalid E-Mail" }
      ```
    - If the phone number is invalid:  
      ```json
      { "code": 1004, "message": "Invalid Phone" }
      ```
  - `401 UNAUTHORIZED`:  
    - If the old password is incorrect:  
      ```json
      { "code": 4002, "message": "Wrong Password" }
      ```
  - `500 INTERNAL_SERVER_ERROR`:  
    - For any other server error.

---

## `DELETE /api/v1/user/delete`
**Description:**  
Deletes the currently authenticated user.

- **Headers:**  
  - `Authorization`: JWT token (required)

- **Filter Responses:**  
  - `401 UNAUTHORIZED`:  
    - If the JWT is missing, invalid, or the user does not exist:  
      ```json
      { "code": 401, "message": "Invalid or missing JWT token" }
      ```
      or  
      ```json
      { "code": 401, "message": "User not found" }
      ```
  - `403 FORBIDDEN`:  
    - If 2FA is required but not completed:  
      ```json
      { "2fa_required": true, "message": "2FA verification required" }
      ```
  - `400 BAD REQUEST`:  
    - If the 2FA method is not set:  
      ```json
      { "2fa_method": null, "message": "2FA Method not set" }
      ```

- **Controller Responses:**  
  - `204 NO CONTENT`:  
    - If the user was successfully deleted.
  - `404 NOT FOUND`:  
    - If the user was not found.

---

## `GET /api/v1/user/no2fa/2fa`
**Description:**  
Initializes or manages two-factor authentication (2FA) for the authenticated user.

- **Headers:**  
  - `Authorization`: JWT token (required)

- **Filter Responses:**  
  - `401 UNAUTHORIZED`:  
    - If the JWT is missing, invalid, or the user does not exist:  
      ```json
      { "code": 401, "message": "Invalid or missing JWT token" }
      ```
      or  
      ```json
      { "code": 401, "message": "User not found" }
      ```

- **Controller Responses:**  
  - `200 OK`:  
    - Returns a QR code string for TOTP setup if method is `"2fa"` and user has no secret method yet.
    - Returns empty body if 2FA is already set up or another method is used.
  - `400 BAD REQUEST`:  
    - If the method is missing and the user has no secret method.
  - `404 NOT FOUND`:  
    - If the user does not exist.
  - `500 INTERNAL_SERVER_ERROR`:  
    - For any other server error.

---

## `POST /api/v1/user/no2fa/2fa`
**Description:**  
Verifies the submitted 2FA code for the authenticated user.

- **Headers:**  
  - `Authorization`: JWT token (required)

- **Filter Responses:**  
  - `401 UNAUTHORIZED`:  
    - If the JWT is missing, invalid, or the user does not exist:  
      ```json
      { "code": 401, "message": "Invalid or missing JWT token" }
      ```
      or  
      ```json
      { "code": 401, "message": "User not found" }
      ```

- **Controller Responses:**  
  - `200 OK`:  
    - If the code is valid, returns a new access token.
  - `401 UNAUTHORIZED`:  
    - If 2FA is not enabled or the code is invalid.
  - `404 NOT FOUND`:  
    - If the user does not exist.
  - `500 INTERNAL_SERVER_ERROR`:  
    - For any other server error.

---

## `POST /api/v1/user/noa/validateToken`
**Description:**  
Checks if a given token is valid.

- **Request Body:**  
  - Token object

- **Responses:**  
  - `200 OK`:  
    - Returns a boolean (`true` if valid, `false` if invalid).

---

## `POST /api/v1/user/noa/renewToken`
**Description:**  
Renews a token if it is valid.

- **Headers:**  
  - `Authorization`: JWT token (required)

- **Filter Responses:**  
  - `401 UNAUTHORIZED`:  
    - If the JWT is missing, invalid, or the user does not exist:  
      ```json
      { "code": 401, "message": "Invalid or missing JWT token" }
      ```
      or  
      ```json
      { "code": 401, "message": "User not found" }
      ```

- **Controller Responses:**  
  - `200 OK`:  
    - Returns a new token if renewal is successful.
  - `401 UNAUTHORIZED`:  
    - If the token is invalid or the user is not found.

---

**Notes:**  
- All error responses (unless otherwise specified) are JSON objects with `code` and `message`.
- All endpoints require valid JWT tokens in the `Authorization` header unless marked as "noa" (no authentication).
- For 2FA endpoints, if the user does not have a secret method set and no method is provided, a `400 BAD REQUEST` is returned.
- Filter responses (authentication/authorization errors) always take precedence and are returned before controller logic is executed.

