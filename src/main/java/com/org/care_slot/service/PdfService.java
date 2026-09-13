package com.org.care_slot.service;

import java.util.Map;

public interface PdfService {
    byte[] generatePdfFromTemplate(String templateName, Map<String, Object> variables);
}
