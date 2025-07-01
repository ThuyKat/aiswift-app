package com.aiswift.Global.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiswift.Enum.StripePaymentType;
import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.DTO.SubPlanRequest;
import com.aiswift.Global.DTO.TenantLogDTO;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.SubPlanDetail.PlanDetailStatus;
import com.aiswift.Global.Entity.SubscriptionPlan;
import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Global.Repository.SubPlanDetailRepository;

@Service
public class SubPlanDetailService {

	@Autowired
	private SubPlanDetailRepository subPlanDetailRepository;
	
	@Autowired
	private SubscriptionPlanService subscriptionPlanService;
	
	@Autowired
	private TenantActivityLogService tenantActivityLogService;
	
	private static final int ADD_ADDITIONAL_ADMIN = 1;
	private static final int ADD_ADDITIONAL_TENANT = 5;
	private static final int CREATE_NEW_TENANT = 7;
	private static final int UPGRADE_PLAN = 8;

	public SubPlanDetail getSubPlanDetailById(long id) {
		return subPlanDetailRepository.findById(id)
				.orElseThrow(() -> new NoDataFoundException("No plan details found for this id: " + id));
	}
	public SubPlanDetail saveEntity(SubPlanDetail planDetail) {
		return subPlanDetailRepository.save(planDetail);
	}
	
	public SubPlanDetail getLatestPlanDetailByOwner(Owner owner) {

		return subPlanDetailRepository.findTopByOwnerOrderBySubscriptionStartDesc(owner)
				.orElse(null);
		}

//		return subPlanDetailRepository.findTopByOwnerOrderBySubscriptionStartDesc(owner)
//				.orElseThrow(() -> new NoDataFoundException("No Subscription Plan Detail found."));

	
	@Transactional(transactionManager = "globalTransactionManager")
	public SubPlanDetail changeOwnerLatestPlanDetailStatus(Owner owner, String status, long requestPlanDetailId) {
		try {
			SubPlanDetail planDetail = getLatestPlanDetailByOwner(owner);
			
			if(planDetail.getId() != requestPlanDetailId) {
				throw new IllegalArgumentException("Invalid plan detail Id.");
			}						
			PlanDetailStatus newStatus = PlanDetailStatus.valueOf(status.toUpperCase());
			planDetail.setStatus(newStatus);
			
			return subPlanDetailRepository.save(planDetail);
			
		}catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid status: " + status);
		}		
	}
	
	// + 30 days billing cycle
	public void updateNextBillingDate(long id) {	
		SubPlanDetail planDetail = getSubPlanDetailById(id);
		planDetail.setNextBillingDate(planDetail.getNextBillingDate().plusDays(30));		
		subPlanDetailRepository.save(planDetail);
	}
	
	// get list to check Billing cycle
	public List<SubPlanDetail> getPlanDetailListByDateAndStatus(LocalDate date, SubPlanDetail.PlanDetailStatus status){
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.atTime(23, 59, 59);
		
		List<SubPlanDetail> list = subPlanDetailRepository.findByNextBillingDateAndStatus(startOfDay, endOfDay, status);		
		if(list.isEmpty() || list == null) {
			throw new NoDataFoundException("No Billing Cycle found.");
		}		
		return list;		
	}	
	
	// initial saving data to the table, Status: INACTIVE
	@Transactional(transactionManager = "globalTransactionManager")
	public SubPlanDetail saveInitialPlanDetail(SubPlanRequest request, Owner owner, SubscriptionPlan plan) {
		SubPlanDetail planDetail = new SubPlanDetail();
		planDetail.setOwner(owner);
		planDetail.setSubscriptionPlan(plan);
		planDetail.setAdditionalTenantCount(request.getAdditionalTenantCount());
		planDetail.setMaxTenant(plan.getTenantLimit() + request.getAdditionalTenantCount());
		planDetail.setAdditionalAdminCount(request.getAdditionalAdminCount());
		planDetail.setStatus(SubPlanDetail.PlanDetailStatus.INACTIVE);

		return subPlanDetailRepository.save(planDetail);
	}

	// change from INACTIVE to ACTIVE
	@Transactional(transactionManager = "globalTransactionManager")
	public void activateSubscriptionPlan(long planDetailId) {
		SubPlanDetail planDetail = getSubPlanDetailById(planDetailId);

		// update plan from INACTIVE to ACTIVE when payment success
		planDetail.setStatus(SubPlanDetail.PlanDetailStatus.ACTIVE);
		planDetail.setSubscriptionStart(LocalDateTime.now());

		subPlanDetailRepository.save(planDetail);
	}

	// UPDATE Active tenant count, when Owner create new tenant
	@Transactional(transactionManager = "globalTransactionManager")
	public void updateActiveTenantCount(long planDetailId, Owner owner, Tenant tenant) {
		TenantLogDTO tenantLogDTO = new TenantLogDTO();
		SubPlanDetail planDetail = getSubPlanDetailById(planDetailId);
		int activeTenantCount = planDetail.getActiveTenantCount();
		int updatedActiveTenantCount = activeTenantCount + 1;

		planDetail.setActiveTenantCount(updatedActiveTenantCount);
		subPlanDetailRepository.save(planDetail);
		
		tenantLogDTO.setOwner(owner);
		tenantLogDTO.setTenantId(tenant.getId());
		// CREATE NEW TENANT;
		tenantLogDTO.setActionTypeId(CREATE_NEW_TENANT);
		tenantLogDTO.setOldValue(String.valueOf(activeTenantCount));
		tenantLogDTO.setNewValue(String.valueOf(updatedActiveTenantCount));
		tenantLogDTO.setMessage(String.format("Create new Tenant with database name: %s", tenant.getDbName()));	
		
		tenantActivityLogService.createTenantActivityLog(tenantLogDTO);	
	}

	// change admin or tenant count
	@Transactional(transactionManager = "globalTransactionManager")
	public void updateAdditionalCount(long planDetailId, StripePaymentType paymentType, int count) {
		SubPlanDetail planDetail = getSubPlanDetailById(planDetailId);
		
		TenantLogDTO tenantLogDTO = new TenantLogDTO();		
		Owner owner = planDetail.getOwner();
		
		tenantLogDTO.setOwner(owner);
		tenantLogDTO.setTenantId(-1);	
		  try {
		// ENUM is a SINGLETON, 1 object 1 reference
		if (paymentType == StripePaymentType.ADDITIONAL_ADMIN) {
			planDetail.setAdditionalAdminCount(planDetail.getAdditionalAdminCount() + count);				
			tenantLogDTO.setActionTypeId(ADD_ADDITIONAL_ADMIN); // ADD NEW ADMIN
			tenantLogDTO.setOldValue(String.valueOf(planDetail.getAdditionalAdminCount() - count));
			tenantLogDTO.setNewValue(String.valueOf(planDetail.getAdditionalAdminCount()));
			tenantLogDTO.setMessage(String.format("Add %d new additional Admin", count));	
		}
		if (paymentType == StripePaymentType.ADDITIONAL_TENANT) {
			planDetail.setAdditionalTenantCount(planDetail.getAdditionalTenantCount() + count);
			planDetail.setMaxTenant(planDetail.getMaxTenant() + count);	
						
			tenantLogDTO.setActionTypeId(ADD_ADDITIONAL_TENANT); // ADD NEW TENANT
			tenantLogDTO.setOldValue(String.valueOf(planDetail.getAdditionalTenantCount() - count));
			tenantLogDTO.setNewValue(String.valueOf(planDetail.getAdditionalTenantCount()));
			tenantLogDTO.setMessage(String.format("Add %d new additional tenant, Max tenant now is %d", count,
					planDetail.getMaxTenant()));	
		}
		
		subPlanDetailRepository.save(planDetail);
		System.out.println("SAVE PLAN DETAIL??");		 
		try {
            tenantActivityLogService.createTenantActivityLog(tenantLogDTO);
            System.out.println("UPDATE LOG");
        } catch (Exception e) {
            System.err.println("Error saving tenant log: " + e.getMessage());
            e.printStackTrace();
        }

    } catch (Exception e) {
        System.err.println("Error in transaction: " + e.getMessage());
        e.printStackTrace();
    }
	}
	//update tenant count for new plan + current addtitional
	@Transactional(transactionManager = "globalTransactionManager")
	public void updateNewPlanUpgrade(long planDetailId, StripePaymentType paymentType, int count, int newPlanId) {
		SubPlanDetail planDetail = getSubPlanDetailById(planDetailId);
		// get old plan id		
		int oldPlanId = planDetail.getSubscriptionPlan().getId();
		
		// get new plan
		SubscriptionPlan plan = subscriptionPlanService.getPlanById(newPlanId);
		//save new plan + update max tenant
		planDetail.setSubscriptionPlan(plan);
		planDetail.setMaxTenant(plan.getTenantLimit() + planDetail.getAdditionalTenantCount());
		subPlanDetailRepository.save(planDetail);
		
		//update log
		TenantLogDTO tenantLogDTO = new TenantLogDTO();		
		Owner owner = planDetail.getOwner();
		
		tenantLogDTO.setOwner(owner);
		tenantLogDTO.setTenantId(-1);
		// UPGRADE PLAN 8;
		tenantLogDTO.setActionTypeId(UPGRADE_PLAN);
		tenantLogDTO.setOldValue(String.valueOf(oldPlanId));
		tenantLogDTO.setNewValue(String.valueOf(newPlanId));
		tenantLogDTO.setMessage(String.format("Upgrade to new plan: %s", plan.getName()));	
		
		tenantActivityLogService.createTenantActivityLog(tenantLogDTO);	
	}
}
