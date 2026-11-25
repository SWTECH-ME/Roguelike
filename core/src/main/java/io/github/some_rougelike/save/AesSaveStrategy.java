package io.github.some_rougelike.save;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import io.github.some_rougelike.GameConfig;

public class AesSaveStrategy implements SaveEncryptionStrategy {

    /** 
     * @return SecretKeySpec
     */
    private SecretKeySpec buildKey() {
        byte[] keyBytes = GameConfig.SAVE_ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] fixedKey = Arrays.copyOf(keyBytes, 16);
        return new SecretKeySpec(fixedKey, GameConfig.SAVE_ENCRYPTION_ALGORITHM);
    }

    /** 
     * @param plainText
     * @return String
     */
    @Override
    public String encode(String plainText) {
        if (plainText == null) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(GameConfig.SAVE_ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return plainText;
        }
    }

    /** 
     * @param storedText
     * @return String
     */
    @Override
    public String decode(String storedText) {
        if (storedText == null || storedText.trim().isEmpty()) {
            return "";
        }
        String trimmed = storedText.trim();
        if (trimmed.startsWith("{")) {
            return trimmed;
        }
        try {
            Cipher cipher = Cipher.getInstance(GameConfig.SAVE_ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, buildKey());
            byte[] decodedBytes = Base64.getDecoder().decode(trimmed);
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return trimmed;
        }
    }
}
