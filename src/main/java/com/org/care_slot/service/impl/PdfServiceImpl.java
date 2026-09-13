package com.org.care_slot.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.org.care_slot.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {

    private final TemplateEngine templateEngine;
    private File cachedFontFile = null;

    private synchronized File getFontFile() {
        if (cachedFontFile != null && cachedFontFile.exists()) {
            return cachedFontFile;
        }
        try {
            ClassPathResource fontResource = new ClassPathResource("fonts/Roboto-Regular.ttf");
            if (fontResource.exists()) {
                File temp = File.createTempFile("pdf_roboto_", ".ttf");
                temp.deleteOnExit();
                try (InputStream is = fontResource.getInputStream()) {
                    Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                cachedFontFile = temp;
                return cachedFontFile;
            }
        } catch (Exception e) {
            log.warn("Could not load bundled font file from classpath: {}", e.getMessage());
        }

        // Fallback to Windows system font if present
        File sysFont = new File("C:/Windows/Fonts/arial.ttf");
        if (sysFont.exists()) {
            return sysFont;
        }
        return null;
    }

    @Override
    public byte[] generatePdfFromTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process("pdf/" + templateName, context);
            if (htmlContent != null) {
                htmlContent = htmlContent.replaceAll("&(?!(amp|lt|gt|quot|apos|#\\d+|#x[0-9a-fA-F]+);)", "&amp;");
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "");

            File fontFile = getFontFile();
            if (fontFile != null && fontFile.exists()) {
                builder.useFont(fontFile, "Roboto");
                builder.useFont(fontFile, "Arial");
                builder.useFont(fontFile, "Times New Roman");
                builder.useFont(fontFile, "sans-serif");
            }

            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF from template {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Lỗi sinh file PDF: " + e.getMessage(), e);
        }
    }
}
