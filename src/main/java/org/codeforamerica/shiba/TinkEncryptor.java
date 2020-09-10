package org.codeforamerica.shiba;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.config.TinkConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class TinkEncryptor {
    private final Aead aead;

    public TinkEncryptor(@Value("${encryption-key}") String encryptionKey) throws GeneralSecurityException, IOException {
        TinkConfig.register();
        aead = CleartextKeysetHandle.read(
                JsonKeysetReader.withString(encryptionKey)).getPrimitive(Aead.class);
    }


    public byte[] encrypt(String data) {
        try {
            return aead.encrypt(data.getBytes(), null);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(byte[] encryptedData) {
        try {
            return new String(aead.decrypt(encryptedData, null));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
