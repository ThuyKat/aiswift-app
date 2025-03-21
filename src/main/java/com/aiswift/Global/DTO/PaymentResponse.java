package com.aiswift.Global.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aiswift.Global.Entity.Payment.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
	long id;
	BigDecimal amount;
	PaymentStatus status;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
}
