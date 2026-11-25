package io.github.some_rougelike.save;

public class PlainSaveStrategy implements SaveEncryptionStrategy {

    private final AesSaveStrategy fallbackDecryptor = new AesSaveStrategy();

    /** 
     * @param plainText
     * @return String
     */
    @Override
    public String encode(String plainText) {
        return plainText == null ? "" : plainText;
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
        if (!trimmed.startsWith("{")) {
            return fallbackDecryptor.decode(trimmed);
        }
        return trimmed;
    }
}
