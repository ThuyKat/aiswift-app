package com.aiswift.Global.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponse {
	long id;
	String paymentType;
	int quantity;
	BigDecimal unitPrice;
	BigDecimal amount;
	LocalDateTime createdAt;
}
