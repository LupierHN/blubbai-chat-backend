package chat.blubbai.backend.model.enums;

import lombok.Getter;

@Getter
public enum Method2FA {
    EMAIL("Email"),
    SMS("Sms"),
    AUTHENTICATOR("Authenticator");

    private final String method;

    Method2FA(String method) {
        this.method = method;
    }

}
