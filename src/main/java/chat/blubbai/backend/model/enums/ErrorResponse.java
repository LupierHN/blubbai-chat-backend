package chat.blubbai.backend.model.enums;

import lombok.Getter;

/**
 * Enum representing various error responses with their corresponding messages.
 * Each enum constant corresponds to a specific error scenario.<p>
 * Error codes are prefixed with a number indicating the category <p>
 * (e.g., 1xxx for user-related errors, 4xxx for authentication errors).
 */
@Getter
public enum ErrorResponse {
    USER_NOT_FOUND(1001,"User not found."),
    BAD_EMAIL(1002,"Invalid E-mail address."),
    USERNAME_CONFLICT(1003,"User already exists."),
    BAD_PHONE(1004,"Invalid phone number."),
    BAD_USERNAME(1005,"Invalid username."),
    METHOD_NOT_SET(4001,"2FA Method not set."),
    INVALID_PASSWORD(4002,"Invalid password."),
    INVALID_2FA(4003,"2FA Code wrong or expired."),
    TOKEN_EXPIRED(4004,"Token expired."),
    TWO_FACTOR_REQUIRED(4005,"Two-factor authentication required.");

    private final String message;
    private final int value;

    ErrorResponse(int value, String message) {
        this.message = message;
        this.value = value;
    }
}
