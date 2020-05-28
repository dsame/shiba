package org.codeforamerica.shiba;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pdf.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class CAFFieldFillerTest {

    private final CAFFieldFiller subject = new CAFFieldFiller(new ClassPathResource("test.pdf"));

    @Test
    void shouldMapTextFields() throws IOException {
        String expectedFieldValue = "Michael";
        Collection<PdfField> fields = List.of(
                new SimplePdfField("TEXT_FIELD", expectedFieldValue)
        );

        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);

        assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo(expectedFieldValue);
    }

    @Test
    void shouldMapDateFields() throws IOException {
        Collection<PdfField> fields = List.of(
                new DatePdfField("DATE_FIELD", LocalDate.of(2020, 1, 31))
        );

        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);

        assertThat(acroForm.getField("DATE_FIELD").getValueAsString()).isEqualTo("01/31/2020");
    }

    @Test
    void shouldMapToggleFields() throws IOException {
        Collection<PdfField> fields = List.of(new TogglePdfField("TOGGLE_FIELD", true));
        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);

        assertThat(acroForm.getField("TOGGLE_FIELD").getValueAsString()).isEqualTo("LEFT");
    }

    @Test
    void shouldMapRadioFields() throws IOException {
        Collection<PdfField> fields = List.of(new SimplePdfField("RADIO_FIELD", "RADIO_OPTION_1"));

        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);

        assertThat(acroForm.getField("RADIO_FIELD").getValueAsString()).isEqualTo("RADIO_OPTION_1");
    }

    @Test
    void shouldSetTheAppropriateNonValueForTheFieldType() throws IOException {
        Collection<PdfField> fields = List.of(
                new SimplePdfField("TEXT_FIELD", null),
                new TogglePdfField("TOGGLE_FIELD", (Boolean) null)
        );

        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);
        assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo("");
        assertThat(acroForm.getField("TOGGLE_FIELD").getValueAsString()).isEqualTo("Off");
    }

    @Test
    void shouldMapMultipleChoiceFields() throws IOException {
        Collection<PdfField> fields = List.of(
                new BinaryPdfField("BINARY_FIELD_1", true),
                new BinaryPdfField("BINARY_FIELD_2", false),
                new BinaryPdfField("BINARY_FIELD_3", true)
        );

        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);
        assertThat(acroForm.getField("BINARY_FIELD_1").getValueAsString()).isEqualTo("Yes");
        assertThat(acroForm.getField("BINARY_FIELD_2").getValueAsString()).isEqualTo("Off");
        assertThat(acroForm.getField("BINARY_FIELD_3").getValueAsString()).isEqualTo("Yes");
    }

    @Test
    void shouldReturnTheAppropriateFilename() {
        ApplicationFile applicationFile = subject.fill(emptyList());

        assertThat(applicationFile.getFileName()).isEqualTo("test.pdf");
    }

    private PDAcroForm getPdAcroForm(ApplicationFile applicationFile) throws IOException {
        Path path = Files.createTempDirectory("");
        File file = new File(path.toFile(), "test.pdf");
        Files.write(file.toPath(), applicationFile.getFileBytes());

        PDDocument pdDocument = PDDocument.load(file);
        return pdDocument.getDocumentCatalog().getAcroForm();
    }
}