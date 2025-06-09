package chat.blubbai.backend.model.enums;

import lombok.Getter;

@Getter
public enum Sender {
    USER("User"),
    AI("AI");

    private final String value;

    Sender(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
