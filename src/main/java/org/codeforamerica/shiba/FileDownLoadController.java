package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.PdfGenerator;
import org.codeforamerica.shiba.xml.XmlGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FileDownLoadController {
    private final PagesConfiguration pagesConfiguration;
    private final PagesData data;
    private final PdfGenerator pdfGenerator;
    private final XmlGenerator xmlGenerator;

    public FileDownLoadController(
            PagesConfiguration pagesConfiguration,
            PagesData data,
            PdfGenerator pdfGenerator,
            XmlGenerator xmlGenerator
    ) {
        this.pagesConfiguration = pagesConfiguration;
        this.data = data;
        this.pdfGenerator = pdfGenerator;
        this.xmlGenerator = xmlGenerator;
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadPdf() {
        ApplicationFile applicationFile = pdfGenerator.generate(ApplicationInputs.from(pagesConfiguration, data));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-xml")
    ResponseEntity<byte[]> downloadXml() {
        ApplicationFile applicationFile = xmlGenerator.generate(ApplicationInputs.from(pagesConfiguration, data));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }
}
