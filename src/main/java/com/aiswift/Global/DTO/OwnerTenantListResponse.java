package com.aiswift.Global.DTO;

import java.util.List;

import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.Tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerTenantListResponse {
	Long id; 
	String firstName;
	String lastName;
	String email;
	String status;
	List<Tenant>tenants;
	SubPlanDetail planDetail;
}
