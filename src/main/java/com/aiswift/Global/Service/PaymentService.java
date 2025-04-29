package com.aiswift.Global.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiswift.Enum.AdditionalType;
import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.DTO.PlanAdditionalPaymentRequest;
import com.aiswift.Global.DTO.PlanUpgradeRequest;
import com.aiswift.Global.DTO.SubPlanRequest;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.Payment;
import com.aiswift.Global.Entity.PaymentDetail;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.SubscriptionPlan;
import com.aiswift.Global.Repository.PaymentDetailRepository;
import com.aiswift.Global.Repository.PaymentRepository;
import com.aiswift.Global.Repository.ProrataDetailRepository;



@Service
public class PaymentService {

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private PaymentDetailService paymentDetailService;

	@Autowired
	private PaymentDetailRepository paymentDetailRepository;

	@Autowired
	private ProrataDetailService prorataDetailService;
	
	@Autowired
	private ProrataDetailRepository prorataDetailRepository;
	
	static final int PLAN_PAYMENT_ID = 1;
	static final int ADDITIONAL_ADMIN_ID = 2;
	static final int ADDITIONAL_TENAT_ID = 3;
	static final int PLAN_UPGRADE_ID = 6;
	static final int COUNT_ONE = 1;
	
	public Payment getPaymentById(long id) {
		return paymentRepository.findById(id)
				.orElseThrow(() -> new NoDataFoundException("No payment found for this id: " + id));

	}

	public Payment getPaymentByPaymentIntentId(String id) {
		return paymentRepository.findByPaymentIntentId(id)
				.orElseThrow(() -> new NoDataFoundException("No payment found for this id: " + id));

	}
	// save 1st subscription
	@Transactional(transactionManager = "globalTransactionManager")
	public void saveInitPayment(Owner owner, SubPlanDetail planDetail, BigDecimal amount, String paymentIntentId,
			SubPlanRequest request, SubscriptionPlan plan) {
		// save to payment table
		Payment payment = new Payment();
		payment.setOwner(owner);
		payment.setSubPlanDetail(planDetail);
		payment.setAmount(amount);
		payment.setPaymentIntentId(paymentIntentId);
		payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

		payment = paymentRepository.save(payment);

		List<PaymentDetail> paymentDetails = new ArrayList<>();
		// each payment break down to different type
		paymentDetails
				.add(paymentDetailService.createPaymentDetail(payment, PLAN_PAYMENT_ID, plan.getBaseCost(), COUNT_ONE, plan.getBaseCost()));

		if (request.getAdditionalAdminCount() > 0) {
			BigDecimal additionalAdminAmount = plan.getAdditionalAmindFee()
					.multiply(BigDecimal.valueOf(request.getAdditionalAdminCount()));

			paymentDetails.add(paymentDetailService.createPaymentDetail(payment, ADDITIONAL_ADMIN_ID, additionalAdminAmount,
					request.getAdditionalAdminCount(), plan.getAdditionalAmindFee()));

		}
		if (request.getAdditionalTenantCount() > 0) {
			BigDecimal additionalTenantAmount = plan.getAdditionalTenantFee()
					.multiply(BigDecimal.valueOf(request.getAdditionalTenantCount()));

			paymentDetails.add(paymentDetailService.createPaymentDetail(payment, ADDITIONAL_TENAT_ID, additionalTenantAmount,
					request.getAdditionalTenantCount(), plan.getAdditionalTenantFee()));
		}
		if (paymentDetails.isEmpty()) {
			throw new IllegalStateException("No payment details found to save.");
		}
		paymentDetailRepository.saveAll(paymentDetails);
	}

	
	// save extra
	@Transactional(transactionManager = "globalTransactionManager")
	public void savePlanAdditionalPayment(PlanAdditionalPaymentRequest request) {
		// save to payment table
		Payment payment = new Payment();
		payment.setOwner(request.getOwner());
		payment.setSubPlanDetail(request.getPlanDetail());
		payment.setAmount(request.getAmount());
		payment.setPaymentIntentId(request.getPaymentIntentId());
		payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

		payment = paymentRepository.save(payment);

		if (AdditionalType.ADMIN.name().equals(request.getAdditionalType())) {
			prorataDetailRepository.save(
					prorataDetailService.createProrataDetail(
							payment, ADDITIONAL_ADMIN_ID, request.getAmount(), request.getCount(), 
							request.getPlan().getAdditionalAmindFee(), request.getRemainingDays()));
			
		}
		if (AdditionalType.TENANT.name().equals(request.getAdditionalType())) {
			prorataDetailRepository.save(
					prorataDetailService.createProrataDetail(
							payment, ADDITIONAL_TENAT_ID, request.getAmount(), request.getCount(), 
							request.getPlan().getAdditionalTenantFee(), request.getRemainingDays()));
		}		

	}
	// save upgrade
		@Transactional(transactionManager = "globalTransactionManager")
		public void savePlanUpgradePayment(PlanUpgradeRequest request) {
			// save to payment table
			Payment payment = new Payment();
			payment.setOwner(request.getOwner());
			payment.setSubPlanDetail(request.getPlanDetail());
			payment.setAmount(request.getAmount());
			payment.setPaymentIntentId(request.getPaymentIntentId());
			payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

			payment = paymentRepository.save(payment);
			
			prorataDetailRepository.save(
					prorataDetailService.createProrataDetail(
							payment, PLAN_UPGRADE_ID, request.getAmount(), COUNT_ONE, 
							request.getPlan().getBaseCost(), request.getRemainingDays()));	

		}
	
		// save 1st subscription
		@Transactional(transactionManager = "globalTransactionManager")
		public void saveMonthlyPayment(Owner owner, SubPlanDetail planDetail, BigDecimal amount, String paymentIntentId,
				SubscriptionPlan plan) {
			// save to payment table
			Payment payment = new Payment();
			payment.setOwner(owner);
			payment.setSubPlanDetail(planDetail);
			payment.setAmount(amount);
			payment.setPaymentIntentId(paymentIntentId);
			payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

			payment = paymentRepository.save(payment);

			List<PaymentDetail> paymentDetails = new ArrayList<>();
			// each payment break down to different type
			paymentDetails
					.add(paymentDetailService.createPaymentDetail(payment, PLAN_PAYMENT_ID, plan.getBaseCost(), COUNT_ONE, plan.getBaseCost()));
			
			int additionalAdminCount = planDetail.getAdditionalAdminCount();
			int additionalTenantCount = planDetail.getAdditionalTenantCount();
			
			if (additionalAdminCount > 0) {
				BigDecimal additionalAdminFee = plan.getAdditionalAmindFee()
						.multiply(BigDecimal.valueOf(additionalAdminCount));

				paymentDetails.add(paymentDetailService.createPaymentDetail(payment, ADDITIONAL_ADMIN_ID, additionalAdminFee,
						additionalAdminCount, plan.getAdditionalAmindFee()));

			}
			if (additionalTenantCount > 0) {
				BigDecimal additionalTenantFee = plan.getAdditionalTenantFee()
						.multiply(BigDecimal.valueOf(additionalTenantCount));

				paymentDetails.add(paymentDetailService.createPaymentDetail(payment, ADDITIONAL_TENAT_ID, additionalTenantFee,
						additionalTenantCount, plan.getAdditionalTenantFee()));
			}
			if (paymentDetails.isEmpty()) {
				throw new IllegalStateException("No payment details found to save.");
			}
			paymentDetailRepository.saveAll(paymentDetails);
		}
	
	//change status
	@Transactional(transactionManager = "globalTransactionManager")
	public void changePaymentStatusToSuccess(long paymentId) {
		Payment payment = getPaymentById(paymentId);

		payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);

		paymentRepository.save(payment);
	}

	// for payment-retry
	@Transactional(transactionManager = "globalTransactionManager")
	public void changePaymentStatusToFailed(long paymentId, String newPaymentIntentId) {
		Payment payment = getPaymentById(paymentId);

		payment.setPaymentIntentId(newPaymentIntentId);
		payment.setPaymentStatus(Payment.PaymentStatus.FAILED);

		paymentRepository.save(payment);
	}
}
