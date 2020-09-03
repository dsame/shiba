package org.codeforamerica.shiba;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Configuration
public class MnitEsbWebServiceTemplateConfiguration {
    @Bean
    WebServiceTemplate webServiceTemplate(WebServiceTemplateBuilder webServiceTemplateBuilder,
                                          SSLContextBuilder sslContextBuilder,
                                          @Value("${mnit-esb.f5-username}") String f5Username,
                                          @Value("${mnit-esb.f5-password}") String f5Password,
                                          @Value("${mnit-esb.jaxb-context-path}") String jaxbContextPath,
                                          @Value("${mnit-esb.url}") String url) throws KeyManagementException, NoSuchAlgorithmException {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath(jaxbContextPath);
        String auth = f5Username + ":" + f5Password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        HttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
                .setSSLContext(sslContextBuilder.build())
//                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setDefaultHeaders(List.of(new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth))))
                .build();
        return webServiceTemplateBuilder
                .setDefaultUri(url)
                .setMarshaller(jaxb2Marshaller)
                .setUnmarshaller(jaxb2Marshaller)
                .messageSenders(new HttpComponentsMessageSender(httpClient))
                .build();
    }
}
