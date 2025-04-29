package com.aiswift.Global.DTO;

import java.math.BigDecimal;

import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.SubscriptionPlan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanUpgradeRequest {
	private Owner owner;
	private SubscriptionPlan plan;
	private SubPlanDetail planDetail;	
	private int count;
	private int remainingDays;
	private BigDecimal amount;
	private String paymentIntentId;
}
