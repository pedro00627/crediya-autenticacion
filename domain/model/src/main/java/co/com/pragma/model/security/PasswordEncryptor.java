package co.com.pragma.model.security;

public interface PasswordEncryptor {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
