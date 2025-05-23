package chat.dvai.backend.utils;

public class EnvProvider {

    public static String getEnv(String name) {
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable " + name + " not found");
        }
        return value;
    }

    public static String getEnvOrDefault(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }
}
