package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface Encryptor {
    byte[] encrypt(ApplicationData data);

    ApplicationData decrypt(byte[] encryptedData);
}
