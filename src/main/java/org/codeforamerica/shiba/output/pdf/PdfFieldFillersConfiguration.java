package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.DocumentType;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Configuration
public class PdfFieldFillersConfiguration {
    @Bean
    public PdfFieldFiller caseworkerCafFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:caf-body.pdf") Resource cafBody,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(coverPages, cafBody), font);
    }

    @Bean
    public PdfFieldFiller clientCafFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:caf-standard-headers.pdf") Resource standardHeaders,
            @Value("classpath:caf-body.pdf") Resource cafBody,
            @Value("classpath:caf-standard-footers.pdf") Resource standardFooters,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(
                coverPages, standardHeaders, cafBody, standardFooters
        ), font);
    }

    @Bean
    public PdfFieldFiller caseworkerCcapFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:ccap-cover.pdf") Resource ccapCover,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(
                coverPages, ccapCover
        ), font);
    }

    @Bean
    public PdfFieldFiller clientCcapFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:ccap-cover.pdf") Resource ccapCover,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(
                coverPages, ccapCover
        ), font);
    }

    @Bean
    public Map<Recipient, Map<DocumentType, PdfFieldFiller>> pdfFieldFillers(
            PdfFieldFiller caseworkerCafFiller,
            PdfFieldFiller clientCafFiller,
            PdfFieldFiller caseworkerCcapFiller,
            PdfFieldFiller clientCcapFiller
    ) {
        return Map.of(
                CASEWORKER, Map.of(
                        DocumentType.CAF, caseworkerCafFiller,
                        DocumentType.CCAP, caseworkerCcapFiller),
                CLIENT, Map.of(
                        DocumentType.CAF, clientCafFiller,
                        DocumentType.CCAP, clientCcapFiller)
        );
    }
}
