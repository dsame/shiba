# SHIBA - Securely Hosted Integrated Benefits Application

## Development setup

### Install the following system dependencies:

- Java 14 Development Kit: `brew cask install java`
- Gradle build tool version 6.3: `brew install gradle`

### Setup live reload:

- Install the [live reload chrome extension](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei?hl=en)

### Setup IntelliJ for the project:

- Configure an additional dictionary in `preferences > editor > proofreading > spelling` using `.idea/dictionaries/shiba.xml`
- Install the Lombok plugin
- Enable annotation processing
- Set the Project SDK to Java 14 in `File > Project Structure`
- Install the EnvFile plugin
- Create `.env` file in the root of the project directory
- Paste in values from `SHIBA application-secrets.yaml` in LastPass with the format `ENV_VAR_NAME=ENV_VAR_VALUE`, translating the property notation to environment variable notation as follows:
    - `shiba.username: someUsername` --> `SHIBA_USERNAME=someUsername`
    - `mail-gun.api-key: someApiKey` --> `MAIL-GUN_API-KEY=someApiKey`
- Generate an encryption key - see instructions in the next section.
- Run the application using ShibaApplication run configuration

### Generate an encryption key:
- Install the `tinkey` command line utility to generate an encryption key, following [their docs](https://github.com/google/tink/blob/master/docs/TINKEY.md)
- Run the following command to generate an encryption key and copy it to your clipboard: `tinkey create-keyset --key-template AES256_GCM | awk '{printf("%s",$0)}' | pbcopy`
- Add the encryption key to the `.env` file as a new environment variable: `ENCRYPTION_KEY=<value from clipboard>`

### Start the local and test databases:

This project uses `docker-compose` to manage local and test databases.
- Install docker-compose: `brew install docker-compose`
- [Install Docker](https://hub.docker.com/editions/community/docker-ce-desktop-mac/)
- Invoke the following from the project directory to start the databases: `docker-compose up -d`

To stop the databases, run:
`docker-compose down` 

### Test:

From the project root invoke
```./gradlew clean test```

### Setup Fake Filler(optional, Chrome only):

- Using an automatic form filler makes manual test easier.
- Install Fake Filler for Chrome(https://chrome.google.com/webstore/detail/fake-filler/bnjjngeaknajbdcgpfkgnonkmififhfo)
- Go to Fake Filler options/backup and restore(chrome-extension://bnjjngeaknajbdcgpfkgnonkmififhfo/options.html#/backup)
- Import the configuration from project root/fakeFillerConfig.txt
- Setup keyboard shortcuts following (chrome-extension://bnjjngeaknajbdcgpfkgnonkmififhfo/options.html#/keyboard-shortcuts)

## SSL Certificates Setup (local and test)

For local and test environments, we rely on self-signed certificates for SSL communication.
The following steps can be followed to set these up. Self-signed server and client certificates require a root CA certificate, which is also self-signed and can be generated with:
 
`openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout rootCA.key -out rootCA.crt`

### Configuring SSL communication between a client (us) and an external server

Client-side self-signed certificates can be generated and installed into a new java keystore:

- Create a certificate signing request, copying the generated certificate request into a file named `client.csr`. A private key will also have been generated called `client.key`.

`openssl req -new -newkey rsa:4096 -nodes -keyout client.key`

- Sign the certificate request using the root CA certificate.

`openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in client.csr -out client.crt -days 365 -CAcreateserial`

- Package the signed certificate `client.crt` and the private key `client.key` into a PKCS file.

`openssl pkcs12 -export -out client.p12 -name "client" -inkey client.key -in client.crt`

- Import the signed certificate and private key into a keystore file.

`keytool -importkeystore -deststorepass <password> -destkeypass <password> -deststoretype pkcs12 -srckeystore client.p12 -srcstoretype pkcs12 -srcstorepass <password> -destkeystore client-keystore.jks -alias client`

The client certificate can then be shared with external servers who can register the client as trusted.

- Client exports the certificate from their keystore.

`keytool -exportcert -alias client -file client.crt -keystore client-keystore.jks -storepass <password>`

- Server imports the certificate into their truststore (java keystore used for cataloging trusted client certs).

`keytool -importcert -keystore server-truststore.jks -alias client -file client.crt -storepass <password>`

### Setting Spring active profiles
Open "Edit Run/Debug configuration" dialog

Enter comma separated names of the profiles in "Active profiles"