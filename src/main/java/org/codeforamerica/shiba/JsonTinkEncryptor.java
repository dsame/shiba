package org.codeforamerica.shiba;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonTinkEncryptor implements Encryptor {
    private final ObjectMapper objectMapper;
    private final TinkEncryptor tinkEncryptor;

    public JsonTinkEncryptor(
            ObjectMapper objectMapper,
            TinkEncryptor tinkEncryptor) {
        this.objectMapper = objectMapper;
        this.tinkEncryptor = tinkEncryptor;
    }

    @Override
    public byte[] encrypt(ApplicationData data) {
        try {
            return tinkEncryptor.encrypt(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ApplicationData decrypt(byte[] encryptedData) {
        try {
            return objectMapper.readValue(tinkEncryptor.decrypt(encryptedData), ApplicationData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
