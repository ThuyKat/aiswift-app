package com.aiswift.Global.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Service.DeveloperService;
import com.aiswift.Global.Service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.aiswift.Global.DTO.OwnerListResponse;
import com.aiswift.Global.DTO.OwnerPaymentListResponse;
import com.aiswift.Global.DTO.OwnerPlanDetailListResponse;
import com.aiswift.Global.DTO.OwnerTenantListResponse;
import com.aiswift.Global.DTO.PaymentResponse;
import com.aiswift.Global.Entity.Developer;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.TenantActivityLog;
import com.aiswift.Global.Service.DeveloperService;
import com.aiswift.Global.Service.OwnerService;
import com.aiswift.Global.Service.PaymentDetailService;
import com.aiswift.Global.Service.PaymentService;
import com.aiswift.Global.Service.ProrataDetailService;
import com.aiswift.Global.Service.SubPlanDetailService;
import com.aiswift.Global.Service.TenantActivityLogService;

import jakarta.validation.constraints.Min;



@RestController
@RequestMapping("/api/developer")
public class DeveloperController {

	@Autowired
	private DeveloperService developerService;

	@Autowired
	private OwnerService ownerService;
	
	@Autowired
	private SubPlanDetailService subPlanDetailService;
	
	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private ProrataDetailService prorataDetailService;
	
	@Autowired
	private PaymentDetailService paymentDetailService;
	
	@Autowired
	private TenantActivityLogService tenantActivityLogService;
	
	private final static Logger logger = LoggerFactory.getLogger(DeveloperController.class);	
	//1	
	@GetMapping("/owner-list")
	public ResponseEntity<Object> getOwnerList(Principal principal) {
		
	        List<Owner> owners = ownerService.findAll();
	        List<OwnerListResponse> ownerListResponses = owners.stream()
	            .map(owner -> new OwnerListResponse(
	                owner.getId(),
	                owner.getFirstName(),
	                owner.getLastName(),
	                owner.getEmail(),
	                owner.getStatus().name(),
	                owner.getCreatedBy(),
	                owner.getUpdatedBy(),
	                owner.getCreatedAt(),
	                owner.getUpdatedAt()	               
	            ))
	            .collect(Collectors.toList());
	    logger.info("Successfully retrieved owner list.");
	    
	        return new ResponseEntity<>(Map.of(
	            "message", "Successfully retrieved the Owner List", 
	            "ownerList", ownerListResponses), 
	            HttpStatus.OK);
	   
	}
	
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PutMapping("/edit-role")
	public ResponseEntity<Object> changeDeveloperRole(Principal principal, 
			@RequestParam(required = true) String devEmail, @RequestParam(required = true) String role) {
		
		Developer dev = developerService.changeDevToNewRole(devEmail, role);
		
		 return ResponseEntity.ok(
				 Map.of("message", "Successfully change to new role: "+ dev.getRole().toString()));
	}
	//2
	@GetMapping("/owner/{id}/tenant-list")
	public ResponseEntity<Object> getOwnerDetailById(@PathVariable @Min(1) long id){
		Owner owner = ownerService.getOwnerById(id);
		OwnerTenantListResponse ownerResponse = new OwnerTenantListResponse();
		ownerResponse.setId(owner.getId());
		ownerResponse.setFirstName(owner.getFirstName());
		ownerResponse.setLastName(owner.getLastName());
		ownerResponse.setEmail(owner.getEmail());
		ownerResponse.setTenants(owner.getTenants());
		ownerResponse.setPlanDetail(subPlanDetailService.getLatestPlanDetailByOwner(owner));
		
		 return ResponseEntity.ok(
				 Map.of("message", "Successfully retrieved owner's tenant list.",
						 "owner", ownerResponse));
	}
	//3
	@GetMapping("/owner/{id}/plan-detail-list")
	public ResponseEntity<Object> getOwnerwithAllPlanDetailById(@PathVariable @Min(1) long id){
		Owner owner = ownerService.getOwnerWithSubPlanDetailsById(id);
		OwnerPlanDetailListResponse response = new OwnerPlanDetailListResponse();
		response.setId(owner.getId());
		response.setFirstName(owner.getFirstName());
		response.setLastName(owner.getLastName());
		response.setEmail(owner.getEmail());
		response.setPlanDetails(owner.getSubPlanDetails());
		
		 return ResponseEntity.ok(
				 Map.of("message", "Successfully retrieved owner's plan detail list.",
						 "owner", response));
	}
	//4
	@GetMapping("/owner/{id}/activity-log")
	public ResponseEntity<Object> getOwnerActivityLog(@PathVariable @Min(1) long id){
		Owner owner = ownerService.getOwnerById(id);
		List<TenantActivityLog> logList = tenantActivityLogService.getLogByOwnerId(owner.getId());	
		
		 return ResponseEntity.ok(
				 Map.of("message", "Successfully retrieved owner's activity log list.",
						 "ownerActityLog", logList));
	}
	
	//5 	
	@PutMapping("/owner/{id}/edit-status")
	public ResponseEntity<Object> changeDeveloperRole(
			@PathVariable @Min(1) long id, @RequestParam String status, Principal principal) {		
		Owner owner = ownerService.changeOwnerStatus(id, status);		
		 return ResponseEntity.ok(
				 Map.of("message", "Successfully change owner's status: "+ owner.getStatus().name()));
	}
	
	//6	-> EVENT LISTENER?? Logging, Notification Emails, Asynchronous Processing
	@PutMapping("/owner/{ownerId}/plan-detail/{id}/status")
		public ResponseEntity<Object> changeOwnerLatestPlanDetailStatus(
				@PathVariable @Min(1) long ownerId, @PathVariable @Min(1) long id,
				@RequestParam String status, Principal principal) {		
			Owner owner = ownerService.getOwnerById(ownerId);
			
			SubPlanDetail planDetail = subPlanDetailService.changeOwnerLatestPlanDetailStatus(owner, status, id);
				
			 return ResponseEntity.ok(
					 Map.of("message", "Successfully change owner's latest plan detail status: "+ planDetail.getStatus().name()));
		}
	
	//7 
	@GetMapping("/owner/{id}/payment-list")
	public ResponseEntity<Object> getOwnerPaymentActivityList(@PathVariable @Min(1) long id){
		Owner owner = ownerService.getOwnerById(id);
		
		OwnerPaymentListResponse response = new OwnerPaymentListResponse();
		response.setId(owner.getId());
		response.setFirstName(owner.getFirstName());
		response.setLastName(owner.getLastName());
		response.setEmail(owner.getEmail());

		response.setPayments(
			    owner.getPayments().stream()
			        .map(payment -> 
			            new PaymentResponse(
			                payment.getId(),
			                payment.getAmount(),
			                payment.getPaymentStatus(),
			                payment.getCreatedAt(), 
			                payment.getUpdatedAt()
			            ) 
			        ) 
			        .collect(Collectors.toList())
			);	
		
		 return ResponseEntity.ok(
				 Map.of("message", "Successfully retrieved owner's payment activity list.",
						 "owner", response));
	}
	
	//8
	@GetMapping("/owner/{ownerId}/payment/{id}/details")
	public ResponseEntity<Object> getOwnerPaymentActivityList(
			@PathVariable @Min(1) long ownerId, @PathVariable @Min(1) long id){
		Owner owner = ownerService.getOwnerWithPayments(ownerId);
		
//		List<Object T> response = 
		
		 return ResponseEntity.ok().build();
	}
	
	// PAGINATION
	//1. list of owner then click to owner will show (2)
	//2. get 1 Owner with tenants and current plan detail (status, next payment.., extra) 
	//3. plan detail: show previous plan from that owner
	//4. owner activity log
	//5. Change Owner status
	//6. change plan detail status 
	//7. show all payments -> 8
	//8. click on 1 payment will show either prorataDetail or paymentDetail
	//9. schedule to inactiviate owner and plan
	
	
}
