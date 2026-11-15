package io.github.some_rougelike.save;

public interface SaveEncryptionStrategy {
    String encode(String plainText);
    String decode(String storedText);
}
