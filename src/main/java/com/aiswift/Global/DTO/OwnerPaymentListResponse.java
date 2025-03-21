package com.aiswift.Global.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerPaymentListResponse {
	Long id; 
	String firstName;
	String lastName;
	String email;
	String status;	
	List<PaymentResponse> payments;
}
