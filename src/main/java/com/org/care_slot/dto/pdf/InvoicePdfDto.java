package com.org.care_slot.dto.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePdfDto {
    private Long invoiceId;
    private Long patientId;
    private Long visitId;
    private String invoiceCode;
    private String invoiceType;
    private String patientName;
    private int age;
    private String gender;
    private String phone;
    private String address;
    private String clinicName;
    private String status;
    private LocalDateTime paidAt;
    private BigDecimal totalAmount;
    private List<InvoiceItemPdfDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemPdfDto {
        private int index;
        private String itemName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }
}
