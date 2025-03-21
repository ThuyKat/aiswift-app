package com.aiswift.Global.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.Entity.PaymentType;
import com.aiswift.Global.Repository.PaymentTypeRepository;

@Service
public class PaymentTypeService {

	@Autowired
	private PaymentTypeRepository paymentTypeRepository;
	
	public PaymentType getPaymentTypeById(int id) {
		return paymentTypeRepository.findById(id)
				.orElseThrow(() -> new NoDataFoundException("No Payment Type found with Id: " + id));
	}
}
