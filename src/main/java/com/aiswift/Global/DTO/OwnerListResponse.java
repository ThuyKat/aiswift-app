package com.aiswift.Global.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerListResponse {
	Long id; 
	String firstName;
	String lastName;
	String email;	
	String status;
	String createdBy;
	String updatedBy;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
}
