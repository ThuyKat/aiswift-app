package com.aiswift.Global.Controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Enum.StripePaymentType;
import com.aiswift.Global.Entity.Payment;
import com.aiswift.Global.Service.PaymentService;
import com.aiswift.Global.Service.StripeService;
import com.aiswift.Global.Service.SubPlanDetailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;


@RestController
@RequestMapping("/stripe/webhook")
public class StripeWebHookController {
	@Autowired
	private PaymentService paymentService;

	@Autowired
	private SubPlanDetailService subPlanDetailService;

	@Value(("${stripe.webhook.secret}"))
	private String endpointSecret;
	
	@Autowired
	private StripeService stripeService;
	
	private static final Logger logger = LoggerFactory.getLogger(StripeWebHookController.class);
	
	// stripe login
	// stripe listen --forward-to localhost:8080/stripe/webhook
	// stripe trigger payment_intent.succeeded

	@PostMapping
	public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
			@RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
		try {
			if (sigHeader == null || sigHeader.contains("fake_signature")) {
				logger.warn("Skipping signature verification for local testing.");
			} else {
				// get event Stripe: payment_intent.succeeded, charge.succeeded..
				Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret); 				
				logger.info("Received Stripe Webhook Event: {}", event.getType());
			
				// get raw JSON inside event
				String deserializer = event.getDataObjectDeserializer().getRawJson();
				
				// change raw JSON to Map<String, Object>
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> map = mapper.readValue(deserializer, new TypeReference<Map<String, Object>>() {});
				
				System.out.println(map.get("id"));
				
				// payment successed
				if ("payment_intent.succeeded".equals(event.getType())) {
					// Stripe config for Stripe API: API key, accountID,..
					RequestOptions requestOptions = stripeService.createRequestOptions(); 
					PaymentIntent paymentIntent = PaymentIntent.retrieve(String.valueOf(map.get("id")), requestOptions);								
					
					// no payment intent on Stripe
					if (paymentIntent == null) {
						logger.error("PaymentIntent is null in Webhook.");
						throw new IllegalStateException("Stripe error: Invalid PaymentIntent.");						
					}
					
					// get payment type and count from metadata
					String paymentTypeStr = paymentIntent.getMetadata().get("payment_type");
					String countStr = paymentIntent.getMetadata().get("count");
					String planIdStr = paymentIntent.getMetadata().get("subscription_plan_id");
					
					if (paymentTypeStr == null && countStr == null) {
						throw new IllegalStateException("Stripe error: Missing metadata fields.");						
					}
					
					//change tp enum from String
					StripePaymentType paymentType = StripePaymentType.fromString(paymentTypeStr);
					
					int count = Integer.parseInt(countStr);
					int planId = Integer.parseInt(planIdStr);
					
					// get Payment object from paymentIntentId
					Payment payment = paymentService.getPaymentByPaymentIntentId(paymentIntent.getId());
					
					// change from "PENDING, FAILED" to SUCCESS
					paymentService.changePaymentStatusToSuccess(payment.getId());
					
					switch (paymentType) {
					case SUBSCRIPTION:
						subPlanDetailService.activateSubscriptionPlan(payment.getSubPlanDetail().getId());
						return ResponseEntity.ok("Subscription Activated. ");
						
					case ADDITIONAL_ADMIN:
					case ADDITIONAL_TENANT:
						// add extra admin or tenant based on paymentType
						subPlanDetailService.updateAdditionalCount(payment.getSubPlanDetail().getId(), paymentType,
								count);

						return ResponseEntity.ok("Subscription Updated.");
						
					case MONTHLY_PAYMENT:
						// update 30 days in next billing cycle
						subPlanDetailService.updateNextBillingDate(payment.getSubPlanDetail().getId());
						return ResponseEntity.ok("Successfully paid for this month Subscription.");
					
						
					case PLAN_UPGRADE:
						//update tenant count for new plan + current addtitional
						subPlanDetailService.updateNewPlanUpgrade(payment.getSubPlanDetail().getId(), paymentType, count, planId);;
						return ResponseEntity.ok("Upgrade to new Plan successfully.");	
					default:
						 throw new IllegalStateException("Stripe error: Invalid payment type.");						
					}

				}
			}
			return ResponseEntity.ok("Webhook received");

		} catch (Exception e) {
			throw new RuntimeException("Webhook error: " + e.getMessage());
		}
	}
}
