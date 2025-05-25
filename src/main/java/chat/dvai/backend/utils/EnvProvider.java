package chat.dvai.backend.utils;

public class EnvProvider {

    /**
     * Retrieves the value of an environment variable.
     *
     * @param name the name of the environment variable
     * @return the value of the environment variable
     * @throws IllegalArgumentException if the environment variable is not found
     */
    public static String getEnv(String name) {
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable " + name + " not found");
        }
        return value;
    }

    /**
     * Retrieves the value of an environment variable or returns a default value if not found.
     *
     * @param name the name of the environment variable
     * @param defaultValue the default value to return if the environment variable is not found
     * @return the value of the environment variable or the default value
     */
    public static String getEnvOrDefault(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }
}
